/**
 * AI Chatbot Module
 * Tích hợp với Google Gemini AI để hỗ trợ người dùng
 */

class Chatbot {
    constructor() {
        this.isOpen = false;
        this.conversationId = this.generateConversationId();
        this.messages = [];
        this.apiBase = 'http://localhost:8080/api/chatbot';
        
        this.init();
    }

    init() {
        this.createChatbotHTML();
        this.attachEventListeners();
        this.loadWelcomeMessage();
    }

    generateConversationId() {
        return 'conv_' + Date.now() + '_' + Math.random().toString(36).substr(2, 9);
    }

    createChatbotHTML() {
        const chatbotHTML = `
            <div class="chatbot-container">
                <div class="chatbot-window" id="chatbotWindow">
                    <div class="chatbot-header">
                        <div class="chatbot-header-info">
                            <div class="avatar">
                                <i class="fas fa-robot"></i>
                            </div>
                            <div class="info">
                                <h3>AI Assistant</h3>
                                <p>Trợ lý thông minh</p>
                            </div>
                        </div>
                        <button class="close-btn" id="chatbotCloseBtn">
                            <i class="fas fa-times"></i>
                        </button>
                    </div>
                    <div class="chatbot-messages" id="chatbotMessages">
                        <!-- Messages will be added here -->
                    </div>
                    <div class="quick-actions" id="quickActions">
                        <button class="quick-action-btn" data-action="Xem sự kiện sắp diễn ra">📅 Sự kiện</button>
                        <button class="quick-action-btn" data-action="Hướng dẫn đăng ký">🎫 Đăng ký</button>
                        <button class="quick-action-btn" data-action="QR check-in là gì">📱 QR Code</button>
                    </div>
                    <div class="chatbot-input-container">
                        <input 
                            type="text" 
                            class="chatbot-input" 
                            id="chatbotInput" 
                            placeholder="Nhập câu hỏi của bạn..."
                            autocomplete="off"
                        />
                        <button class="chatbot-send-btn" id="chatbotSendBtn">
                            <i class="fas fa-paper-plane"></i>
                        </button>
                    </div>
                </div>
                <button class="chatbot-button" id="chatbotButton">
                    <i class="fas fa-comments"></i>
                </button>
            </div>
        `;

        document.body.insertAdjacentHTML('beforeend', chatbotHTML);
    }

    attachEventListeners() {
        const button = document.getElementById('chatbotButton');
        const closeBtn = document.getElementById('chatbotCloseBtn');
        const sendBtn = document.getElementById('chatbotSendBtn');
        const input = document.getElementById('chatbotInput');
        const quickActions = document.querySelectorAll('.quick-action-btn');

        button.addEventListener('click', () => this.toggleChatbot());
        closeBtn.addEventListener('click', () => this.closeChatbot());
        sendBtn.addEventListener('click', () => this.sendMessage());
        
        input.addEventListener('keypress', (e) => {
            if (e.key === 'Enter' && !e.shiftKey) {
                e.preventDefault();
                this.sendMessage();
            }
        });

        quickActions.forEach(btn => {
            btn.addEventListener('click', () => {
                const action = btn.getAttribute('data-action');
                this.sendQuickAction(action);
            });
        });
    }

    toggleChatbot() {
        const window = document.getElementById('chatbotWindow');
        this.isOpen = !this.isOpen;
        
        if (this.isOpen) {
            window.classList.add('active');
            document.getElementById('chatbotInput').focus();
        } else {
            window.classList.remove('active');
        }
    }

    closeChatbot() {
        this.isOpen = false;
        document.getElementById('chatbotWindow').classList.remove('active');
    }

    loadWelcomeMessage() {
        const welcomeMsg = {
            type: 'bot',
            content: 'Xin chào! 👋 Tôi là trợ lý AI của EventQR. Tôi có thể giúp bạn:\n\n' +
                     '• Tìm hiểu về các sự kiện sắp diễn ra\n' +
                     '• Hướng dẫn đăng ký tham gia sự kiện\n' +
                     '• Giải đáp thắc mắc về QR check-in\n' +
                     '• Hỗ trợ các tính năng khác của hệ thống\n\n' +
                     'Bạn cần hỗ trợ gì? 😊',
            timestamp: new Date()
        };
        
        this.messages.push(welcomeMsg);
        this.renderMessage(welcomeMsg);
    }

    sendQuickAction(action) {
        document.getElementById('chatbotInput').value = action;
        this.sendMessage();
    }

