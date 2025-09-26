package io.shaama.textrag.integration;

import io.shaama.textrag.chat.ChatRequest;
import io.shaama.textrag.chat.ChatResponse;
import io.shaama.textrag.chat.ChatService;
import io.shaama.textrag.rag.DocumentProcessingService;
import io.shaama.textrag.rag.VectorStoreService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for vector store operations including document upload,
 * embedding generation, and similarity search with detailed logging.
 */
@SpringBootTest
public class VectorStoreIntegrationTest {

    @Autowired
    private DocumentProcessingService documentProcessingService;

    @Autowired
    private VectorStoreService vectorStoreService;

    @Autowired
    private ChatService chatService;

    @Autowired
    private VectorStore vectorStore;

    @Autowired
    private EmbeddingModel embeddingModel;

    private static final String TEST_DOCUMENT_CONTENT = """
        # Machine Learning Fundamentals
        
        Machine Learning (ML) is a subset of artificial intelligence that enables computers to learn and make decisions from data without being explicitly programmed for every task.
        
        ## Types of Machine Learning
        
        ### Supervised Learning
        Supervised learning uses labeled training data to learn a mapping function from input variables to output variables. Common algorithms include:
        - Linear Regression: Used for predicting continuous values
        - Decision Trees: Used for both classification and regression tasks
        - Random Forest: An ensemble method that combines multiple decision trees
        - Support Vector Machines (SVM): Effective for classification and regression
        
        ### Unsupervised Learning
        Unsupervised learning finds hidden patterns in data without labeled examples. Key techniques include:
        - Clustering: Groups similar data points together (K-means, hierarchical clustering)
        - Dimensionality Reduction: Reduces the number of features while preserving information (PCA, t-SNE)
        - Association Rules: Finds relationships between different variables
        
        ### Reinforcement Learning
        Reinforcement learning learns through interaction with an environment, receiving rewards or penalties for actions taken. It's widely used in:
        - Game playing (AlphaGo, chess engines)
        - Robotics and autonomous systems
        - Recommendation systems
        - Financial trading algorithms
        
        ## Key Concepts
        
        ### Feature Engineering
        The process of selecting, modifying, or creating features from raw data to improve model performance. This includes:
        - Feature selection: Choosing the most relevant features
        - Feature scaling: Normalizing features to similar ranges
        - Feature creation: Generating new features from existing ones
        
        ### Model Evaluation
        Proper evaluation ensures models generalize well to unseen data:
        - Training Set: Used to train the model
        - Validation Set: Used for hyperparameter tuning and model selection
        - Test Set: Used for final model evaluation
        - Cross-validation: Technique to assess model performance more reliably
        
        ### Overfitting and Underfitting
        - Overfitting: Model performs well on training data but poorly on new data
        - Underfitting: Model is too simple to capture underlying patterns
        - Regularization techniques help prevent overfitting (L1, L2 regularization, dropout)
        
        ## Applications
        
        Machine learning has revolutionized many industries:
        - Healthcare: Medical diagnosis, drug discovery, personalized treatment
        - Finance: Fraud detection, algorithmic trading, credit scoring
        - Technology: Search engines, recommendation systems, computer vision
        - Transportation: Autonomous vehicles, route optimization, predictive maintenance
        - Marketing: Customer segmentation, targeted advertising, sentiment analysis
        
        ## Best Practices
        
        1. Start with simple models before moving to complex ones
        2. Always validate your model on unseen data
        3. Pay attention to data quality and preprocessing
        4. Use appropriate metrics for your specific problem
        5. Consider the interpretability vs. performance trade-off
        6. Monitor model performance in production
        7. Keep learning and staying updated with new techniques
        """;

    private static final String TEST_FILENAME = "machine_learning_guide.txt";

    @BeforeEach
    void setUp() {
        System.out.println("Setting up integration test for vector store operations");
    }

    @Test
    void testCompleteDocumentToVectorStoreWorkflow() throws IOException {
        System.out.println("=== Starting Complete Vector Store Integration Test ===");

        // Step 1: Create and process the document
        MultipartFile testFile = createTestDocument();
        System.out.println("Created test document: " + testFile.getOriginalFilename() + " with size: " + testFile.getSize() + " bytes");

        // Step 2: Process the document into chunks
        List<String> documentChunks = documentProcessingService.processDocument(testFile);
        System.out.println("Document processed into " + documentChunks.size() + " chunks");
        
        // Log each chunk for visibility
        for (int i = 0; i < documentChunks.size(); i++) {
            String preview = documentChunks.get(i).substring(0, Math.min(100, documentChunks.get(i).length())) + "...";
            System.out.println("Chunk " + (i + 1) + ": " + documentChunks.get(i).length() + " characters - '" + preview + "'");
        }

        assertThat(documentChunks).isNotEmpty();
        assertThat(documentChunks.size()).isGreaterThan(5); // Expecting multiple sections

        // Step 3: Add documents to vector store and log embeddings
        String addResult = vectorStoreService.addToVectorStore(documentChunks);
        System.out.println("Vector store add result: " + addResult);

        // Step 4: Test various questions and analyze embeddings/search results
        testQuestionsAndAnalyzeResults();

        System.out.println("=== Complete Vector Store Integration Test Completed Successfully ===");
    }

