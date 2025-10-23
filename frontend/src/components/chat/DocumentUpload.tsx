import React, { useState, useRef } from 'react';
import { documentApi } from '../../axiosClient';

interface ProcessedDocument {
    fileName: string;
    fileType: string;
    chunks: string[];
    metadata: { [key: string]: any };
    totalChunks: number;
}

interface DocumentUploadProps {
    onUploadSuccess: (documents: ProcessedDocument[]) => void;
    onUploadError: (error: string) => void;
}

export const DocumentUpload: React.FC<DocumentUploadProps> = ({ onUploadSuccess, onUploadError }) => {
    const [isUploading, setIsUploading] = useState<boolean>(false);
    const [dragActive, setDragActive] = useState<boolean>(false);
    const [uploadedDocuments, setUploadedDocuments] = useState<ProcessedDocument[]>([]);
    const fileInputRef = useRef<HTMLInputElement>(null);

    const handleFileUpload = async (files: FileList): Promise<void> => {
        setIsUploading(true);

        try {
            const formData = new FormData();
            
            // Add all files to form data
            Array.from(files).forEach(file => {
                formData.append('docs', file);
            });

            const response = await documentApi.post('/upload', formData, {
                headers: {
                    'Content-Type': 'multipart/form-data',
                }
            });

            const processedDocs: ProcessedDocument[] = response.data;
            setUploadedDocuments(prev => [...prev, ...processedDocs]);
            
            const fileNames = Array.from(files).map(f => f.name).join(', ');
            onUploadSuccess(processedDocs);
        } catch (error: any) {
            console.error('Error uploading documents:', error);
            const fileNames = Array.from(files).map(f => f.name).join(', ');
            const errorMessage = error.response?.data?.message || `Failed to upload "${fileNames}". Please try again.`;
            onUploadError(errorMessage);
        } finally {
            setIsUploading(false);
        }
    };

    const handleFileSelect = (e: React.ChangeEvent<HTMLInputElement>): void => {
        const files = e.target.files;
        if (files && files.length > 0) {
            handleFileUpload(files);
        }
        // Clear the input so the same files can be selected again
        if (fileInputRef.current) {
            fileInputRef.current.value = '';
        }
    };

    const handleDragOver = (e: React.DragEvent<HTMLDivElement>): void => {
        e.preventDefault();
        setDragActive(true);
    };

    const handleDragLeave = (e: React.DragEvent<HTMLDivElement>): void => {
        e.preventDefault();
        setDragActive(false);
    };

    const handleDrop = (e: React.DragEvent<HTMLDivElement>): void => {
        e.preventDefault();
        setDragActive(false);

        const files = e.dataTransfer.files;
        if (files && files.length > 0) {
            handleFileUpload(files);
        }
    };

    const triggerFileInput = (): void => {
        fileInputRef.current?.click();
    };

    const getSupportedFormats = (): string => {
        return "PDF (.pdf), Word (.docx), Text (.txt), CSV (.csv), Excel (.xlsx, .xls)";
    };

    const clearUploadedDocuments = (): void => {
        setUploadedDocuments([]);
    };

    const getFileTypeIcon = (fileType: string): string => {
        switch (fileType.toLowerCase()) {
            case 'pdf': return '📄';
            case 'docx': return '📝';
            case 'txt': return '📰';
            case 'csv': return '📊';
            case 'xlsx':
            case 'xls': return '📈';
            default: return '📄';
        }
    };

    return (
        <div className="mb-4">
            {/* Upload Area */}
            <div
                className={`border-2 border-dashed rounded-lg p-6 text-center transition-colors cursor-pointer ${
                    dragActive
                        ? 'border-blue-500 bg-blue-50'
                        : isUploading
                            ? 'border-gray-300 bg-gray-50'
                            : 'border-gray-300 hover:border-blue-400 hover:bg-blue-50'
                }`}
                onDragOver={handleDragOver}
                onDragLeave={handleDragLeave}
                onDrop={handleDrop}
                onClick={!isUploading ? triggerFileInput : undefined}
            >
                <input
                    ref={fileInputRef}
                    type="file"
                    accept=".pdf,.docx,.txt,.csv,.xlsx,.xls"
                    onChange={handleFileSelect}
                    className="hidden"
                    disabled={isUploading}
                    multiple
                />
                {isUploading ? (
                    <div className="flex flex-col items-center">
                        <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600 mb-2"></div>
                        <p className="text-sm text-gray-600">Uploading and processing documents...</p>
                    </div>
                ) : (
                    <div className="flex flex-col items-center">
                        <svg
                            className="w-10 h-10 text-gray-400 mb-2"
                            fill="none"
                            stroke="currentColor"
                            viewBox="0 0 24 24"
                        >
                            <path
                                strokeLinecap="round"
                                strokeLinejoin="round"
                                strokeWidth={2}
                                d="M7 16a4 4 0 01-.88-7.903A5 5 0 1115.9 6L16 6a5 5 0 011 9.9M15 13l-3-3m0 0l-3 3m3-3v12"
                            />
                        </svg>
                        <p className="text-sm font-medium text-gray-700 mb-1">
                            Click to upload or drag and drop multiple files
                        </p>
                        <p className="text-xs text-gray-500">
                            Supported: {getSupportedFormats()}
                        </p>
                        <p className="text-xs text-gray-400 mt-1">
                            Maximum file size: 10MB per file
                        </p>
                    </div>
                )}
            </div>

            {/* Upload Instructions */}
            <div className="mt-2 text-xs text-gray-500 text-center">
                Upload documents to ask questions about their content. The system will process and store them for retrieval.
                <br />
                <strong>Chunking Strategy:</strong> TXT/PDF/Word: paragraphs | CSV/Excel: rows with headers
            </div>

            {/* Uploaded Documents List */}
            {uploadedDocuments.length > 0 && (
                <div className="mt-4 border rounded-lg p-4 bg-gray-50">
                    <div className="flex justify-between items-center mb-3">
                        <h3 className="text-sm font-medium text-gray-700">
                            Uploaded Documents ({uploadedDocuments.length})
                        </h3>
                        <button
                            onClick={clearUploadedDocuments}
                            className="text-xs text-red-600 hover:text-red-800 underline"
                        >
                            Clear All
                        </button>
                    </div>
                    <div className="max-h-40 overflow-y-auto">
                        {uploadedDocuments.map((doc, index) => (
                            <div key={index} className="flex items-center justify-between p-2 mb-2 bg-white rounded border">
                                <div className="flex items-center space-x-2">
                                    <span className="text-lg">{getFileTypeIcon(doc.fileType)}</span>
                                    <div>
                                        <p className="text-sm font-medium text-gray-800">{doc.fileName}</p>
                                        <p className="text-xs text-gray-500">
                                            {doc.totalChunks} chunks | {doc.fileType.toUpperCase()}
                                            {doc.metadata.headers && (
                                                <span> | Headers: {doc.metadata.headers}</span>
                                            )}
                                        </p>
                                    </div>
                                </div>
                                <div className="text-green-500">
                                    <svg className="w-4 h-4" fill="currentColor" viewBox="0 0 20 20">
                                        <path fillRule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clipRule="evenodd" />
                                    </svg>
                                </div>
                            </div>
                        ))}
                    </div>
                </div>
            )}
        </div>
    );
};
