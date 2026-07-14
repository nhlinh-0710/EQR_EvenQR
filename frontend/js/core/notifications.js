// ======================================
// 🔔 NOTIFICATION MANAGEMENT (CHUNG CHO USER VÀ ADMIN)
// ======================================

let allNotifications = [];
let unreadCount = 0;

/**
 * Khởi tạo notification system
 */
function initNotifications() {
    const user = JSON.parse(localStorage.getItem('currentUser'));
    if (!user) {
        console.warn('⚠️ Chưa đăng nhập, không thể load notifications');
        return;
    }

    console.log('🔔 Khởi tạo notifications cho user:', user.user_id, 'role:', user.role);

    // Load notifications từ API
    loadNotifications(user.user_id);
    
    // Cập nhật badge
    updateNotificationBadge(user.user_id);
    
    // Khởi tạo WebSocket dựa trên role
    if (user.role === 'organizer' || user.role === 'manage') {
        // Organizer: kết nối để nhận thông báo feedback
        if (typeof initOrganizerWebSocket === 'function') {
            console.log('🔌 Khởi tạo WebSocket cho organizer...');
            setTimeout(() => {
                initOrganizerWebSocket();
            }, 1000);
        }
    } else {
        // User: kết nối để nhận thông báo reply từ organizer
        if (typeof initUserWebSocket === 'function') {
            console.log('🔌 Khởi tạo WebSocket cho user...');
            setTimeout(() => {
                initUserWebSocket();
            }, 1000);
        }
    }
}

/**
 * Load notifications từ API
 */
async function loadNotifications(userId) {
    if (!userId) {
        console.warn('⚠️ Không có userId để load notifications');
        return;
    }
    
    try {
        console.log('🔍 Đang load notifications cho user:', userId);
        const response = await fetch(`http://localhost:8080/api/notifications/user/${userId}`);
        
        if (!response.ok) {
            const errorText = await response.text();
            console.error('❌ HTTP Error:', response.status, errorText);
            throw new Error(`HTTP ${response.status}: ${errorText}`);
        }
        
        allNotifications = await response.json();
        console.log('📋 Loaded notifications:', allNotifications);
        console.log('📊 Tổng số notifications:', allNotifications ? allNotifications.length : 0);
        
        // Render notifications
        renderNotifications();
        
        // Cập nhật badge
        updateNotificationBadge(userId);
        
    } catch (error) {
        console.error('❌ Lỗi load notifications:', error);
        const list = document.getElementById('notificationList');
        if (list) {
            list.innerHTML = `<div class="notification-empty">Không thể tải thông báo: ${error.message}</div>`;
        }
    }
}

/**
 * Render notifications vào panel
 */
function renderNotifications() {
    const list = document.getElementById('notificationList');
    if (!list) return;

    if (allNotifications.length === 0) {
        list.innerHTML = '<div class="notification-empty">Chưa có thông báo nào</div>';
        return;
    }

    // Sắp xếp: unread trước, mới nhất trước
    const sorted = [...allNotifications].sort((a, b) => {
        const aUnread = a.status === 'unread' ? 1 : 0;
        const bUnread = b.status === 'unread' ? 1 : 0;
        if (aUnread !== bUnread) return bUnread - aUnread;
        
        const aTime = new Date(a.createdAt || 0).getTime();
        const bTime = new Date(b.createdAt || 0).getTime();
        return bTime - aTime;
    });

    list.innerHTML = sorted.map(notif => {
        const isUnread = notif.status === 'unread';
        const timeAgo = formatTimeAgo(notif.createdAt);
        
        // Xác định type từ title/message nếu không có field type
        let type = notif.type;
        if (!type) {
            const title = (notif.title || '').toLowerCase();
            const message = (notif.message || '').toLowerCase();
            if (title.includes('đánh giá') || message.includes('đánh giá')) {
                type = 'feedback';
            } else if (title.includes('phản hồi') || message.includes('phản hồi') || message.includes('trả lời')) {
                type = 'feedback_reply';
            } else if (title.includes('đăng ký') || message.includes('đăng ký')) {
                type = 'registration';
            }
        }
        
        // Icon khác nhau tùy loại notification
        let icon = 'fa-bell';
        if (type === 'feedback') icon = 'fa-star';
        if (type === 'feedback_reply') icon = 'fa-reply';
        if (type === 'registration') icon = 'fa-user-plus';
        
        return `
            <div class="notification-item ${isUnread ? 'unread' : ''}" 
                 onclick="handleNotificationClick(${notif.notificationId}, ${notif.userId})">
                <div class="notification-item-icon">
                    <i class="fas ${icon}"></i>
                </div>
                <div class="notification-item-content">
                    <div class="notification-item-title">${escapeHtml(notif.title || 'Thông báo')}</div>
                    <div class="notification-item-message">${escapeHtml(notif.message || '')}</div>
                    <div class="notification-item-time">${timeAgo}</div>
                </div>
            </div>
        `;
    }).join('');
}