    private void testQuestionsAndAnalyzeResults() {
        String sessionId = "test-session-" + System.currentTimeMillis();
        
        // Test questions covering different aspects of the document
        String[] testQuestions = {
            "What is machine learning?",
            "What are the main types of machine learning?",
            "Explain supervised learning algorithms",
            "What is overfitting and how can it be prevented?",
            "What are some applications of machine learning in healthcare?",
            "What is feature engineering?",
            "How does reinforcement learning work?",
            "What evaluation techniques should be used for machine learning models?"
        };

        System.out.println("=== Testing " + testQuestions.length + " questions with embedding analysis ===");

        for (int i = 0; i < testQuestions.length; i++) {
            String question = testQuestions[i];
            System.out.println("\n--- Question " + (i + 1) + ": '" + question + "' ---");

            // Generate embedding for the question
            analyzeQuestionEmbedding(question);

            // Perform similarity search and log results
            analyzeVectorSearch(question);

            // Get chat response
            ChatRequest chatRequest = new ChatRequest(sessionId, question);
            ChatResponse chatResponse = chatService.getAnswer(chatRequest);

            System.out.println("Chat Response: " + chatResponse.answer());
            assertThat(chatResponse.answer()).isNotBlank();
            assertThat(chatResponse.sessionId()).isEqualTo(sessionId);
            assertThat(chatResponse.question()).isEqualTo(question);

            System.out.println("--- Question " + (i + 1) + " completed ---\n");
        }
    }

    private void analyzeQuestionEmbedding(String question) {
        System.out.println("Generating embedding for question: '" + question + "'");
        
        try {
            EmbeddingResponse embeddingResponse = embeddingModel.embedForResponse(List.of(question));
            
            if (embeddingResponse != null && !embeddingResponse.getResults().isEmpty()) {
                float[] embedding = embeddingResponse.getResults().get(0).getOutput();
                System.out.println("Question embedding generated successfully:");
                System.out.println("  - Dimensions: " + embedding.length);
                System.out.println("  - First 10 values: " + getFirstNValues(embedding, 10));
                System.out.printf("  - Vector magnitude: %.6f%n", calculateMagnitude(embedding));
            } else {
                System.out.println("Failed to generate embedding for question: " + question);
            }
        } catch (Exception e) {
            System.out.println("Error generating embedding for question '" + question + "': " + e.getMessage());
        }
    }

    private void analyzeVectorSearch(String question) {
        System.out.println("Performing vector similarity search for: '" + question + "'");
        
        try {
            // Perform similarity search using the simple string API
            List<Document> searchResults = vectorStore.similaritySearch(question);
            
            System.out.println("Vector search returned " + searchResults.size() + " results:");
            
            for (int i = 0; i < searchResults.size(); i++) {
                Document doc = searchResults.get(i);
                System.out.println("  Result " + (i + 1) + ": Document found with metadata: " + doc.getMetadata());
                
                // Try to get content through toString or other means
                String docString = doc.toString();
                if (docString.length() > 100) {
                    System.out.println("    Document preview: '" + docString.substring(0, 100) + "...'");
                } else {
                    System.out.println("    Document content: '" + docString + "'");
                }
            }
            
            assertThat(searchResults).isNotEmpty();
            
        } catch (Exception e) {
            System.out.println("Error performing vector search for question '" + question + "': " + e.getMessage());
        }
    }

    private MultipartFile createTestDocument() {
        return new MockMultipartFile(
                "doc",
                TEST_FILENAME,
                "text/plain",
                TEST_DOCUMENT_CONTENT.getBytes()
        );
    }

    private String getFirstNValues(float[] array, int n) {
        StringBuilder sb = new StringBuilder("[");
        int limit = Math.min(n, array.length);
        for (int i = 0; i < limit; i++) {
            sb.append(String.format("%.4f", array[i]));
            if (i < limit - 1) sb.append(", ");
        }
        sb.append("]");
        return sb.toString();
    }

