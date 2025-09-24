# TextRAG - Retrieval-Augmented Generation with Document Processing

A full-stack application that enables users to upload documents and ask questions about their content using Retrieval-Augmented Generation (RAG) with AI-powered chat functionality.

![TextRAG Architecture](https://img.shields.io/badge/Spring%20Boot-3.5.6-brightgreen) ![React](https://img.shields.io/badge/React-19.1.0-blue) ![TypeScript](https://img.shields.io/badge/TypeScript-4.9.5-blue) ![Tailwind CSS](https://img.shields.io/badge/Tailwind%20CSS-3.4.1-38B2AC)

## 🚀 Features

### Document Processing
- **Multi-format Support**: PDF (.pdf), Word documents (.docx), and text files (.txt)
- **Intelligent Text Extraction**: Uses Apache PDFBox for PDFs and Apache POI for Word documents
- **Smart Chunking**: Automatically splits documents on double newlines for optimal RAG performance
- **Vector Storage**: Stores processed documents in PostgreSQL with pgvector extension

### AI-Powered Chat
- **Conversational RAG**: Ask questions about uploaded documents with context awareness
- **Session Management**: Maintains conversation history per browser session
- **Real-time Responses**: Powered by Google Vertex AI Gemini models
- **Semantic Search**: Uses text-embedding-005 model for accurate document retrieval

### Modern Web Interface
- **Drag & Drop Upload**: Intuitive file upload with visual feedback
- **Responsive Design**: Works seamlessly on desktop and mobile devices
- **Real-time Chat**: Instant messaging interface with typing indicators
- **Document Management**: Upload multiple documents and query across all content

## 🏗️ Architecture

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   React UI      │    │  Spring Boot    │    │   PostgreSQL    │
│   (Frontend)    │◄──►│   (Backend)     │◄──►│   + pgvector    │
│                 │    │                 │    │                 │
│ • File Upload   │    │ • RAG API       │    │ • Vector Store  │
│ • Chat Interface│    │ • Doc Processing│    │ • Chat History  │
│ • Session Mgmt  │    │ • AI Integration│    │                 │
└─────────────────┘    └─────────────────┘    └─────────────────┘
                                │
                                ▼
                       ┌─────────────────┐
                       │  Vertex AI      │
                       │                 │
                       │ • Gemini Chat   │
                       │ • Text Embedding│
                       └─────────────────┘
```

## 🛠️ Technology Stack

### Backend
- **Framework**: Spring Boot 3.5.6
- **Language**: Java 21
- **Database**: PostgreSQL with pgvector extension
- **AI Integration**: Google Vertex AI (Gemini + Embeddings)
- **Document Processing**: Apache PDFBox, Apache POI
- **API Documentation**: OpenAPI 3.0 (Swagger)
- **Build Tool**: Gradle

### Frontend
- **Framework**: React 19.1.0 with TypeScript
- **Styling**: Tailwind CSS 3.4.1
- **HTTP Client**: Axios
- **Build Tool**: React Scripts (Create React App)

## 📋 Prerequisites

### System Requirements
- Java 21 or higher
- Node.js 16+ and npm
- PostgreSQL 12+ with pgvector extension
- Google Cloud Platform account (for Vertex AI)

### Database Setup
```sql
-- Install pgvector extension
CREATE EXTENSION IF NOT EXISTS vector;

-- Create database
CREATE DATABASE work;
```

### Google Cloud Setup
1. Create a Google Cloud Project
2. Enable Vertex AI API
3. Set up authentication (Application Default Credentials)
4. Configure project settings in `application.properties`

## 🚀 Quick Start

### 1. Clone the Repository
```bash
git clone <repository-url>
cd textrag
```

### 2. Configure Backend
Update `src/main/resources/application.properties`:
```properties
# Database Configuration
spring.datasource.url=jdbc:postgresql://localhost:5432/work
spring.datasource.username=your_username
spring.datasource.password=your_password

# Google Cloud Configuration
spring.ai.vertex.ai.gemini.project-id=your-gcp-project
spring.ai.vertex.ai.gemini.location=us-central1
spring.ai.vertex.ai.embedding.project-id=your-gcp-project
spring.ai.vertex.ai.embedding.location=us-central1
```

### 3. Start the Application

#### Development Mode (Backend + Frontend separately)
```bash
# Terminal 1: Start Backend
./gradlew bootRun

# Terminal 2: Start Frontend
cd frontend
npm install
npm start
```

#### Production Mode (Integrated build)
```bash
# Build everything together
./gradlew build

# Run the application
java -jar build/libs/textrag-0.0.1-SNAPSHOT.jar
```

### 4. Access the Application
- **Frontend**: http://localhost:3000 (development) or http://localhost:8080 (production)
- **API Documentation**: http://localhost:8080/swagger-ui/index.html
- **Backend API**: http://localhost:8080/api/v1

## 📖 Usage Guide

### 1. Upload Documents
1. Open the application in your browser
2. Click the upload button in the header or drag files to the upload area
3. Select or drop supported files (PDF, DOCX, TXT)
4. Wait for processing completion confirmation

### 2. Ask Questions
1. Type your question in the chat input field
2. Press Enter or click the send button
3. The AI will analyze your documents and provide relevant answers
4. Continue the conversation with follow-up questions

### 3. Manage Sessions
- Each browser session gets a unique ID
- Conversation history is maintained within sessions
- Session ID is displayed in the chat footer

## 🔧 API Endpoints

### Document Management
```http
# Upload and process documents
POST /api/v1/rag/documents/upload
Content-Type: multipart/form-data

# Process documents without storing
POST /api/v1/rag/documents/process
Content-Type: multipart/form-data
```

### Chat Interface
```http
# Send chat messages
POST /api/v1/chat
Content-Type: application/json

{
  "sessionId": "session-123",
  "Question": "What is the main topic of the document?"
}
```

### Example API Usage
```bash
# Upload a document
curl -X POST http://localhost:8080/api/v1/rag/documents/upload \
  -F "doc=@./path/to/document.pdf"

# Ask a question
curl -X POST http://localhost:8080/api/v1/chat \
  -H "Content-Type: application/json" \
  -d '{
    "sessionId": "my-session",
    "Question": "Summarize the key points from the document"
  }'
