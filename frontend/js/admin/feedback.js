/* =========================================================
   FEEDBACK PAGE JS — LOGIC KẾT NỐI API VÀO HTML
   ========================================================= */

// API Configuration
// Tránh duplicate declaration
if (typeof window.FEEDBACK_API_BASE_URL === 'undefined') {
    window.FEEDBACK_API_BASE_URL = 'http://localhost:8080/api';
}
// Dùng tên biến riêng để tránh xung đột với các file khác
const FEEDBACK_API_BASE_URL = window.FEEDBACK_API_BASE_URL;
let allFeedbacks = [];
let currentSelectedFeedback = null;
let currentOrganizerId = null;

// Initialize on page load
document.addEventListener('DOMContentLoaded', function() {
    console.log('📝 Feedback page loading...');
    
    // Check authentication
    const userData = getCurrentUser();
    if (!userData) {
        console.error('❌ Chưa đăng nhập!');
        redirectToHome();
        return;
    }
    
    currentOrganizerId = userData.user_id;
    if (!currentOrganizerId) {
        console.error('❌ Không tìm thấy organizer ID!');
        return;
    }
    
    // Update user name in header
    const userNameElement = document.getElementById('userName');
    if (userNameElement) {
        userNameElement.textContent = userData.name || 'Người dùng';
    }
    
    // Initialize page
    initializeFeedbackPage();
    
    console.log('✅ Feedback page loaded successfully');
});

/**
 * Initialize feedback page
 */
function initializeFeedbackPage() {
    // Load feedbacks
    loadFeedbacks();
    
    // Setup event listeners
    setupEventListeners();
}

/**
 * Setup event listeners for filters, search, etc.
 */
function setupEventListeners() {
    // Event filter
    const eventFilter = document.querySelector('.af-filters .af-select:first-of-type');
    if (eventFilter) {
        eventFilter.addEventListener('change', handleEventFilter);
    }
    
    // Rating filter
    const ratingFilter = document.querySelectorAll('.af-select')[1];
    if (ratingFilter) {
        ratingFilter.addEventListener('change', handleRatingFilter);
    }
    
    // Search input
    const searchInput = document.querySelector('.af-search input');
    if (searchInput) {
        searchInput.addEventListener('input', handleSearch);
    }
    
    // Export buttons
    const exportAllBtn = document.getElementById('exportAllBtn');
    if (exportAllBtn) {
        exportAllBtn.addEventListener('click', exportAllFeedbacks);
    }
    
    const exportSingleBtn = document.getElementById('exportSingleBtn');
    if (exportSingleBtn) {
        exportSingleBtn.addEventListener('click', exportSingleFeedback);
    }
}

/**
 * Load feedbacks from API (global function for retry button)
 */
window.loadFeedbacks = async function() {
    const container = document.getElementById('af-feedback-groups');
    const emptyState = document.getElementById('af-empty-state');
    
    if (!container) {
        console.error('❌ Không tìm thấy container af-feedback-groups');
        return;
    }
    
    container.innerHTML = '<p style="text-align: center; padding: 40px;"><i class="fas fa-spinner fa-spin"></i> Đang tải feedback...</p>';
    
    try {
        const url = `${FEEDBACK_API_BASE_URL}/feedback/organizer/${currentOrganizerId}`;
        console.log('📡 Đang gọi API:', url);
        
        const response = await fetch(url);
        
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }
        
        const data = await response.json();
        console.log('✅ Nhận được feedback:', data);
        
        // Check if data is wrapped in a response object
        if (data.success === false) {
            throw new Error(data.message || 'Lỗi khi lấy feedback');
        }
        
        // Handle both array and object responses
        const feedbacks = Array.isArray(data) ? data : (data.feedbacks || []);
        allFeedbacks = feedbacks;
        
        if (feedbacks.length === 0) {
            container.style.display = 'none';
            if (emptyState) emptyState.style.display = 'block';
            updateSummaryCards([]);
            return;
        }
        
        container.style.display = 'block';
        if (emptyState) emptyState.style.display = 'none';
        
        // Render feedbacks
        renderFeedbacks(feedbacks);
        
        // Update summary cards
        updateSummaryCards(feedbacks);
        
        // Populate event filter
        populateEventFilter(feedbacks);
        
    } catch (error) {
        console.error('❌ Lỗi khi tải feedback:', error);
        container.innerHTML = `
            <div style="text-align: center; padding: 40px; color: #ef4444;">
                <i class="fas fa-exclamation-triangle" style="font-size: 48px; margin-bottom: 16px;"></i>
                <p style="font-size: 16px; margin-bottom: 8px;">Không thể tải feedback</p>
                <p style="font-size: 14px; color: #64748b;">${error.message}</p>
                <button onclick="loadFeedbacks()" style="margin-top: 16px; padding: 8px 16px; background: #3b82f6; color: white; border: none; border-radius: 6px; cursor: pointer;">
                    Thử lại
                </button>
            </div>
        `;
    }
}

