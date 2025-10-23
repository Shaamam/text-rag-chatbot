package io.shaama.textrag.rag.model;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProcessedDocument {
    private String fileName;
    private String fileType;
    private List<String> chunks;
    private Map<String, Object> metadata;
    private int totalChunks;

    // Keep the old constructor for backward compatibility
    public ProcessedDocument(String content, String documentName, String documentType) {
        this.fileName = documentName;
        this.fileType = documentType;
        this.chunks = List.of(content);
        this.metadata = new HashMap<>();
        this.totalChunks = 1;
    }

    public void addMetadata(String key, Object value) {
        if (this.metadata == null) {
            this.metadata = new HashMap<>();
        }
        this.metadata.put(key, value);
    }
}
