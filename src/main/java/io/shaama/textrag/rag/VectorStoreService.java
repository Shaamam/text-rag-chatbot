package io.shaama.textrag.rag;

import io.shaama.textrag.rag.model.ProcessedDocument;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class VectorStoreService {

    private final VectorStore vectorStore;

    public String addToVectorStore(ProcessedDocument processedDoc) {
        List<Document> documents = processedDoc.getChunks()
                .parallelStream()
                .map(chunk -> {
                    // Create metadata for each chunk including document info
                    Map<String, Object> chunkMetadata = new HashMap<>(processedDoc.getMetadata());
                    chunkMetadata.put("chunkIndex", processedDoc.getChunks().indexOf(chunk));
                    chunkMetadata.put("totalChunks", processedDoc.getTotalChunks());
                    
                    return new Document(chunk, chunkMetadata);
                })
                .toList();

        vectorStore.add(documents);

        log.info("Added {} chunks from document '{}' to vector store", 
                documents.size(), processedDoc.getFileName());

        return String.format("Successfully processed and added %d chunks from document '%s'", 
                documents.size(), processedDoc.getFileName());
    }

    // Keep old method for backward compatibility
    public String addToVectorStore(List<String> contents) {
        List<Document> documents = contents
                .parallelStream()
                .map(Document::new)
                .toList();

        vectorStore.add(documents);

        return "Data Added";
    }
}