/**
 * Render feedbacks grouped by event
 */
function renderFeedbacks(feedbacks) {
    const container = document.getElementById('af-feedback-groups');
    if (!container) return;
    
    // Group feedbacks by event
    const groupedByEvent = {};
    feedbacks.forEach(feedback => {
        const eventId = feedback.eventId;
        if (!groupedByEvent[eventId]) {
            groupedByEvent[eventId] = {
                eventId: eventId,
                eventName: feedback.eventTitle || 'Sự kiện không có tên',
                feedbacks: []
            };
        }
        groupedByEvent[eventId].feedbacks.push(feedback);
    });
    
    // Sort events by most recent feedback
    const eventGroups = Object.values(groupedByEvent).sort((a, b) => {
        const aLatest = new Date(a.feedbacks[0].createdAt || 0);
        const bLatest = new Date(b.feedbacks[0].createdAt || 0);
        return bLatest - aLatest;
    });
    
    // Render HTML
    let html = '';
    eventGroups.forEach(group => {
        // Sort feedbacks by date (newest first)
        group.feedbacks.sort((a, b) => {
            const dateA = new Date(a.createdAt || 0);
            const dateB = new Date(b.createdAt || 0);
            return dateB - dateA;
        });
        
        html += `
            <div class="af-event-group" data-event-id="${group.eventId}">
                <div class="af-event-header">
                    <h4 class="af-event-name">${escapeHtml(group.eventName)}</h4>
                    <span class="af-event-count">${group.feedbacks.length} feedback</span>
                </div>
                <div class="af-feedback-list">
                    ${group.feedbacks.map(feedback => renderFeedbackItem(feedback)).join('')}
                </div>
            </div>
        `;
    });
    
    container.innerHTML = html;
    
    // Attach click handlers
    attachFeedbackClickHandlers();
}

/**
 * Render a single feedback item
 */
function renderFeedbackItem(feedback) {
    const rating = feedback.rating || 0;
    const stars = '★'.repeat(rating) + '☆'.repeat(5 - rating);
    const userName = feedback.userName || 'Khách ẩn danh';
    const comment = feedback.comment || '(Không có bình luận)';
    const time = formatDateTime(feedback.createdAt);
    const hasReply = feedback.organizerReply && feedback.organizerReply.trim().length > 0;
    
    return `
        <div class="af-feedback-item" data-feedback-id="${feedback.feedbackId}">
            <div class="af-feedback-header">
                <div class="af-feedback-user">
                    <strong>${escapeHtml(userName)}</strong>
                    <span class="af-feedback-rating" title="${rating} sao">${stars}</span>
                </div>
                <span class="af-feedback-time">${time}</span>
            </div>
            <div class="af-feedback-comment">${escapeHtml(comment)}</div>
            ${hasReply ? '<div class="af-feedback-replied"><i class="fas fa-reply"></i> Đã phản hồi</div>' : ''}
        </div>
    `;
}

/**
 * Attach click handlers to feedback items
 */
function attachFeedbackClickHandlers() {
    const feedbackItems = document.querySelectorAll('.af-feedback-item');
    feedbackItems.forEach(item => {
        item.addEventListener('click', function(e) {
            const feedbackId = parseInt(this.getAttribute('data-feedback-id'));
            selectFeedback(feedbackId);
        });
    });
}

/**
 * Select a feedback to show details (global function for onclick)
 */