    private double calculateMagnitude(float[] vector) {
        double sumOfSquares = 0.0;
        for (float value : vector) {
            sumOfSquares += value * value;
        }
        return Math.sqrt(sumOfSquares);
    }

    @Test
    void testEmbeddingConsistency() {
        System.out.println("=== Testing Embedding Consistency ===");
        
        String testText = "Machine learning is a subset of artificial intelligence";
        
        // Generate embedding multiple times
        for (int i = 1; i <= 3; i++) {
            System.out.println("Generating embedding #" + i + " for: '" + testText + "'");
            
            EmbeddingResponse response = embeddingModel.embedForResponse(List.of(testText));
            
            if (response != null && !response.getResults().isEmpty()) {
                float[] embedding = response.getResults().get(0).getOutput();
                System.out.printf("  Embedding #%d: %d dimensions, magnitude: %.6f, first 5 values: %s%n", 
                        i, embedding.length, calculateMagnitude(embedding), 
                        getFirstNValues(embedding, 5));
            }
        }
        
        System.out.println("=== Embedding Consistency Test Completed ===");
    }

    @Test
    void testVectorStoreDirectOperations() {
        System.out.println("=== Testing Direct Vector Store Operations ===");
        
        // Create test documents with known content
        List<Document> testDocs = List.of(
            new Document("Artificial Intelligence is the simulation of human intelligence in machines"),
            new Document("Machine Learning is a subset of AI that enables computers to learn from data"),
            new Document("Deep Learning uses neural networks with multiple layers to model complex patterns")
        );
        
        System.out.println("Adding " + testDocs.size() + " test documents to vector store");
        vectorStore.add(testDocs);
        
        // Test similarity search
        String searchQuery = "What is artificial intelligence?";
        System.out.println("Searching for: '" + searchQuery + "'");
        
        List<Document> results = vectorStore.similaritySearch(searchQuery);
        
        System.out.println("Found " + results.size() + " similar documents:");
        for (int i = 0; i < results.size(); i++) {
            Document doc = results.get(i);
            System.out.println("  Document " + (i + 1) + ": Metadata: " + doc.getMetadata());
            System.out.println("    Document string representation: '" + doc.toString() + "'");
        }
        
        assertThat(results).isNotEmpty();
        
        System.out.println("=== Direct Vector Store Operations Test Completed ===");
    }

    @Test
    void testEmbeddingComparison() {
        System.out.println("=== Testing Embedding Similarity Comparison ===");
        
        String[] relatedTexts = {
            "Machine learning algorithms learn from data",
            "Artificial intelligence systems can process information",
            "Deep learning uses neural networks for pattern recognition",
            "The weather is sunny today" // Unrelated text for comparison
        };
        
        System.out.println("Generating embeddings for " + relatedTexts.length + " different texts");
        
        for (int i = 0; i < relatedTexts.length; i++) {
            String text = relatedTexts[i];
            System.out.println("Text " + (i + 1) + ": '" + text + "'");
            
            EmbeddingResponse response = embeddingModel.embedForResponse(List.of(text));
            
            if (response != null && !response.getResults().isEmpty()) {
                float[] embedding = response.getResults().get(0).getOutput();
                System.out.printf("  Embedding: %d dimensions, magnitude: %.6f%n", 
                        embedding.length, calculateMagnitude(embedding));
                System.out.println("  Sample values: " + getFirstNValues(embedding, 8));
                
                // Calculate similarity with first text (if not the first text)
                if (i > 0) {
                    EmbeddingResponse firstResponse = embeddingModel.embedForResponse(List.of(relatedTexts[0]));
                    if (firstResponse != null && !firstResponse.getResults().isEmpty()) {
                        float[] firstEmbedding = firstResponse.getResults().get(0).getOutput();
                        double similarity = calculateCosineSimilarity(embedding, firstEmbedding);
                        System.out.printf("  Cosine similarity with first text: %.6f%n", similarity);
                    }
                }
            }
            System.out.println("");
        }
        
        System.out.println("=== Embedding Similarity Comparison Test Completed ===");
    }

    private double calculateCosineSimilarity(float[] vectorA, float[] vectorB) {
        if (vectorA.length != vectorB.length) {
            return 0.0;
        }
        
        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;
        
        for (int i = 0; i < vectorA.length; i++) {
            dotProduct += vectorA[i] * vectorB[i];
            normA += vectorA[i] * vectorA[i];
            normB += vectorB[i] * vectorB[i];
        }
        
        return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }
}