/**
 * Toggle notification panel
 */
window.toggleNotificationPanel = function() {
    const panel = document.getElementById('notificationPanel');
    if (!panel) return;

    panel.classList.toggle('active');
    
    // Load notifications nếu panel được mở lần đầu
    if (panel.classList.contains('active') && allNotifications.length === 0) {
        const user = JSON.parse(localStorage.getItem('currentUser'));
        if (user) {
            loadNotifications(user.user_id);
        }
    }
};

/**
 * Đóng notification panel khi click bên ngoài
 */
document.addEventListener('click', function(event) {
    const panel = document.getElementById('notificationPanel');
    const btn = document.getElementById('notificationBtn');
    
    if (panel && btn && !panel.contains(event.target) && !btn.contains(event.target)) {
        panel.classList.remove('active');
    }
});

/**
 * Xử lý khi click vào notification
 */
window.handleNotificationClick = async function(notificationId, userId) {
    // Đánh dấu đã đọc
    try {
        const response = await fetch(
            `http://localhost:8080/api/notifications/${notificationId}/read?userId=${userId}`,
            { method: 'PUT' }
        );
        
        if (response.ok) {
            // Cập nhật local state
            const notif = allNotifications.find(n => n.notificationId === notificationId);
            if (notif) {
                notif.status = 'read';
            }
            
            // Re-render
            renderNotifications();
            
            // Cập nhật badge
            updateNotificationBadge(userId);
            
            // Nếu là notification về feedback reply, có thể điều hướng tới trang feedback
            const notification = allNotifications.find(n => n.notificationId === notificationId);
            if (notification && notification.type === 'feedback_reply') {
                // Có thể thêm logic điều hướng ở đây
                // window.location.href = 'feedback.html';
            }
        }
    } catch (error) {
        console.error('❌ Lỗi đánh dấu đã đọc:', error);
    }
};

/**
 * Đánh dấu tất cả đã đọc
 */
window.markAllNotificationsAsRead = async function() {
    const user = JSON.parse(localStorage.getItem('currentUser'));
    if (!user) return;

    const unreadNotifications = allNotifications.filter(n => n.status === 'unread');
    
    // Đánh dấu từng cái một
    for (const notif of unreadNotifications) {
        try {
            await fetch(
                `http://localhost:8080/api/notifications/${notif.notificationId}/read?userId=${user.user_id}`,
                { method: 'PUT' }
            );
            notif.status = 'read';
        } catch (error) {
            console.error('❌ Lỗi đánh dấu đã đọc:', error);
        }
    }
    
    // Re-render
    renderNotifications();
    
    // Cập nhật badge
    updateNotificationBadge(user.user_id);
};

/**
 * Cập nhật notification badge
 */
