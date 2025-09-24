import axios from 'axios';

// Define API client for chat
export const chatApi = axios.create({
    baseURL: 'http://localhost:8080/api/v1/chat',
    headers: {
        'Content-Type': 'application/json',
    }
});

// Define API client for document upload
export const documentApi = axios.create({
    baseURL: 'http://localhost:8080/api/v1/rag/documents',
    headers: {
        'Content-Type': 'multipart/form-data',
    }
});