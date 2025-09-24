# TextRAG API Testing

This folder contains HTTP request files for testing the TextRAG API endpoints.

## Files

- `api-requests.http` - Contains all API endpoint requests
- `sample.txt` - Sample text document for testing
- `README.md` - This file

## How to Use

### Using VS Code with REST Client Extension

1. Install the "REST Client" extension in VS Code
2. Open `api-requests.http`
3. Click on "Send Request" above each request block
4. View responses in the VS Code panel

### Using IntelliJ IDEA / WebStorm

1. Open `api-requests.http` in IntelliJ IDEA or WebStorm
2. Click on the green arrow next to each request
3. View responses in the HTTP Client tool window

### Using curl (Command Line)

For document upload:
```bash
curl -X POST http://localhost:8080/api/v1/rag/documents/upload \
  -F "doc=@./request/sample.txt"
```

For chat:
```bash
curl -X POST http://localhost:8080/api/v1/chat \
  -H "Content-Type: application/json" \
  -d '{"sessionId": "test-session", "Question": "What is this document about?"}'
```

## API Endpoints

### Document Processing

1. **Process Document** (`POST /api/v1/rag/documents/process`)
   - Processes document and returns chunks without storing
   - Useful for testing document parsing

2. **Upload Document** (`POST /api/v1/rag/documents/upload`)
   - Processes and stores document in vector database
   - Required before asking questions

### Chat

3. **Chat** (`POST /api/v1/chat`)
   - Ask questions about uploaded documents
   - Uses RAG to find relevant information
   - Supports conversation sessions

## Supported File Types

- **PDF files** (.pdf) - Full text extraction
- **Word documents** (.docx) - Text and paragraph extraction  
- **Text files** (.txt) - Direct text processing

## Testing Workflow

1. Start the server: `./gradlew bootRun`
2. Upload a document using the upload endpoint
3. Ask questions using the chat endpoint
4. Test different file formats and question types

## Swagger UI

Access the interactive API documentation at:
http://localhost:8080/swagger-ui/index.html

## Sample Files

Add your own test files to this folder:
- `sample.pdf` - For PDF testing
- `sample.docx` - For Word document testing
- `sample.txt` - Already provided

## Notes

- Make sure PostgreSQL is running before starting the server
- Update file paths in HTTP requests to match your test files
- Session IDs can be any string - use different ones to test conversation memory
- The system splits documents on double newlines (`\n\n`) for optimal chunking