window.selectFeedback = function(feedbackId) {
    const feedback = allFeedbacks.find(f => f.feedbackId === feedbackId);
    if (!feedback) {
        console.error('❌ Không tìm thấy feedback:', feedbackId);
        return;
    }
    
    currentSelectedFeedback = feedback;
    
    // Update side panel
    updateSidePanel(feedback);
    
    // Highlight selected item
    document.querySelectorAll('.af-feedback-item').forEach(item => {
        item.classList.remove('active');
    });
    const selectedItem = document.querySelector(`[data-feedback-id="${feedbackId}"]`);
    if (selectedItem) {
        selectedItem.classList.add('active');
    }
}

/**
 * Update side panel with feedback details
 */
function updateSidePanel(feedback) {
    const placeholder = document.getElementById('af-side-content');
    const details = document.getElementById('af-side-details');
    
    if (!placeholder || !details) return;
    
    // Hide placeholder, show details
    placeholder.style.display = 'none';
    details.style.display = 'block';
    
    // Update content
    const eventName = document.getElementById('side-event-name');
    const userName = document.getElementById('side-user-name');
    const rating = document.getElementById('side-rating');
    const time = document.getElementById('side-time');
    const content = document.getElementById('side-content');
    
    if (eventName) eventName.textContent = feedback.eventTitle || 'Sự kiện không có tên';
    if (userName) {
        const name = feedback.userName || 'Khách ẩn danh';
        const ratingValue = feedback.rating || 0;
        userName.innerHTML = `${escapeHtml(name)} · <span id="side-rating">${ratingValue} sao</span>`;
    }
    if (time) time.textContent = formatDateTime(feedback.createdAt);
    if (content) content.textContent = feedback.comment || '(Không có bình luận)';
    
    // Update reply section
    updateReplySection(feedback);
}

/**
 * Update reply section
 */
function updateReplySection(feedback) {
    const replyDisplay = document.getElementById('af-reply-display');
    const replyForm = document.getElementById('af-reply-form');
    const replyText = document.getElementById('af-reply-text');
    const replyTime = document.getElementById('af-reply-time');
    const replyInput = document.getElementById('af-reply-input');
    const cancelBtn = document.getElementById('af-reply-cancel');
    
    if (!replyDisplay || !replyForm) return;
    
    const hasReply = feedback.organizerReply && feedback.organizerReply.trim().length > 0;
    
    if (hasReply) {
        // Show existing reply
        replyDisplay.style.display = 'block';
        replyForm.style.display = 'none';
        
        if (replyText) replyText.textContent = feedback.organizerReply;
        if (replyTime) {
            replyTime.textContent = formatDateTime(feedback.organizerReplyAt);
        }
    } else {
        // Show form to create new reply
        replyDisplay.style.display = 'none';
        replyForm.style.display = 'block';
        
        if (replyInput) {
            replyInput.value = '';
            if (cancelBtn) cancelBtn.style.display = 'none';
        }
    }
}

/**
 * Submit reply to feedback (global function for onclick)
 */
window.submitReply = async function() {
    if (!currentSelectedFeedback) {
        alert('Vui lòng chọn một feedback để phản hồi');
        return;
    }
    
    const replyInput = document.getElementById('af-reply-input');
    if (!replyInput) return;
    
    const replyText = replyInput.value.trim();
    if (!replyText) {
        alert('Vui lòng nhập nội dung phản hồi');
        return;
    }
    
    try {
        const url = `${FEEDBACK_API_BASE_URL}/feedback/${currentSelectedFeedback.feedbackId}/reply`;
        console.log('📡 Đang gửi reply:', url);
        
        const response = await fetch(url, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                organizerId: currentOrganizerId,
                reply: replyText
            })
        });
        
        if (!response.ok) {
            const errorData = await response.json();
            throw new Error(errorData.message || 'Lỗi khi gửi phản hồi');
        }
        
        const data = await response.json();
        console.log('✅ Reply thành công:', data);
        
        // Update feedback in local data
        if (data.feedback) {
            const index = allFeedbacks.findIndex(f => f.feedbackId === currentSelectedFeedback.feedbackId);
            if (index !== -1) {
                allFeedbacks[index] = data.feedback;
                currentSelectedFeedback = data.feedback;
            }
        }
        
        // Update UI
        updateReplySection(currentSelectedFeedback);
        
        // Show success message
        alert('Đã gửi phản hồi thành công!');
        
    } catch (error) {
        console.error('❌ Lỗi khi gửi reply:', error);
        alert('Lỗi: ' + error.message);
    }
}

