package io.shaama.textrag.rag;

import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class DocumentProcessingService {

    /**
     * Processes a document by reading its content, splitting on double newlines,
     * and removing empty fields. Supports text files, Word documents (.docx), and PDF files (.pdf).
     *
     * @param document the multipart file to process
     * @return list of processed document chunks
     * @throws IOException if there's an error reading the file
     */
    public List<String> processDocument(MultipartFile document) throws IOException {
        if (document.isEmpty()) {
            log.warn("Received empty document");
            return new ArrayList<>();
        }

        String content = extractContent(document);
        log.debug("Read document content with length: {}", content.length());

        // Split content on double newlines ("\n\n")
        List<String> chunks = Arrays.stream(content.split("\n\n"))
                .map(String::trim) // Trim whitespace from each chunk
                .filter(chunk -> !chunk.isEmpty()) // Remove empty chunks
                .collect(Collectors.toList());

        log.info("Processed document '{}' into {} non-empty chunks",
                document.getOriginalFilename(), chunks.size());

        return (chunks);
    }

    /**
     * Extracts text content from the uploaded file based on its type.
     *
     * @param document the multipart file to extract content from
     * @return the extracted text content
     * @throws IOException if there's an error reading the file
     */
    private String extractContent(MultipartFile document) throws IOException {
        String filename = document.getOriginalFilename();
        String contentType = document.getContentType();

        log.debug("Processing file: {} with content type: {}", filename, contentType);

        // Check if it's a PDF document
        if (isPdfDocument(filename, contentType)) {
            return extractFromPdfDocument(document);
        }
        // Check if it's a Word document
        else if (isWordDocument(filename, contentType)) {
            return extractFromWordDocument(document);
        } else {
            // Default to text extraction
            return new String(document.getBytes(), StandardCharsets.UTF_8);
        }
    }

    /**
     * Determines if the file is a Word document based on filename and content type.
     *
     * @param filename the name of the file
     * @param contentType the content type of the file
     * @return true if it's a Word document, false otherwise
     */
    private boolean isWordDocument(String filename, String contentType) {
        if (filename != null) {
            String lowerFilename = filename.toLowerCase();
            if (lowerFilename.endsWith(".docx")) {
                return true;
            }
        }

        if (contentType != null) {
            return contentType.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        }

        return false;
    }

    /**
     * Extracts text content from a Word document (.docx).
     *
     * @param document the Word document to extract text from
     * @return the extracted text content
     * @throws IOException if there's an error reading the Word document
     */
    private String extractFromWordDocument(MultipartFile document) throws IOException {
        log.debug("Extracting text from Word document: {}", document.getOriginalFilename());

        try (XWPFDocument wordDocument = new XWPFDocument(document.getInputStream())) {
            List<XWPFParagraph> paragraphs = wordDocument.getParagraphs();
            StringBuilder content = new StringBuilder();

            for (XWPFParagraph paragraph : paragraphs) {
                String text = paragraph.getText();
                if (text != null && !text.trim().isEmpty()) {
                    content.append(text).append("\n\n");
                }
            }

            String extractedContent = content.toString().trim();
            log.debug("Extracted {} characters from Word document", extractedContent.length());

            return extractedContent;
        } catch (Exception e) {
            log.error("Failed to extract text from Word document: {}", e.getMessage(), e);
            throw new IOException("Failed to process Word document: " + e.getMessage(), e);
        }
    }

    /**
     * Determines if the file is a PDF document based on filename and content type.
     *
     * @param filename the name of the file
     * @param contentType the content type of the file
     * @return true if it's a PDF document, false otherwise
     */
    private boolean isPdfDocument(String filename, String contentType) {
        if (filename != null) {
            String lowerFilename = filename.toLowerCase();
            if (lowerFilename.endsWith(".pdf")) {
                return true;
            }
        }

        if (contentType != null) {
            return contentType.equals("application/pdf");
        }

        return false;
    }

    /**
     * Extracts text content from a PDF document using Apache PDFBox.
     *
     * @param document the PDF document to extract text from
     * @return the extracted text content
     * @throws IOException if there's an error reading the PDF document
     */
    private String extractFromPdfDocument(MultipartFile document) throws IOException {
        log.debug("Extracting text from PDF document: {}", document.getOriginalFilename());

        try (PDDocument pdfDocument = PDDocument.load(document.getInputStream())) {
            PDFTextStripper textStripper = new PDFTextStripper();
            
            // Configure text stripper for better text extraction
            textStripper.setSortByPosition(true);
            textStripper.setLineSeparator("\n");
            textStripper.setWordSeparator(" ");
            textStripper.setParagraphStart("");
            textStripper.setParagraphEnd("\n\n");
            
            String extractedContent = textStripper.getText(pdfDocument);
            
            log.debug("Extracted {} characters from PDF document", extractedContent.length());
            log.debug("PDF document has {} pages", pdfDocument.getNumberOfPages());

            return extractedContent.trim();
        } catch (Exception e) {
            log.error("Failed to extract text from PDF document: {}", e.getMessage(), e);
            throw new IOException("Failed to process PDF document: " + e.getMessage(), e);
        }
    }

    /**
     * Formats a string by removing serial numbers and converting to Question/Answer format.
     * Example: "1. What is AI?\nAI is..." becomes "Question: What is AI?, Answer: AI is..."
     *
     * @param input the string to format
     * @return formatted string in "Question: <question>, Answer: <answer>" format
     */
    public String formatQuestionAnswer(String input) {
        if (input == null || input.trim().isEmpty()) {
            log.debug("Received null or empty string for formatting");
            return "";
        }

        log.debug("Formatting string with length: {}", input.length());

        // Step 1: Split based on "\n" to separate question and answer
        String[] parts = input.split("\n", 2);

        if (parts.length < 2) {
            log.debug("No newline found, treating entire input as question");
            return "Question: " + removeSerialNumber(parts[0].trim()) + ", Answer: ";
        }

        String questionPart = parts[0].trim();
        String answerPart = parts[1].trim();

        // Step 2: Remove serial number from question part
        String cleanQuestion = removeSerialNumber(questionPart);

        // Step 3: Format as Question/Answer
        String formatted = String.format("Question: %s, Answer: %s", cleanQuestion, answerPart);

        log.debug("Formatted result: {}", formatted);
        return formatted;
    }

    /**
     * Removes serial number from the beginning of a string.
     * Handles patterns like "1.", "2)", "a.", "i)", etc.
     *
     * @param input the string to clean
     * @return string without serial number
     */
    private String removeSerialNumber(String input) {
        if (input == null || input.trim().isEmpty()) {
            return "";
        }

        String trimmed = input.trim();

        // Remove patterns like "1.", "2)", "a.", "i)", etc.
        // This regex matches: optional whitespace, digits or letters, followed by . or ), followed by optional whitespace
        String cleaned = trimmed.replaceFirst("^\\s*[0-9a-zA-Z]+[.):]\\s*", "");

        return cleaned.trim();
    }

    /**
     * Formats multiple strings using the formatQuestionAnswer method.
     *
     * @param inputs list of strings to format
     * @return list of formatted strings
     */
    public List<String> formatQuestionAnswers(List<String> inputs) {
        if (inputs == null || inputs.isEmpty()) {
            log.debug("Received null or empty list for formatting");
            return new ArrayList<>();
        }

        log.info("Formatting {} strings as question-answer pairs", inputs.size());

        return inputs.stream()
                .map(this::formatQuestionAnswer)
                .filter(formatted -> !formatted.isEmpty()) // Remove empty results
                .collect(Collectors.toList());
    }
}