async function updateNotificationBadge(userId) {
    if (!userId) {
        console.warn('⚠️ Không có userId để cập nhật badge');
        return;
    }
    
    try {
        console.log('🔍 Đang cập nhật badge cho user:', userId);
        const response = await fetch(`http://localhost:8080/api/notifications/user/${userId}/count`);
        
        if (!response.ok) {
            console.error('❌ HTTP Error khi cập nhật badge:', response.status);
            return;
        }
        
        const data = await response.json();
        unreadCount = data.count || 0;
        console.log('📊 Số notification chưa đọc:', unreadCount);
        
        const badge = document.getElementById('notification-badge');
        if (badge) {
            if (unreadCount > 0) {
                badge.textContent = unreadCount > 99 ? '99+' : unreadCount;
                badge.style.display = 'flex';
                console.log('✅ Badge đã cập nhật:', badge.textContent);
            } else {
                badge.textContent = '';
                badge.style.display = 'none';
                console.log('✅ Không có notification chưa đọc');
            }
        } else {
            console.warn('⚠️ Không tìm thấy element #notification-badge');
        }
    } catch (error) {
        console.error('❌ Lỗi cập nhật badge:', error);
    }
}

/**
 * Xem tất cả notifications
 */
window.viewAllNotifications = function() {
    // Có thể mở trang notifications riêng hoặc scroll trong panel
    console.log('Xem tất cả notifications');
    // TODO: Implement nếu cần
};

/**
 * Format time ago
 */
function formatTimeAgo(dateString) {
    if (!dateString) return '';
    
    const now = new Date();
    const date = new Date(dateString);
    const diffMs = now - date;
    const diffMins = Math.floor(diffMs / 60000);
    const diffHours = Math.floor(diffMs / 3600000);
    const diffDays = Math.floor(diffMs / 86400000);
    
    if (diffMins < 1) return 'Vừa xong';
    if (diffMins < 60) return `${diffMins} phút trước`;
    if (diffHours < 24) return `${diffHours} giờ trước`;
    if (diffDays < 7) return `${diffDays} ngày trước`;
    
    return date.toLocaleDateString('vi-VN');
}

/**
 * Escape HTML
 */
function escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

/**
 * Thêm notification mới vào list (khi nhận từ WebSocket)
 */
function addNotificationToList(notification) {
    console.log('📨 Nhận notification mới từ WebSocket:', notification);
    
    // Kiểm tra xem notification đã tồn tại chưa
    const exists = allNotifications.some(n => 
        n.notificationId === notification.notificationId
    );
    
    if (!exists) {
        // Thêm vào đầu list
        allNotifications.unshift(notification);
        console.log('✅ Đã thêm notification vào list. Tổng số:', allNotifications.length);
        
        // Re-render nếu panel đang mở
        const panel = document.getElementById('notificationPanel');
        if (panel && panel.classList.contains('active')) {
            renderNotifications();
        }
        
        // Cập nhật badge
        const user = JSON.parse(localStorage.getItem('currentUser'));
        if (user) {
            updateNotificationBadge(user.user_id);
        }
    } else {
        console.log('⚠️ Notification đã tồn tại, bỏ qua');
    }
}

// Override showNotificationToast từ websocket.js để thêm vào list
if (typeof window.showNotificationToast !== 'undefined') {
    const originalShowToast = window.showNotificationToast;
    window.showNotificationToast = function(notification) {
        // Gọi hàm gốc để hiển thị toast
        originalShowToast(notification);
        
        // Thêm vào notification list
        addNotificationToList(notification);
    };
}

// Khởi tạo khi DOM ready
document.addEventListener('DOMContentLoaded', function() {
    console.log('🔔 Initializing notifications system...');
    // Đợi một chút để đảm bảo các script khác đã load
    setTimeout(function() {
        initNotifications();
        
        // Debug: Kiểm tra xem các hàm có tồn tại không
        console.log('🔍 Debug - Functions available:');
        console.log('  - initNotifications:', typeof initNotifications);
        console.log('  - initUserWebSocket:', typeof initUserWebSocket);
        console.log('  - initOrganizerWebSocket:', typeof initOrganizerWebSocket);
        console.log('  - addNotificationToList:', typeof addNotificationToList);
        console.log('  - updateNotificationBadge:', typeof updateNotificationBadge);
    }, 500);
});

// Export để có thể gọi từ nơi khác
window.initNotifications = initNotifications;
window.loadNotifications = loadNotifications;
window.addNotificationToList = addNotificationToList;
window.updateNotificationBadge = updateNotificationBadge;