/**
 * Edit reply (global function for onclick)
 */
window.editReply = function() {
    if (!currentSelectedFeedback) return;
    
    const replyDisplay = document.getElementById('af-reply-display');
    const replyForm = document.getElementById('af-reply-form');
    const replyInput = document.getElementById('af-reply-input');
    const cancelBtn = document.getElementById('af-reply-cancel');
    
    if (!replyDisplay || !replyForm || !replyInput) return;
    
    // Show form with existing reply
    replyDisplay.style.display = 'none';
    replyForm.style.display = 'block';
    
    if (currentSelectedFeedback.organizerReply) {
        replyInput.value = currentSelectedFeedback.organizerReply;
    }
    
    if (cancelBtn) cancelBtn.style.display = 'inline-block';
}

/**
 * Cancel reply editing (global function for onclick)
 */
window.cancelReply = function() {
    if (!currentSelectedFeedback) return;
    
    const replyDisplay = document.getElementById('af-reply-display');
    const replyForm = document.getElementById('af-reply-form');
    const cancelBtn = document.getElementById('af-reply-cancel');
    
    if (!replyDisplay || !replyForm) return;
    
    // Show display again
    updateReplySection(currentSelectedFeedback);
    
    if (cancelBtn) cancelBtn.style.display = 'none';
}

/**
 * Handle event filter change
 */
function handleEventFilter(e) {
    const selectedEvent = e.target.value;
    filterFeedbacks(selectedEvent, null, null);
}

/**
 * Handle rating filter change
 */
function handleRatingFilter(e) {
    const selectedRating = e.target.value;
    filterFeedbacks(null, selectedRating, null);
}

/**
 * Handle search input
 */
function handleSearch(e) {
    const searchTerm = e.target.value.trim();
    filterFeedbacks(null, null, searchTerm);
}

/**
 * Filter feedbacks
 */
function filterFeedbacks(eventFilter, ratingFilter, searchTerm) {
    let filtered = [...allFeedbacks];
    
    // Filter by event
    if (eventFilter && eventFilter !== 'Tất cả sự kiện') {
        filtered = filtered.filter(f => f.eventTitle === eventFilter);
    }
    
    // Filter by rating
    if (ratingFilter && ratingFilter !== '') {
        if (ratingFilter === '5 sao') {
            filtered = filtered.filter(f => f.rating === 5);
        } else if (ratingFilter === '4 sao') {
            filtered = filtered.filter(f => f.rating === 4);
        } else if (ratingFilter === '3 sao') {
            filtered = filtered.filter(f => f.rating === 3);
        } else if (ratingFilter === '1–2 sao') {
            filtered = filtered.filter(f => f.rating <= 2);
        }
    }
    
    // Filter by search term
    if (searchTerm) {
        const term = searchTerm.toLowerCase();
        filtered = filtered.filter(f => {
            const userName = (f.userName || '').toLowerCase();
            const comment = (f.comment || '').toLowerCase();
            return userName.includes(term) || comment.includes(term);
        });
    }
    
    // Re-render
    renderFeedbacks(filtered);
    updateSummaryCards(filtered);
}

/**
 * Populate event filter dropdown
 */
function populateEventFilter(feedbacks) {
    const eventFilter = document.querySelector('.af-filters .af-select:first-of-type');
    if (!eventFilter) return;
    
    // Get unique event names
    const eventNames = [...new Set(feedbacks.map(f => f.eventTitle).filter(Boolean))];
    
    // Keep "Tất cả sự kiện" option
    const firstOption = eventFilter.querySelector('option');
    eventFilter.innerHTML = '';
    if (firstOption) {
        eventFilter.appendChild(firstOption);
    }
    
    // Add event options
    eventNames.forEach(name => {
        const option = document.createElement('option');
        option.value = name;
        option.textContent = name;
        eventFilter.appendChild(option);
    });
}

/**
 * Update summary cards
 */