```

## 🧪 Testing

### Backend Tests
```bash
./gradlew test
```

### Frontend Tests
```bash
cd frontend
npm test
```

### API Testing
Use the provided HTTP requests in `request/api-requests.http` with:
- VS Code REST Client extension
- IntelliJ IDEA HTTP Client
- Postman or similar tools

## 📁 Project Structure

```
textrag/
├── src/main/java/io/shaama/textrag/          # Backend source code
│   ├── chat/                                 # Chat API controllers & services
│   ├── rag/                                  # Document processing & RAG
│   └── TextragApplication.java               # Main application class
├── src/main/resources/
│   ├── application.properties                # Backend configuration
│   └── pdf/                                  # Sample PDF documents
├── frontend/                                 # React frontend application
│   ├── src/
│   │   ├── components/chat/                  # Chat UI components
│   │   ├── axiosClient.ts                    # API client configuration
│   │   └── App.tsx                           # Main React component
│   └── public/                               # Static assets
├── request/                                  # API testing files
│   ├── api-requests.http                     # HTTP request examples
│   └── sample.txt                            # Sample test document
└── build.gradle                              # Build configuration
```

## 🔧 Configuration

### Environment Variables
```bash
# Google Cloud (optional, if not using ADC)
export GOOGLE_APPLICATION_CREDENTIALS=/path/to/service-account.json

# Database (optional, overrides application.properties)
export DB_URL=jdbc:postgresql://localhost:5432/work
export DB_USERNAME=your_username
export DB_PASSWORD=your_password
```

### Customization Options

#### Model Configuration
```properties
# Change AI models in application.properties
spring.ai.vertex.ai.gemini.chat.options.model=gemini-2.5-pro
spring.ai.vertex.ai.embedding.text.options.model=text-embedding-005
```

#### Vector Store Settings
```properties
# Adjust vector store configuration
spring.ai.vectorstore.pgvector.dimensions=1024
spring.ai.vectorstore.pgvector.table-name=your_table_name
```

## 🚨 Troubleshooting

### Common Issues

#### 1. Database Connection Failed
```bash
# Check PostgreSQL is running
brew services start postgresql
# or
sudo systemctl start postgresql

# Verify pgvector extension
psql -d work -c "CREATE EXTENSION IF NOT EXISTS vector;"
```

#### 2. Google Cloud Authentication
```bash
# Login to Google Cloud
gcloud auth application-default login

# Set project
gcloud config set project your-gcp-project-id
```

#### 3. Frontend Build Issues
```bash
# Clear npm cache and reinstall
cd frontend
rm -rf node_modules package-lock.json
npm install
```

#### 4. PDF Processing Errors
- Ensure PDFBox dependencies are included in classpath
- Check if PDF files are not password-protected
- Verify file size limits (default: 10MB)

### Debugging Tips

#### Enable Detailed Logging
```properties
# Add to application.properties for debug logs
logging.level.io.shaama.textrag=DEBUG
logging.level.org.springframework.ai=DEBUG
```

#### Check Application Health
```bash
# View application logs
tail -f app.log

# Check process status
ps aux | grep java | grep textrag
```

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

### Development Guidelines
- Follow Java and TypeScript coding standards
- Write tests for new features
- Update documentation for API changes
- Ensure responsive design for UI changes

## 📝 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🙏 Acknowledgments

- [Spring AI](https://spring.io/projects/spring-ai) for RAG capabilities
- [Apache PDFBox](https://pdfbox.apache.org/) for PDF processing
- [pgvector](https://github.com/pgvector/pgvector) for vector storage
- [Vertex AI](https://cloud.google.com/vertex-ai) for AI models
- [Tailwind CSS](https://tailwindcss.com/) for styling

## 📞 Support

For questions, issues, or contributions:
- Create an issue on GitHub
- Check the [API documentation](http://localhost:8080/swagger-ui/index.html)
- Review the troubleshooting section above

---

**Made with ❤️ using Spring Boot, React, and AI**
