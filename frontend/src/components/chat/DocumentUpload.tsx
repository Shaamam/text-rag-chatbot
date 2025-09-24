import React, { useState, useRef } from 'react';
import { documentApi } from '../../axiosClient';

interface DocumentUploadProps {
    onUploadSuccess: (message: string) => void;
    onUploadError: (error: string) => void;
}

export const DocumentUpload: React.FC<DocumentUploadProps> = ({ onUploadSuccess, onUploadError }) => {
    const [isUploading, setIsUploading] = useState<boolean>(false);
    const [dragActive, setDragActive] = useState<boolean>(false);
    const fileInputRef = useRef<HTMLInputElement>(null);

    const handleFileUpload = async (file: File): Promise<void> => {
        setIsUploading(true);

        try {
            const formData = new FormData();
            formData.append('doc', file);

            const response = await documentApi.post('/upload', formData, {
                headers: {
                    'Content-Type': 'multipart/form-data',
                }
            });

            onUploadSuccess(`Document "${file.name}" uploaded successfully! ${response.data}`);
        } catch (error: any) {
            console.error('Error uploading document:', error);
            const errorMessage = error.response?.data?.message || `Failed to upload "${file.name}". Please try again.`;
            onUploadError(errorMessage);
        } finally {
            setIsUploading(false);
        }
    };

    const handleFileSelect = (e: React.ChangeEvent<HTMLInputElement>): void => {
        const files = e.target.files;
        if (files && files[0]) {
            handleFileUpload(files[0]);
        }
        // Clear the input so the same file can be selected again
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
        if (files && files[0]) {
            handleFileUpload(files[0]);
        }
    };

    const triggerFileInput = (): void => {
        fileInputRef.current?.click();
    };

    const getSupportedFormats = (): string => {
        return "PDF (.pdf), Word (.docx), Text (.txt)";
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
                    accept=".pdf,.docx,.txt"
                    onChange={handleFileSelect}
                    className="hidden"
                    disabled={isUploading}
                />

                {isUploading ? (
                    <div className="flex flex-col items-center">
                        <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600 mb-2"></div>
                        <p className="text-sm text-gray-600">Uploading and processing document...</p>
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
                            Click to upload or drag and drop
                        </p>
                        <p className="text-xs text-gray-500">
                            Supported: {getSupportedFormats()}
                        </p>
                        <p className="text-xs text-gray-400 mt-1">
                            Maximum file size: 10MB
                        </p>
                    </div>
                )}
            </div>

            {/* Upload Instructions */}
            <div className="mt-2 text-xs text-gray-500 text-center">
                Upload documents to ask questions about their content. The system will process and store them for retrieval.
            </div>
        </div>
    );
};
