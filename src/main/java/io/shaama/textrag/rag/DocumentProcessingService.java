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

    public List<String> processDocument(MultipartFile document) throws IOException {
        if (document.isEmpty()) {
            log.warn("Received empty document");
            return new ArrayList<>();
        }

        String content = extractContent(document);
        log.debug("Read document content with length: {}", content.length());

        // Split content on double newlines ("\n\n")
        List<String> chunks = Arrays.stream(content.split("\n\n"))
                .map(String::trim)
                .filter(chunk -> !chunk.isEmpty())
                .collect(Collectors.toList());

        log.info("Processed document '{}' into {} non-empty chunks", document.getOriginalFilename(), chunks.size());

        return chunks;
    }

    private String extractContent(MultipartFile document) throws IOException {
        String filename = document.getOriginalFilename();
        String contentType = document.getContentType();

        log.debug("Processing file: {} with content type: {}", filename, contentType);

        if (isPdfDocument(filename, contentType)) {
            return extractFromPdfDocument(document);
        }
        else if (isWordDocument(filename, contentType)) {
            return extractFromWordDocument(document);
        } else {
            return new String(document.getBytes(), StandardCharsets.UTF_8);
        }
    }

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

    private String extractFromPdfDocument(MultipartFile document) throws IOException {
        log.debug("Extracting text from PDF document: {}", document.getOriginalFilename());

        try (PDDocument pdfDocument = PDDocument.load(document.getInputStream())) {
            PDFTextStripper textStripper = new PDFTextStripper();
            
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
}
