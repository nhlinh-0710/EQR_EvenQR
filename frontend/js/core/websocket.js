// ======================================
// 🔔 WEBSOCKET CLIENT - REALTIME NOTIFICATIONS
// ======================================

// Sử dụng IIFE để tránh duplicate declaration
(function() {
    'use strict';
    
    // Kiểm tra xem đã được khai báo chưa
    if (typeof window.__websocketModule !== 'undefined') {
        console.warn('WebSocket module đã được load, sử dụng instance hiện có');
        return;
    }
    
    window.__websocketModule = true;
    
    let stompClient = null;
    let isConnected = false;

let subscriptions = []; // Lưu danh sách các subscriptions để có thể subscribe nhiều topic

/**
 * Kết nối WebSocket và subscribe vào topic
 * @param {number} userId - ID của user/organizer
 * @param {string} userType - 'organizer' hoặc 'user'
 * @param {function} onNotificationReceived - Callback khi nhận thông báo
 */
function connectWebSocket(userId, userType, onNotificationReceived) {
    if (isConnected && stompClient !== null) {
        console.log('WebSocket đã được kết nối, đang subscribe thêm topic...');
        // Nếu đã connected, chỉ cần subscribe thêm topic
        subscribeToTopic(userId, userType, onNotificationReceived);
        return;
    }

    // Kiểm tra xem SockJS có được load chưa
    if (typeof SockJS === 'undefined') {
        console.error('❌ SockJS library chưa được load. Vui lòng thêm script vào HTML.');
        console.error('   Đảm bảo đã thêm: <script src="https://cdn.jsdelivr.net/npm/sockjs-client@1/dist/sockjs.min.js"></script>');
        return;
    }
    
    // Kiểm tra xem Stomp có được load chưa
    // Stomp có thể là Stomp.StompClient (v7+) hoặc Stomp (v6-)
    let StompClient = null;
    if (typeof Stomp !== 'undefined') {
        // Stomp v7+ sử dụng Stomp.StompClient
        StompClient = Stomp.StompClient || Stomp.Client || Stomp;
        console.log('✅ Stomp library đã được load:', typeof Stomp);
    } else {
        console.error('❌ Stomp library chưa được load. Vui lòng thêm script vào HTML.');
        console.error('   Đảm bảo đã thêm: <script src="https://cdn.jsdelivr.net/npm/@stomp/stompjs@7/bundles/stomp.umd.min.js"></script>');
        return;
    }
    
    // Sử dụng SockJS và STOMP
    console.log('🔌 Đang tạo SockJS connection...');
    const socket = new SockJS('http://localhost:8080/ws');
    
    // Tạo Stomp client - Stomp.js v7 sử dụng API mới
    // Kiểm tra xem có phải Stomp v7 không
    if (typeof StompClient.Client !== 'undefined') {
        // Stomp v7+ với Client class
        console.log('📦 Sử dụng Stomp v7+ API');
        stompClient = new StompClient.Client({
            webSocketFactory: () => socket,
            debug: function(str) {
                // console.log('STOMP:', str); // Uncomment để debug
            },
            reconnectDelay: 5000,
            heartbeatIncoming: 4000,
            heartbeatOutgoing: 4000
        });
        
        // Connect với v7 API
        stompClient.onConnect = function(frame) {
            console.log('✅ Đã kết nối WebSocket thành công! (v7)');
            console.log('📋 Frame:', frame);
            isConnected = true;
            
            // Subscribe vào topic
            subscribeToTopic(userId, userType, onNotificationReceived);
        };
        
        stompClient.onStompError = function(frame) {
            console.error('❌ STOMP error:', frame);
        };
        
        stompClient.onWebSocketError = function(error) {
            console.error('❌ WebSocket error:', error);
            isConnected = false;
            // Thử kết nối lại sau 5 giây
            setTimeout(function() {
                console.log('🔄 Đang thử kết nối lại WebSocket...');
                connectWebSocket(userId, userType, onNotificationReceived);
            }, 5000);
        };
        
        // Kích hoạt kết nối
        stompClient.activate();
        
    } else if (typeof StompClient.over !== 'undefined') {
        // Stomp v6- với Stomp.over()
        console.log('📦 Sử dụng Stomp v6- API');
        stompClient = StompClient.over(socket);
        
        // Disable debug logging
        stompClient.debug = function(str) {
            // console.log('STOMP:', str); // Uncomment để debug
        };

        stompClient.connect({}, function(frame) {
            console.log('✅ Đã kết nối WebSocket thành công! (v6)');
            console.log('📋 Frame:', frame);
            isConnected = true;

            // Subscribe vào topic
            subscribeToTopic(userId, userType, onNotificationReceived);

        }, function(error) {
            console.error('❌ Lỗi kết nối WebSocket:', error);
            console.error('💡 Kiểm tra:');
            console.error('   1. Backend có đang chạy không? (http://localhost:8080)');
            console.error('   2. WebSocket endpoint có đúng không? (/ws)');
            console.error('   3. CORS có được cấu hình đúng không?');
            console.error('   4. Xem Network tab để kiểm tra request details');
            isConnected = false;
            
            // Thử kết nối lại sau 5 giây
            setTimeout(function() {
                console.log('🔄 Đang thử kết nối lại WebSocket...');
                connectWebSocket(userId, userType, onNotificationReceived);
            }, 5000);
        });
    } else {
        console.error('❌ Không thể khởi tạo Stomp client. API không được hỗ trợ.');
        return;
    }
}

/**
 * Subscribe vào một topic cụ thể
 */
function subscribeToTopic(userId, userType, onNotificationReceived) {
    if (!isConnected || !stompClient) {
        console.error('❌ WebSocket chưa kết nối');
        return;
    }

    const topic = userType === 'organizer' 
        ? `/topic/organizer/${userId}`
        : `/topic/user/${userId}`;
    
    console.log('📡 Đang subscribe vào:', topic);
    
    // Subscribe - API giống nhau cho cả v6 và v7
    try {
        const subscription = stompClient.subscribe(topic, function(message) {
            try {
                // V7: message.body, v6: message.body hoặc message
                const body = message.body || message;
                const notification = typeof body === 'string' ? JSON.parse(body) : body;
                console.log('🔔 Nhận thông báo mới:', notification);
                
                // Gọi callback để xử lý thông báo
                if (onNotificationReceived && typeof onNotificationReceived === 'function') {
                    onNotificationReceived(notification);
                } else {
                    // Fallback: hiển thị toast nếu không có callback
                    showNotificationToast(notification);
                }
            } catch (error) {
                console.error('❌ Lỗi khi parse thông báo:', error);
                console.error('Raw message:', message);
            }
        });

        // Lưu subscription để có thể unsubscribe sau
        subscriptions.push({ topic, subscription });
        
        console.log('✅ Đã subscribe thành công vào topic:', topic);
    } catch (error) {
        console.error('❌ Lỗi khi subscribe vào topic:', error);
    }
}

/**
 * Kết nối WebSocket cho organizer (backward compatibility)
 */
function connectOrganizerWebSocket(organizerId, onNotificationReceived) {
    connectWebSocket(organizerId, 'organizer', onNotificationReceived);
}

/**
 * Kết nối WebSocket cho user
 */
function connectUserWebSocket(userId, onNotificationReceived) {
    connectWebSocket(userId, 'user', onNotificationReceived);
}

/**
 * Ngắt kết nối WebSocket
 */
function disconnectWebSocket() {
    if (stompClient !== null) {
        stompClient.disconnect(function() {
            console.log('🔌 Đã ngắt kết nối WebSocket');
        });
        stompClient = null;
        isConnected = false;
    }
}

/**
 * Hiển thị thông báo dạng toast notification
 * @param {object} notification - Đối tượng thông báo từ server
 */
function showNotificationToast(notification) {
    // Sử dụng hàm showNotification từ main.js nếu có
    if (typeof showNotification === 'function') {
        showNotification(notification.message || notification.title || 'Có thông báo mới', 'info');
    } else {
        // Tạo toast notification riêng nếu không có hàm showNotification
        createToastNotification(notification);
    }
    
    // Cập nhật badge số thông báo chưa đọc
    if (notification.userId) {
        updateNotificationBadge(notification.userId);
    }
    
    // Log để debug
    console.log('🔔 Thông báo WebSocket:', notification);
}

/**
 * Tạo toast notification tùy chỉnh
 * @param {object} notification - Đối tượng thông báo
 */
function createToastNotification(notification) {
    // Xóa các notification cũ
    const existingToasts = document.querySelectorAll('.websocket-toast');
    existingToasts.forEach(toast => {
        toast.style.animation = 'slideOutRight 0.3s ease-in forwards';
        setTimeout(() => toast.remove(), 300);
    });

    // Tạo element toast mới
    const toast = document.createElement('div');
    toast.className = 'websocket-toast';
    toast.innerHTML = `
        <div class="toast-content">
            <div class="toast-icon">
                <i class="fas fa-bell"></i>
            </div>
            <div class="toast-message">
                <div class="toast-title">${notification.title || 'Thông báo mới'}</div>
                <div class="toast-text">${notification.message || ''}</div>
            </div>
            <button class="toast-close" onclick="this.closest('.websocket-toast').remove()">
                <i class="fas fa-times"></i>
            </button>
        </div>
    `;

    // Thêm styles
    toast.style.cssText = `
        position: fixed;
        top: 20px;
        right: 20px;
        z-index: 10000;
        min-width: 320px;
        max-width: 400px;
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        border-radius: 12px;
        box-shadow: 0 10px 30px rgba(0, 0, 0, 0.2);
        animation: slideInRight 0.4s ease-out;
        overflow: hidden;
    `;

    const content = toast.querySelector('.toast-content');
    content.style.cssText = `
        display: flex;
        align-items: center;
        gap: 15px;
        padding: 16px 20px;
        color: white;
    `;

    const icon = toast.querySelector('.toast-icon');
    icon.style.cssText = `
        font-size: 24px;
        color: #ffd700;
    `;

    const message = toast.querySelector('.toast-message');
    message.style.cssText = `
        flex: 1;
    `;

    const title = toast.querySelector('.toast-title');
    title.style.cssText = `
        font-weight: 600;
        font-size: 14px;
        margin-bottom: 4px;
    `;

    const text = toast.querySelector('.toast-text');
    text.style.cssText = `
        font-size: 12px;
        opacity: 0.9;
        line-height: 1.4;
    `;

    const closeBtn = toast.querySelector('.toast-close');
    closeBtn.style.cssText = `
        background: rgba(255, 255, 255, 0.2);
        border: none;
        border-radius: 50%;
        width: 28px;
        height: 28px;
        cursor: pointer;
        color: white;
        display: flex;
        align-items: center;
        justify-content: center;
        transition: background 0.2s;
    `;

    closeBtn.addEventListener('mouseenter', function() {
        this.style.background = 'rgba(255, 255, 255, 0.3)';
    });

    closeBtn.addEventListener('mouseleave', function() {
        this.style.background = 'rgba(255, 255, 255, 0.2)';
    });

    // Thêm CSS animations nếu chưa có
    if (!document.getElementById('websocket-toast-styles')) {
        const style = document.createElement('style');
        style.id = 'websocket-toast-styles';
        style.textContent = `
            @keyframes slideInRight {
                from {
                    opacity: 0;
                    transform: translateX(100%);
                }
                to {
                    opacity: 1;
                    transform: translateX(0);
                }
            }
            
            @keyframes slideOutRight {
                from {
                    opacity: 1;
                    transform: translateX(0);
                }
                to {
                    opacity: 0;
                    transform: translateX(100%);
                }
            }
        `;
        document.head.appendChild(style);
    }

    document.body.appendChild(toast);

    // Tự động xóa sau 6 giây
    setTimeout(() => {
        if (toast.parentElement) {
            toast.style.animation = 'slideOutRight 0.3s ease-in forwards';
            setTimeout(() => toast.remove(), 300);
        }
    }, 6000);
}

/**
 * Cập nhật badge số thông báo chưa đọc
 * @param {number} userId - ID của user
 */
async function updateNotificationBadge(userId) {
    try {
        const response = await fetch(`http://localhost:8080/api/notifications/user/${userId}/count`);
        if (response.ok) {
            const data = await response.json();
            const badge = document.getElementById('notification-badge');
            if (badge) {
                const count = data.count || 0;
                if (count > 0) {
                    badge.textContent = count > 99 ? '99+' : count;
                    badge.style.display = 'flex';
                } else {
                    badge.textContent = '';
                    badge.style.display = 'none';
                }
            }
            
            // Gọi hàm updateNotificationBadge từ notifications.js nếu có
            if (typeof window.updateNotificationBadge === 'function') {
                window.updateNotificationBadge(userId);
            }
        }
    } catch (error) {
        console.error('Lỗi khi cập nhật badge:', error);
    }
}

/**
 * Khởi tạo WebSocket cho organizer
 * Gọi hàm này khi trang admin/organizer được load
 */
function initOrganizerWebSocket() {
    const user = JSON.parse(localStorage.getItem('currentUser'));
    if (!user) {
        console.warn('⚠️ Chưa đăng nhập, không thể kết nối WebSocket');
        return;
    }

    // Chỉ kết nối nếu user là organizer
    if (user.role === 'organizer' || user.role === 'manage') {
        const organizerId = user.user_id;
        console.log('🔌 Đang kết nối WebSocket cho organizer:', organizerId);
        
        connectWebSocket(organizerId, 'organizer', function(notification) {
            // Hiển thị thông báo toast
            showNotificationToast(notification);
            
            // Thêm vào notification list nếu có hàm addNotificationToList
            if (typeof addNotificationToList === 'function') {
                addNotificationToList(notification);
            }
            
            // Cập nhật badge
            updateNotificationBadge(organizerId);
            
            // Có thể thêm logic khác như reload danh sách sự kiện, v.v.
            // Ví dụ: reloadEventsList();
        });
    }
}

/**
 * Khởi tạo WebSocket cho user
 * Gọi hàm này khi trang user được load
 */
function initUserWebSocket() {
    const user = JSON.parse(localStorage.getItem('currentUser'));
    if (!user) {
        console.warn('⚠️ Chưa đăng nhập, không thể kết nối WebSocket');
        return;
    }

    // Kết nối cho tất cả user (kể cả organizer nếu họ đang ở trang user)
    const userId = user.user_id;
    console.log('🔌 Đang kết nối WebSocket cho user:', userId);
    
    connectWebSocket(userId, 'user', function(notification) {
        // Hiển thị thông báo toast
        showNotificationToast(notification);
        
        // Thêm vào notification list nếu có hàm addNotificationToList
        if (typeof addNotificationToList === 'function') {
            addNotificationToList(notification);
        }
        
        // Cập nhật badge
        updateNotificationBadge(userId);
        
        // Nếu là notification về feedback reply, reload feedbacks list
        if (notification.type === 'feedback_reply') {
            // Reload feedbacks list nếu đang ở trang feedback
            if (typeof loadUserFeedbacks === 'function') {
                setTimeout(loadUserFeedbacks, 500);
            }
        }
    });
}

/**
 * Khởi tạo WebSocket dựa trên role của user
 */
function initWebSocket() {
    const user = JSON.parse(localStorage.getItem('currentUser'));
    if (!user) {
        console.warn('⚠️ Chưa đăng nhập, không thể kết nối WebSocket');
        return;
    }

    // Kết nối cho organizer nếu là organizer
    if (user.role === 'organizer' || user.role === 'manage') {
        initOrganizerWebSocket();
    }
    
    // Luôn kết nối cho user để nhận notification về feedback reply
    initUserWebSocket();
}

// Tự động kết nối khi trang được load (nếu đã có SockJS và Stomp)
function checkAndInitWebSocket() {
    // Kiểm tra lại sau khi DOM ready
    if (typeof SockJS !== 'undefined' && typeof Stomp !== 'undefined') {
        // Chờ một chút để đảm bảo các script khác đã load xong
        setTimeout(initWebSocket, 500);
    } else {
        // Thử lại sau 500ms nếu chưa load
        setTimeout(checkAndInitWebSocket, 500);
    }
}

// Khởi tạo WebSocket sau khi DOM ready và libraries đã load
(function() {
    let retryCount = 0;
    const maxRetries = 30; // Tối đa 15 giây (30 * 500ms) - tăng thời gian chờ
    
    function waitForLibraries() {
        // Kiểm tra xem SockJS và Stomp đã load chưa
        // Stomp có thể là Stomp.StompClient hoặc Stomp (tùy version)
        const hasSockJS = typeof SockJS !== 'undefined';
        const hasStomp = typeof Stomp !== 'undefined' || (typeof window !== 'undefined' && window.Stomp);
        
        if (hasSockJS && hasStomp) {
            console.log('✅ SockJS và Stomp đã sẵn sàng');
            console.log('  - SockJS:', typeof SockJS);
            console.log('  - Stomp:', typeof Stomp !== 'undefined' ? typeof Stomp : (window.Stomp ? typeof window.Stomp : 'not found'));
            setTimeout(initWebSocket, 500);
        } else {
            retryCount++;
            if (retryCount >= maxRetries) {
                console.error('❌ Không thể load SockJS/Stomp sau ' + maxRetries + ' lần thử');
                console.error('💡 Hãy kiểm tra:');
                console.error('   1. Internet connection');
                console.error('   2. CDN có accessible không (thử mở link trong browser)');
                console.error('   3. Script tags trong HTML có đúng không');
                console.error('   4. Network tab trong DevTools có lỗi không');
                console.error('💡 Hoặc tải libraries về local và host trực tiếp');
                return;
            }
            
            // Chỉ log mỗi 5 lần để tránh spam
            if (retryCount % 5 === 0 || retryCount <= 3) {
                console.log('⏳ Đang đợi SockJS/Stomp load... (' + retryCount + '/' + maxRetries + ')');
                console.log('  - SockJS:', hasSockJS ? '✅' : '❌');
                console.log('  - Stomp:', hasStomp ? '✅' : '❌');
            }
            
            // Nếu chưa có, đợi thêm
            setTimeout(waitForLibraries, 500);
        }
    }
    
    // Bắt đầu đợi khi DOM ready hoặc ngay lập tức nếu DOM đã ready
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', function() {
            setTimeout(waitForLibraries, 500); // Đợi 500ms để scripts có thời gian load
        });
    } else {
        // DOM đã ready, đợi một chút để scripts load xong
        setTimeout(waitForLibraries, 500);
    }
})();

    // Export các hàm cần thiết ra global scope
    window.connectWebSocket = connectWebSocket;
    window.connectOrganizerWebSocket = connectOrganizerWebSocket;
    window.connectUserWebSocket = connectUserWebSocket;
    window.disconnectWebSocket = disconnectWebSocket;
    window.initOrganizerWebSocket = initOrganizerWebSocket;
    window.initUserWebSocket = initUserWebSocket;
    window.initWebSocket = initWebSocket;
    window.updateNotificationBadge = updateNotificationBadge;
    window.showNotificationToast = showNotificationToast;
    
})(); // Đóng IIFE