    async sendMessage() {
        const input = document.getElementById('chatbotInput');
        const message = input.value.trim();
        
        if (!message) return;

        // Clear input
        input.value = '';
        
        // Disable input and send button
        this.setInputDisabled(true);

        // Add user message
        const userMsg = {
            type: 'user',
            content: message,
            timestamp: new Date()
        };
        
        this.messages.push(userMsg);
        this.renderMessage(userMsg);

        // Show typing indicator
        this.showTypingIndicator();

        try {
            const response = await fetch(`${this.apiBase}/chat`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({
                    message: message,
                    conversationId: this.conversationId,
                    userId: this.getUserId()
                })
            });

            // Remove typing indicator
            this.hideTypingIndicator();

            if (!response.ok) {
                // HTTP error status
                const errorData = await response.json().catch(() => ({}));
                const errorMsg = errorData.message || `Lỗi ${response.status}: ${response.statusText}`;
                this.showError(errorMsg);
                return;
            }

            const data = await response.json();
            
            // Check if response has success field (from error response) or is direct ChatbotResponse
            if (data.success === false) {
                // Error response from backend
                this.showError(data.message || 'Xin lỗi, có lỗi xảy ra. Vui lòng thử lại sau.');
            } else if (data.success === true || data.message) {
                // Success response - can be either format
                const botMsg = {
                    type: 'bot',
                    content: data.message || 'Xin lỗi, không nhận được phản hồi.',
                    timestamp: new Date()
                };
                
                this.messages.push(botMsg);
                this.renderMessage(botMsg);
            } else {
                // Unexpected response format
                console.warn('Unexpected response format:', data);
                this.showError('Phản hồi không đúng định dạng. Vui lòng thử lại.');
            }
        } catch (error) {
            console.error('Chatbot error:', error);
            this.hideTypingIndicator();
            
            // More specific error messages
            if (error.name === 'TypeError' && error.message.includes('fetch')) {
                this.showError('Không thể kết nối đến server. Vui lòng kiểm tra:\n• Backend đã chạy chưa (http://localhost:8080)\n• Kết nối mạng\n• CORS configuration');
            } else {
                this.showError('Có lỗi xảy ra: ' + error.message);
            }
        } finally {
            this.setInputDisabled(false);
            input.focus();
        }
    }

    renderMessage(message) {
        const messagesContainer = document.getElementById('chatbotMessages');
        const messageDiv = document.createElement('div');
        messageDiv.className = `message ${message.type}`;

        const timeStr = this.formatTime(message.timestamp);
        
        messageDiv.innerHTML = `
            <div class="message-avatar">
                <i class="fas ${message.type === 'user' ? 'fa-user' : 'fa-robot'}"></i>
            </div>
            <div class="message-content">
                ${this.formatMessage(message.content)}
                <div class="message-time">${timeStr}</div>
            </div>
        `;

        messagesContainer.appendChild(messageDiv);
        this.scrollToBottom();
    }

    formatMessage(content) {
        // Convert newlines to <br>
        return content.replace(/\n/g, '<br>');
    }

    formatTime(date) {
        const hours = date.getHours().toString().padStart(2, '0');
        const minutes = date.getMinutes().toString().padStart(2, '0');
        return `${hours}:${minutes}`;
    }

    showTypingIndicator() {
        const messagesContainer = document.getElementById('chatbotMessages');
        const typingDiv = document.createElement('div');
        typingDiv.className = 'message bot';
        typingDiv.id = 'typingIndicator';
        typingDiv.innerHTML = `
            <div class="message-avatar">
                <i class="fas fa-robot"></i>
            </div>
            <div class="typing-indicator">
                <span></span>
                <span></span>
                <span></span>
            </div>
        `;
        messagesContainer.appendChild(typingDiv);
        this.scrollToBottom();
    }

    hideTypingIndicator() {
        const indicator = document.getElementById('typingIndicator');
        if (indicator) {
            indicator.remove();
        }
    }

    showError(message) {
        const errorMsg = {
            type: 'bot',
            content: message,
            timestamp: new Date(),
            isError: true
        };
        
        this.messages.push(errorMsg);
        
        const messagesContainer = document.getElementById('chatbotMessages');
        const messageDiv = document.createElement('div');
        messageDiv.className = 'message bot error';
        
        const timeStr = this.formatTime(errorMsg.timestamp);
        
        messageDiv.innerHTML = `
            <div class="message-avatar">
                <i class="fas fa-exclamation-circle"></i>
            </div>
            <div class="message-content">
                ${this.formatMessage(message)}
                <div class="message-time">${timeStr}</div>
            </div>
        `;

        messagesContainer.appendChild(messageDiv);
        this.scrollToBottom();
    }

    setInputDisabled(disabled) {
        const input = document.getElementById('chatbotInput');
        const sendBtn = document.getElementById('chatbotSendBtn');
        
        input.disabled = disabled;
        sendBtn.disabled = disabled;
    }

    scrollToBottom() {
        const messagesContainer = document.getElementById('chatbotMessages');
        messagesContainer.scrollTop = messagesContainer.scrollHeight;
    }

    getUserId() {
        try {
            const user = localStorage.getItem('currentUser');
            if (user) {
                const userData = JSON.parse(user);
                return userData.accountId || null;
            }
        } catch (e) {
            console.error('Error getting user ID:', e);
        }
        return null;
    }
}

// Initialize chatbot when DOM is ready
document.addEventListener('DOMContentLoaded', () => {
    window.chatbot = new Chatbot();
});

