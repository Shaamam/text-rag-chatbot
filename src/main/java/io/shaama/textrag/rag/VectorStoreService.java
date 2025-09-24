package io.shaama.textrag.rag;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class VectorStoreService {

    private final VectorStore vectorStore;

    public String addToVectorStore(List<String> contents){

        List<Document> documents = contents
                .parallelStream()
                .map(Document::new)
                .toList();

        vectorStore.add(documents);

        return "Data Added";
    }

}
