package io.shaama.textrag.rag;

import io.shaama.textrag.rag.model.ProcessedDocument;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class DocumentProcessingService {

    public ProcessedDocument processDocument(MultipartFile document) throws IOException {
        if (document.isEmpty()) {
            log.warn("Received empty document");
            return ProcessedDocument.builder()
                    .fileName(document.getOriginalFilename())
                    .fileType("empty")
                    .chunks(new ArrayList<>())
                    .metadata(new HashMap<>())
                    .totalChunks(0)
                    .build();
        }

        String filename = document.getOriginalFilename();
        String fileType = getFileType(filename);
        
        log.info("Processing document: {} of type: {}", filename, fileType);

        switch (fileType) {
            case "pdf":
                return processPdfDocument(document);
            case "docx":
                return processWordDocument(document);
            case "txt":
                return processTextDocument(document);
            case "csv":
                return processCsvDocument(document);
            case "xlsx":
            case "xls":
                return processExcelDocument(document);
            default:
                throw new IOException("Unsupported file type: " + fileType);
        }
    }

    private String getFileType(String filename) {
        if (filename == null) return "unknown";
        
        String lowerFilename = filename.toLowerCase();
        if (lowerFilename.endsWith(".pdf")) return "pdf";
        if (lowerFilename.endsWith(".docx")) return "docx";
        if (lowerFilename.endsWith(".txt")) return "txt";
        if (lowerFilename.endsWith(".csv")) return "csv";
        if (lowerFilename.endsWith(".xlsx")) return "xlsx";
        if (lowerFilename.endsWith(".xls")) return "xls";
        
        return "unknown";
    }

    private ProcessedDocument processPdfDocument(MultipartFile document) throws IOException {
        log.debug("Extracting text from PDF document: {}", document.getOriginalFilename());

        try (PDDocument pdfDocument = PDDocument.load(document.getInputStream())) {
            PDFTextStripper textStripper = new PDFTextStripper();
            
            textStripper.setSortByPosition(true);
            textStripper.setLineSeparator("\n");
            textStripper.setWordSeparator(" ");
            textStripper.setParagraphStart("");
            textStripper.setParagraphEnd("\n\n");
            
            String extractedContent = textStripper.getText(pdfDocument);
            
            // Split content on double newlines for paragraphs
            List<String> chunks = Arrays.stream(extractedContent.split("\n\n"))
                    .map(String::trim)
                    .filter(chunk -> !chunk.isEmpty())
                    .collect(Collectors.toList());

            Map<String, Object> metadata = new HashMap<>();
            metadata.put("documentName", document.getOriginalFilename());
            metadata.put("fileType", "pdf");
            metadata.put("pageCount", pdfDocument.getNumberOfPages());
            metadata.put("totalCharacters", extractedContent.length());

            log.info("Processed PDF document '{}' into {} chunks", document.getOriginalFilename(), chunks.size());

            return ProcessedDocument.builder()
                    .fileName(document.getOriginalFilename())
                    .fileType("pdf")
                    .chunks(chunks)
                    .metadata(metadata)
                    .totalChunks(chunks.size())
                    .build();

        } catch (Exception e) {
            log.error("Failed to extract text from PDF document: {}", e.getMessage(), e);
            throw new IOException("Failed to process PDF document: " + e.getMessage(), e);
        }
    }

    private ProcessedDocument processWordDocument(MultipartFile document) throws IOException {
        log.debug("Extracting text from Word document: {}", document.getOriginalFilename());

        try (XWPFDocument wordDocument = new XWPFDocument(document.getInputStream())) {
            List<XWPFParagraph> paragraphs = wordDocument.getParagraphs();
            List<String> chunks = new ArrayList<>();

            for (XWPFParagraph paragraph : paragraphs) {
                String text = paragraph.getText();
                if (text != null && !text.trim().isEmpty()) {
                    chunks.add(text.trim());
                }
            }

            Map<String, Object> metadata = new HashMap<>();
            metadata.put("documentName", document.getOriginalFilename());
            metadata.put("fileType", "docx");
            metadata.put("paragraphCount", chunks.size());

            log.info("Processed Word document '{}' into {} paragraph chunks", document.getOriginalFilename(), chunks.size());

            return ProcessedDocument.builder()
                    .fileName(document.getOriginalFilename())
                    .fileType("docx")
                    .chunks(chunks)
                    .metadata(metadata)
                    .totalChunks(chunks.size())
                    .build();

        } catch (Exception e) {
            log.error("Failed to extract text from Word document: {}", e.getMessage(), e);
            throw new IOException("Failed to process Word document: " + e.getMessage(), e);
        }
    }

    private ProcessedDocument processTextDocument(MultipartFile document) throws IOException {
        log.debug("Processing text document: {}", document.getOriginalFilename());

        String content = new String(document.getBytes(), StandardCharsets.UTF_8);
        
        // Split text by paragraphs (double newlines or single newlines)
        List<String> chunks = Arrays.stream(content.split("\n\n|\n"))
                .map(String::trim)
                .filter(chunk -> !chunk.isEmpty())
                .collect(Collectors.toList());

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("documentName", document.getOriginalFilename());
        metadata.put("fileType", "txt");
        metadata.put("totalCharacters", content.length());

        log.info("Processed text document '{}' into {} paragraph chunks", document.getOriginalFilename(), chunks.size());

        return ProcessedDocument.builder()
                .fileName(document.getOriginalFilename())
                .fileType("txt")
                .chunks(chunks)
                .metadata(metadata)
                .totalChunks(chunks.size())
                .build();
    }

    private ProcessedDocument processCsvDocument(MultipartFile document) throws IOException {
        log.debug("Processing CSV document: {}", document.getOriginalFilename());

        try (CSVReader csvReader = new CSVReader(new InputStreamReader(document.getInputStream(), StandardCharsets.UTF_8))) {
            List<String[]> records = csvReader.readAll();
            
            if (records.isEmpty()) {
                throw new IOException("CSV file is empty");
            }

            // First row contains headers
            String[] headers = records.get(0);
            List<String> chunks = new ArrayList<>();

            // Process each data row as a chunk
            for (int i = 1; i < records.size(); i++) {
                String[] row = records.get(i);
                StringBuilder rowContent = new StringBuilder();
                
                for (int j = 0; j < Math.min(headers.length, row.length); j++) {
                    if (j > 0) rowContent.append(", ");
                    rowContent.append(headers[j]).append(": ").append(row[j]);
                }
                
                if (rowContent.length() > 0) {
                    chunks.add(rowContent.toString());
                }
            }

            Map<String, Object> metadata = new HashMap<>();
            metadata.put("documentName", document.getOriginalFilename());
            metadata.put("fileType", "csv");
            metadata.put("headers", String.join(", ", headers));
            metadata.put("columnCount", headers.length);
            metadata.put("rowCount", records.size() - 1); // Excluding header row

            log.info("Processed CSV document '{}' into {} row chunks with headers: {}", 
                    document.getOriginalFilename(), chunks.size(), String.join(", ", headers));

            return ProcessedDocument.builder()
                    .fileName(document.getOriginalFilename())
                    .fileType("csv")
                    .chunks(chunks)
                    .metadata(metadata)
                    .totalChunks(chunks.size())
                    .build();

        } catch (CsvException e) {
            log.error("Failed to parse CSV document: {}", e.getMessage(), e);
            throw new IOException("Failed to process CSV document: " + e.getMessage(), e);
        }
    }

    private ProcessedDocument processExcelDocument(MultipartFile document) throws IOException {
        log.debug("Processing Excel document: {}", document.getOriginalFilename());

        try {
            Workbook workbook;
            String filename = document.getOriginalFilename();
            
            if (filename != null && filename.toLowerCase().endsWith(".xlsx")) {
                workbook = new XSSFWorkbook(document.getInputStream());
            } else {
                workbook = new HSSFWorkbook(document.getInputStream());
            }

            List<String> chunks = new ArrayList<>();
            List<String> headers = new ArrayList<>();
            
            // Process the first sheet
            Sheet sheet = workbook.getSheetAt(0);
            Row headerRow = sheet.getRow(0);
            
            // Extract headers
            if (headerRow != null) {
                for (Cell cell : headerRow) {
                    headers.add(getCellValueAsString(cell));
                }
            }

            // Process data rows
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row != null) {
                    StringBuilder rowContent = new StringBuilder();
                    
                    for (int j = 0; j < headers.size(); j++) {
                        Cell cell = row.getCell(j);
                        String cellValue = getCellValueAsString(cell);
                        
                        if (j > 0) rowContent.append(", ");
                        rowContent.append(headers.get(j)).append(": ").append(cellValue);
                    }
                    
                    if (rowContent.length() > 0) {
                        chunks.add(rowContent.toString());
                    }
                }
            }

            Map<String, Object> metadata = new HashMap<>();
            metadata.put("documentName", document.getOriginalFilename());
            metadata.put("fileType", filename != null && filename.toLowerCase().endsWith(".xlsx") ? "xlsx" : "xls");
            metadata.put("headers", String.join(", ", headers));
            metadata.put("columnCount", headers.size());
            metadata.put("rowCount", chunks.size());
            metadata.put("sheetName", sheet.getSheetName());

            workbook.close();

            log.info("Processed Excel document '{}' into {} row chunks with headers: {}", 
                    document.getOriginalFilename(), chunks.size(), String.join(", ", headers));

            return ProcessedDocument.builder()
                    .fileName(document.getOriginalFilename())
                    .fileType(filename != null && filename.toLowerCase().endsWith(".xlsx") ? "xlsx" : "xls")
                    .chunks(chunks)
                    .metadata(metadata)
                    .totalChunks(chunks.size())
                    .build();

        } catch (Exception e) {
            log.error("Failed to process Excel document: {}", e.getMessage(), e);
            throw new IOException("Failed to process Excel document: " + e.getMessage(), e);
        }
    }

    private String getCellValueAsString(Cell cell) {
        if (cell == null) return "";
        
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    return String.valueOf(cell.getNumericCellValue());
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return "";
        }
    }
}
