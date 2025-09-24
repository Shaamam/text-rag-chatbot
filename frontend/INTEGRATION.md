# TextRAG Frontend Integration

This document describes the frontend changes made to integrate document upload functionality with the TextRAG backend.

## Changes Made

### 1. Updated API Client (`axiosClient.ts`)
- Added separate API clients for chat and document operations
- Updated base URLs to match TextRAG endpoints:
  - Chat: `http://localhost:8080/api/v1/chat`
  - Documents: `http://localhost:8080/api/v1/rag/documents`

### 2. Created Document Upload Component (`DocumentUpload.tsx`)
- **Features:**
  - Drag & drop file upload
  - Click to select files
  - Support for PDF (.pdf), Word (.docx), and Text (.txt) files
  - Real-time upload progress indicator
  - Error handling and success feedback
  - File size validation (10MB limit)
  - Visual feedback for drag states

- **Props:**
  - `onUploadSuccess(message: string)`: Called when upload succeeds
  - `onUploadError(error: string)`: Called when upload fails

### 3. Enhanced Chat Window (`ChatWindow.tsx`)
- **New Features:**
  - Integrated document upload area (toggleable)
  - Upload button in header to show/hide upload area
  - Session management with unique session IDs
  - Updated API calls to match TextRAG format
  - Auto-hide upload area after first successful upload

- **Updated UI:**
  - Changed title from "Blue Agent" to "TextRAG Assistant"
  - Updated subtitle to "Document Q&A with RAG"
  - Added upload toggle button in header
  - Updated footer text with session information
  - Improved welcome message

### 4. API Integration
- **Chat API Format:**
  ```typescript
  {
    sessionId: string,
    Question: string
  }
  ```

- **Document Upload API:**
  - Endpoint: `POST /api/v1/rag/documents/upload`
  - Content-Type: `multipart/form-data`
  - Field name: `doc`

## How to Use

### 1. Start Backend Server
```bash
./gradlew bootRun
```

### 2. Start Frontend (Development)
```bash
cd frontend
npm install
npm start
```

### 3. Production Build
The build.gradle is configured to automatically build and copy the frontend:
```bash
./gradlew build
```

## User Workflow

1. **Upload Documents**: 
   - Click upload button in header or drag files to upload area
   - Supported formats: PDF, Word, Text files
   - Files are processed and stored in vector database

2. **Ask Questions**: 
   - Type questions about uploaded documents
   - System uses RAG to find relevant content
   - Maintains conversation context within session

3. **Session Management**: 
   - Each browser session gets unique ID
   - Conversation history maintained per session
   - Session ID displayed in footer

## Technical Notes

- **File Processing**: Documents are chunked on double newlines (`\n\n`)
- **Vector Storage**: Uses PostgreSQL with pgvector extension
- **AI Model**: Vertex AI Gemini for chat completions
- **Embeddings**: Vertex AI text-embedding-005 model

## Error Handling

- Upload failures show error messages in chat
- Network errors are gracefully handled
- File type validation prevents unsupported formats
- File size limits prevent oversized uploads

## Future Enhancements

- [ ] Multiple file upload support
- [ ] Document management (list, delete uploaded docs)
- [ ] File preview before upload
- [ ] Progress bars for large file uploads
- [ ] Support for additional file formats
- [ ] Document metadata display
- [ ] Search history and bookmarking
