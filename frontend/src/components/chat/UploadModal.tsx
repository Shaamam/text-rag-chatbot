import React, { useState, useRef } from 'react';
import { documentApi } from '../../axiosClient';

interface ProcessedDocument {
    fileName: string;
    fileType: string;
    chunks: string[];
    metadata: { [key: string]: any };
    totalChunks: number;
}

interface UploadModalProps {
    isOpen: boolean;
    onClose: () => void;
    onUploadSuccess: (documents: ProcessedDocument[]) => void;
    onUploadError: (error: string) => void;
}

export const UploadModal: React.FC<UploadModalProps> = ({ 
    isOpen, 
    onClose, 
    onUploadSuccess, 
    onUploadError 
}) => {
    const [isUploading, setIsUploading] = useState<boolean>(false);
    const [dragActive, setDragActive] = useState<boolean>(false);
    const fileInputRef = useRef<HTMLInputElement>(null);

    if (!isOpen) return null;

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
            onUploadSuccess(processedDocs);
            onClose();
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
        if (!isUploading) {
            fileInputRef.current?.click();
        }
    };

    const handleOverlayClick = (e: React.MouseEvent<HTMLDivElement>): void => {
        if (e.target === e.currentTarget && !isUploading) {
            onClose();
        }
    };

    return (
        <div 
            className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50"
            onClick={handleOverlayClick}
        >
            <div className="bg-white rounded-lg p-6 max-w-lg w-full mx-4 max-h-[90vh] overflow-y-auto">
                {/* Modal Header */}
                <div className="flex items-center justify-between mb-4">
                    <h2 className="text-xl font-semibold text-gray-900">Upload Document</h2>
                    <button
                        onClick={onClose}
                        disabled={isUploading}
                        className={`text-gray-400 hover:text-gray-600 transition-colors ${
                            isUploading ? 'opacity-50 cursor-not-allowed' : ''
                        }`}
                    >
                        <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                        </svg>
                    </button>
                </div>

                {/* Upload Area */}
                <div
                    className={`border-2 border-dashed rounded-lg p-8 text-center transition-colors cursor-pointer ${
                        dragActive
                            ? 'border-blue-500 bg-blue-50'
                            : isUploading
                            ? 'border-gray-300 bg-gray-50 cursor-not-allowed'
                            : 'border-gray-300 hover:border-blue-400 hover:bg-blue-50'
                    }`}
                    onDragOver={handleDragOver}
                    onDragLeave={handleDragLeave}
                    onDrop={handleDrop}
                    onClick={triggerFileInput}
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
                            <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 mb-4"></div>
                            <p className="text-lg text-gray-600 mb-2">Uploading and processing documents...</p>
                            <p className="text-sm text-gray-500">Please wait, this may take a few moments</p>
                        </div>
                    ) : (
                        <div className="flex flex-col items-center">
                            <svg 
                                className="w-16 h-16 text-gray-400 mb-4" 
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
                            <h3 className="text-lg font-medium text-gray-700 mb-2">
                                Choose files or drag them here
                            </h3>
                            <p className="text-sm text-gray-500 mb-2">
                                Supported formats: PDF, Word, Text, CSV, Excel (.xlsx/.xls)
                            </p>
                            <p className="text-xs text-gray-400 mb-1">
                                Maximum file size: 10MB per file
                            </p>
                            <p className="text-xs text-gray-500">
                                <strong>Chunking:</strong> TXT/PDF/Word: paragraphs | CSV/Excel: rows with headers
                            </p>
                        </div>
                    )}
                </div>

                {/* Instructions */}
                <div className="mt-4 p-4 bg-blue-50 rounded-lg">
                    <h4 className="text-sm font-medium text-blue-800 mb-2">What happens next?</h4>
                    <ul className="text-sm text-blue-600 space-y-1">
                        <li>• Your document will be processed and analyzed</li>
                        <li>• Text content will be extracted and stored securely</li>
                        <li>• You can then ask questions about the document content</li>
                        <li>• The AI will provide answers based on your uploaded documents</li>
                    </ul>
                </div>

                {/* Footer */}
                <div className="mt-6 flex justify-end space-x-3">
                    <button
                        onClick={onClose}
                        disabled={isUploading}
                        className={`px-4 py-2 text-sm font-medium text-gray-700 bg-gray-200 rounded-md hover:bg-gray-300 transition-colors ${
                            isUploading ? 'opacity-50 cursor-not-allowed' : ''
                        }`}
                    >
                        {isUploading ? 'Processing...' : 'Cancel'}
                    </button>
                </div>
            </div>
        </div>
    );
};
