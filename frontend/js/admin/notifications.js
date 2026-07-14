// ======================================
// 🔔 NOTIFICATION MANAGEMENT
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

    // Load notifications từ API
    loadNotifications(user.user_id);
    
    // Cập nhật badge
    updateNotificationBadge(user.user_id);
    
    // Khởi tạo WebSocket nếu là organizer
    if (user.role === 'organizer' || user.role === 'manage') {
        // WebSocket sẽ được khởi tạo tự động bởi websocket.js
        // Nhưng chúng ta cần đảm bảo callback được set đúng
        if (typeof initOrganizerWebSocket === 'function') {
            // Override callback để cập nhật UI khi nhận thông báo
            const originalInit = initOrganizerWebSocket;
            // Không cần override, websocket.js đã có showNotificationToast
        }
    }
}

/**
 * Load notifications từ API
 */
async function loadNotifications(userId) {
    try {
        const response = await fetch(`http://localhost:8080/api/notifications/user/${userId}`);
        if (!response.ok) {
            throw new Error('Không thể tải thông báo');
        }
        
        allNotifications = await response.json();
        console.log('📋 Loaded notifications:', allNotifications);
        
        // Render notifications
        renderNotifications();
        
    } catch (error) {
        console.error('❌ Lỗi load notifications:', error);
        const list = document.getElementById('notificationList');
        if (list) {
            list.innerHTML = '<div class="notification-empty">Không thể tải thông báo</div>';
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
        
        return `
            <div class="notification-item ${isUnread ? 'unread' : ''}" 
                 onclick="handleNotificationClick(${notif.notificationId}, ${notif.userId})">
                <div class="notification-item-icon">
                    <i class="fas fa-bell"></i>
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
function toggleNotificationPanel() {
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
}

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
async function handleNotificationClick(notificationId, userId) {
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
        }
    } catch (error) {
        console.error('❌ Lỗi đánh dấu đã đọc:', error);
    }
}

/**
 * Đánh dấu tất cả đã đọc
 */
async function markAllNotificationsAsRead() {
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
}

/**
 * Cập nhật notification badge
 */
async function updateNotificationBadge(userId) {
    try {
        const response = await fetch(`http://localhost:8080/api/notifications/user/${userId}/count`);
        if (response.ok) {
            const data = await response.json();
            unreadCount = data.count || 0;
            
            const badge = document.getElementById('notification-badge');
            if (badge) {
                if (unreadCount > 0) {
                    badge.textContent = unreadCount > 99 ? '99+' : unreadCount;
                    badge.style.display = 'flex';
                } else {
                    badge.textContent = '';
                    badge.style.display = 'none';
                }
            }
        }
    } catch (error) {
        console.error('❌ Lỗi cập nhật badge:', error);
    }
}

/**
 * Xem tất cả notifications
 */
function viewAllNotifications() {
    // Có thể mở trang notifications riêng hoặc scroll trong panel
    console.log('Xem tất cả notifications');
    // TODO: Implement nếu cần
}

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
    // Thêm vào đầu list
    allNotifications.unshift(notification);
    
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
}

// Override showNotificationToast từ websocket.js để thêm vào list
if (typeof showNotificationToast !== 'undefined') {
    const originalShowToast = showNotificationToast;
    window.showNotificationToast = function(notification) {
        // Gọi hàm gốc để hiển thị toast
        originalShowToast(notification);
        
        // Thêm vào notification list
        addNotificationToList(notification);
    };
}

// Khởi tạo khi DOM ready
document.addEventListener('DOMContentLoaded', function() {
    // Đợi một chút để đảm bảo các script khác đã load
    setTimeout(initNotifications, 500);
});

