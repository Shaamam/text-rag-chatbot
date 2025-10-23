package io.shaama.textrag.rag;

import io.shaama.textrag.rag.model.ProcessedDocument;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/v1/rag/documents")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Document Processing", description = "APIs for processing documents for RAG operations")
public class DocumentController {

    private final DocumentProcessingService documentProcessingService;
    private final VectorStoreService vectorStoreService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "Process documents for RAG",
            description = "Uploads and processes documents (PDF .pdf, Word .docx, Text .txt, CSV .csv, Excel .xlsx/.xls) with appropriate chunking strategies"
    )
    public ResponseEntity<List<ProcessedDocument>> uploadDocuments(
            @Parameter(description = "Documents to process (PDF, Word, Text, CSV, Excel)", required = true)
            @RequestParam("docs") List<MultipartFile> docs
    ) {
        log.info("Processing {} documents", docs.size());

        try {
            List<ProcessedDocument> processedDocuments = new ArrayList<>();

            for (MultipartFile doc : docs) {
                if (!doc.isEmpty()) {
                    ProcessedDocument processedDoc = documentProcessingService.processDocument(doc);
                    String result = vectorStoreService.addToVectorStore(processedDoc);
                    
                    log.info("Processed document: {} - {}", doc.getOriginalFilename(), result);
                    processedDocuments.add(processedDoc);
                }
            }

            log.info("Successfully processed {} documents", processedDocuments.size());
            return ResponseEntity.ok(processedDocuments);
            
        } catch (Exception e) {
            log.error("Error processing documents: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping(value = "/upload-single", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "Process single document for RAG",
            description = "Uploads and processes a single document (PDF .pdf, Word .docx, Text .txt, CSV .csv, Excel .xlsx/.xls)"
    )
    public ResponseEntity<ProcessedDocument> uploadSingleDocument(
            @Parameter(description = "Document to process", required = true)
            @RequestParam("doc") MultipartFile doc
    ) {
        log.info("Processing single document: {}", doc.getOriginalFilename());

        try {
            ProcessedDocument processedDoc = documentProcessingService.processDocument(doc);
            String result = vectorStoreService.addToVectorStore(processedDoc);
            
            log.info("Successfully processed document: {} - {}", doc.getOriginalFilename(), result);
            return ResponseEntity.ok(processedDoc);
            
        } catch (Exception e) {
            log.error("Error processing document: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