function updateSummaryCards(feedbacks) {
    if (feedbacks.length === 0) {
        // Reset to default
        updateSummaryCard(0, '4.8', 'Trên 245 feedback');
        updateSummaryCard(1, '12', 'So với hôm qua: +3');
        updateSummaryCard(2, '82%', '203 feedback 5 sao');
        return;
    }
    
    // Calculate average rating
    const totalRating = feedbacks.reduce((sum, f) => sum + (f.rating || 0), 0);
    const avgRating = (totalRating / feedbacks.length).toFixed(1);
    
    // Count feedbacks today
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    const todayFeedbacks = feedbacks.filter(f => {
        const feedbackDate = new Date(f.createdAt);
        feedbackDate.setHours(0, 0, 0, 0);
        return feedbackDate.getTime() === today.getTime();
    });
    
    // Count 5-star feedbacks
    const fiveStarFeedbacks = feedbacks.filter(f => f.rating === 5);
    const fiveStarPercentage = Math.round((fiveStarFeedbacks.length / feedbacks.length) * 100);
    
    // Update cards
    updateSummaryCard(0, avgRating, `Trên ${feedbacks.length} feedback`);
    updateSummaryCard(1, todayFeedbacks.length.toString(), 'Hôm nay');
    updateSummaryCard(2, `${fiveStarPercentage}%`, `${fiveStarFeedbacks.length} feedback 5 sao`);
}

/**
 * Update a summary card
 */
function updateSummaryCard(index, value, subtitle) {
    const cards = document.querySelectorAll('.af-card');
    if (cards[index]) {
        const h3 = cards[index].querySelector('h3');
        const span = cards[index].querySelector('span');
        if (h3) h3.textContent = value;
        if (span) span.textContent = subtitle;
    }
}

/**
 * Export all feedbacks
 */
function exportAllFeedbacks() {
    if (allFeedbacks.length === 0) {
        alert('Không có feedback nào để xuất');
        return;
    }
    
    exportToCSV(allFeedbacks, 'all_feedbacks');
}

/**
 * Export single feedback
 */
function exportSingleFeedback() {
    if (!currentSelectedFeedback) {
        alert('Vui lòng chọn một feedback để xuất');
        return;
    }
    
    exportToCSV([currentSelectedFeedback], `feedback_${currentSelectedFeedback.feedbackId}`);
}

/**
 * Export to CSV
 */
function exportToCSV(feedbacks, filename) {
    // CSV header
    const headers = ['Event ID', 'Event Name', 'User Name', 'Rating', 'Comment', 'Reply', 'Created At', 'Reply At'];
    
    // CSV rows
    const rows = feedbacks.map(f => {
        return [
            f.eventId || '',
            f.eventTitle || '',
            f.userName || '',
            f.rating || '',
            (f.comment || '').replace(/"/g, '""'), // Escape quotes
            (f.organizerReply || '').replace(/"/g, '""'),
            formatDateTime(f.createdAt),
            f.organizerReplyAt ? formatDateTime(f.organizerReplyAt) : ''
        ];
    });
    
    // Combine
    const csvContent = [
        headers.join(','),
        ...rows.map(row => row.map(cell => `"${cell}"`).join(','))
    ].join('\n');
    
    // Create blob and download
    const blob = new Blob(['\ufeff' + csvContent], { type: 'text/csv;charset=utf-8;' });
    const link = document.createElement('a');
    const url = URL.createObjectURL(blob);
    link.setAttribute('href', url);
    link.setAttribute('download', `${filename}_${new Date().toISOString().split('T')[0]}.csv`);
    link.style.visibility = 'hidden';
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
}

/**
 * Format datetime
 */
function formatDateTime(dateString) {
    if (!dateString) return 'N/A';
    try {
        const date = new Date(dateString);
        return date.toLocaleString('vi-VN', {
            year: 'numeric',
            month: '2-digit',
            day: '2-digit',
            hour: '2-digit',
            minute: '2-digit'
        });
    } catch (e) {
        return dateString;
    }
}

/**
 * Escape HTML to prevent XSS
 */
function escapeHtml(text) {
    if (!text) return '';
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

/**
 * Get current user from localStorage
 */
function getCurrentUser() {
    try {
        const userStr = localStorage.getItem('currentUser');
        if (!userStr) return null;
        return JSON.parse(userStr);
    } catch (e) {
        console.error('Error parsing user data:', e);
        return null;
    }
}

/**
 * Redirect to home
 */
function redirectToHome() {
    window.location.href = '../../index.html';
}

