package io.shaama.textrag.rag;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

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
            summary = "Process document for RAG",
            description = "Uploads and processes a PDF (.pdf), Word document (.docx), or text file by splitting content on double newlines and removing empty fields"
    )
    public ResponseEntity<String> uploadDocument(
            @Parameter(description = "PDF (.pdf), Word document (.docx), or text file to process", required = true)
            @RequestParam("doc") MultipartFile doc
    ) {
        log.info("Processing document: {}", doc.getOriginalFilename());

        try {
            List<String> processedChunks = documentProcessingService.processDocument(doc);
            log.info("Successfully processed document into {} chunks", processedChunks.size());
            String responseMessage = vectorStoreService.addToVectorStore(processedChunks);

            return ResponseEntity.ok(responseMessage);
        } catch (Exception e) {
            log.error("Error processing document: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
