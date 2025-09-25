import React, { useState, useRef, useEffect } from 'react';
import { chatApi } from "../../axiosClient";
import { UploadModal } from './UploadModal';

interface Message {
    text: string;
    sender: 'user' | 'bot';
}

export const ChatWindow: React.FC = () => {
    const [messages, setMessages] = useState<Message[]>([
        { text: "Welcome to TextRAG! Upload documents to get started, then ask questions about their content.", sender: "bot" },
    ]);
    const [inputText, setInputText] = useState<string>('');
    const [isTyping, setIsTyping] = useState<boolean>(false);
    const [apiError, setApiError] = useState<string | null>(null);
    const [sessionId, setSessionId] = useState<string>(() => `session-${Date.now()}`);
    const [isUploadModalOpen, setIsUploadModalOpen] = useState<boolean>(false);
    const messagesEndRef = useRef<HTMLDivElement>(null);
    const textAreaRef = useRef<HTMLTextAreaElement>(null);

    // Auto-scroll to bottom when new messages arrive
    useEffect(() => {
        messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
    }, [messages]);

    // Auto-resize textarea as content grows
    useEffect(() => {
        const textArea = textAreaRef.current;
        if (textArea) {
            // Reset height to allow shrinking
            textArea.style.height = 'auto';
            // Set height based on scroll height (content)
            textArea.style.height = `${textArea.scrollHeight}px`;
        }
    }, [inputText]);

    // Send message to API and get response
    const sendMessageToApi = async (userMessage: string): Promise<void> => {
        setIsTyping(true);
        setApiError(null);

        try {
            // Ensure we're sending the exact format expected by the backend
            const requestPayload = {
                sessionId: sessionId,
                Question: userMessage  // Capital Q as expected by ChatRequest record
            };

            console.log('Sending chat request:', requestPayload);

            const response = await chatApi.post('', requestPayload);

            console.log('Received chat response:', response.data);

            // API returns ChatResponse with sessionId, question, and answer
            const botResponse = response.data.answer;

            if (botResponse) {
                setMessages(prev => [...prev, { text: botResponse, sender: "bot" }]);
            } else {
                throw new Error('No answer received from API');
            }
        } catch (error: any) {
            console.error('Error sending message to API:', error);
            const errorMessage = error.response?.data?.message || error.message || 'Sorry, I encountered an error. Please try again later.';
            setMessages(prev => [...prev, { text: errorMessage, sender: "bot" }]);
            setApiError(errorMessage);
        } finally {
            setIsTyping(false);
        }
    };

    const handleSendMessage = async (e: React.FormEvent): Promise<void> => {
        e.preventDefault();
        if (inputText.trim() === '') return;

        // Add user message to chat
        const userMessage = inputText.trim();
        setMessages(prev => [...prev, { text: userMessage, sender: "user" }]);

        // Clear input
        setInputText('');

        // Send to API and get response
        await sendMessageToApi(userMessage);
    };

    // Handle keyboard shortcuts
    const handleKeyDown = (e: React.KeyboardEvent<HTMLTextAreaElement>): void => {
        if (e.key === 'Enter' && !e.shiftKey) {
            e.preventDefault();
            if (inputText.trim() !== '') {
                handleSendMessage(e);
            }
        }
    };

    // Handle document upload success
    const handleUploadSuccess = (message: string): void => {
        setMessages(prev => [...prev, { text: message, sender: "bot" }]);
        setIsUploadModalOpen(false); // Close modal after successful upload
    };

    // Handle document upload error
    const handleUploadError = (error: string): void => {
        setMessages(prev => [...prev, { text: error, sender: "bot" }]);
    };

    // Toggle upload modal visibility
    const toggleUploadModal = (): void => {
        setIsUploadModalOpen(!isUploadModalOpen);
    };

    return (
        <div className="flex flex-col h-[calc(100vh-160px)] max-w-4xl mx-auto px-4 py-6">
            {/* Chat header */}
            <div className="bg-[#00095B] rounded-t-lg p-4 text-white">
                <div className="flex items-center">
                    {/*<div className="w-10 h-10 bg-white rounded-full flex items-center justify-center mr-3">*/}
                    {/*</div>*/}
                    <div>
                        <h2 className="text-xl font-medium">TextRAG Assistant</h2>
                        <p className="text-sm font-light">Document Q&A with RAG</p>
                    </div>
                    <div className="ml-auto flex items-center space-x-3">
                        <button
                            onClick={toggleUploadModal}
                            className="text-white hover:text-gray-200 transition-colors"
                            title="Upload document"
                        >
                            <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M7 16a4 4 0 01-.88-7.903A5 5 0 1115.9 6L16 6a5 5 0 011 9.9M15 13l-3-3m0 0l-3 3m3-3v12" />
                            </svg>
                        </button>
                        <div className="w-3 h-3 bg-green-500 rounded-full"></div>
                    </div>
                </div>
            </div>

            {/* Chat messages */}
            <div className="flex-1 bg-gray-100 p-4 overflow-y-auto">
                <div className="space-y-4">
                    {messages.map((message, index) => (
                        <div
                            key={index}
                            className={`flex ${message.sender === 'user' ? 'justify-end' : 'justify-start'}`}
                        >
                            <div
                                className={`max-w-[80%] rounded-lg p-3 ${
                                    message.sender === 'user'
                                        ? 'bg-[#00095B] text-white rounded-br-none'
                                        : 'bg-white text-gray-800 shadow-md rounded-bl-none'
                                }`}
                            >
                                <p className="text-sm md:text-base font-normal whitespace-pre-wrap">{message.text}</p>
                            </div>
                        </div>
                    ))}

                    {isTyping && (
                        <div className="flex justify-start">
                            <div className="bg-white text-gray-800 rounded-lg p-3 shadow-md rounded-bl-none max-w-[80%]">
                                <div className="flex space-x-1">
                                    <div className="w-2 h-2 bg-gray-400 rounded-full animate-bounce"></div>
                                    <div className="w-2 h-2 bg-gray-400 rounded-full animate-bounce" style={{animationDelay: '0.2s'}}></div>
                                    <div className="w-2 h-2 bg-gray-400 rounded-full animate-bounce" style={{animationDelay: '0.4s'}}></div>
                                </div>
                            </div>
                        </div>
                    )}

                    <div ref={messagesEndRef} />
                </div>
            </div>

            {/* Chat input */}
            <form onSubmit={handleSendMessage} className="bg-white p-4 border-t border-gray-200 rounded-b-lg shadow-inner">
                <div className="flex items-center space-x-3">
                    <div className="flex-1 relative">
            <textarea
                ref={textAreaRef}
                value={inputText}
                onChange={(e: React.ChangeEvent<HTMLTextAreaElement>) => setInputText(e.target.value)}
                onKeyDown={handleKeyDown}
                placeholder="Type your message here..."
                rows={1}
                className="w-full p-3 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-bg-[#00095B] focus:border-transparent font-normal resize-none overflow-y-auto"
                style={{ maxHeight: '120px', minHeight: '44px' }}
            />
                        <div className="text-xs text-gray-400 absolute bottom-1 right-2">
                            {inputText.trim() !== '' && 'Press Enter to send'}
                        </div>
                    </div>
                    <button
                        type="submit"
                        disabled={inputText.trim() === '' || isTyping}
                        className={`bg-[#00095B] text-white p-3 rounded-md ${
                            inputText.trim() === '' || isTyping ? 'opacity-50 cursor-not-allowed' : 'hover:bg-blue-800'
                        }`}
                    >
                        <svg xmlns="http://www.w3.org/2000/svg" className="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M14 5l7 7m0 0l-7 7m7-7H3" />
                        </svg>
                    </button>
                </div>
                <div className="mt-2 text-xs text-gray-500 font-light text-center">
                    Upload documents and ask questions about their content • Session: {sessionId}
                </div>
            </form>

            {/* Upload Modal */}
            <UploadModal
                isOpen={isUploadModalOpen}
                onClose={() => setIsUploadModalOpen(false)}
                onUploadSuccess={handleUploadSuccess}
                onUploadError={handleUploadError}
            />
        </div>
    );
};
