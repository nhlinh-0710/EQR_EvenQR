// Dashboard JavaScript
document.addEventListener('DOMContentLoaded', function() {
    console.log('Dashboard loading...');
    
    // Check authentication (will redirect if not logged in)
    const userData = initAuth();
    if (!userData) return; // Stop if redirected
    
    // Initialize dashboard
    initializeDashboard();
    
    // Set up event listeners
    setupEventListeners();
    
    // Load user data
    loadUserData();
    
    // Load dashboard statistics
    loadDashboardStatistics();
    
    // Load recent events
    loadRecentEvents();
    
    // Apply role-based menu visibility
    applyRoleBasedMenu(userData.role);
    
    console.log('Dashboard loaded successfully');
});

// Check if user is authenticated (DEPRECATED - use initAuth from auth.js instead)
function checkAuthentication() {
    const userData = getCurrentUser();
    if (!userData) {
        redirectToHome();
        return;
    }
    console.log('User authenticated:', userData);
    applyRoleBasedMenu(userData.role);
}

// Apply role-based menu visibility
function applyRoleBasedMenu(role) {
    const menuItems = document.querySelectorAll('.menu-item');

    // Define menu items for each role
    const manageMenuItems = ['dashboard', 'events', 'participants', 'analytics', 'profile', 'feedback'];
    const userMenuItems = ['suggest-events', 'create-event', 'my-tickets', 'qr-checkin', 'feedback'];

    const allowedItems = role === 'manage' ? manageMenuItems : userMenuItems;

    menuItems.forEach(item => {
        const section = item.getAttribute('data-section');
        if (allowedItems.includes(section)) {
            item.style.display = 'flex';
        } else {
            item.style.display = 'none';
        }
    });
}

// Initialize dashboard functionality
function initializeDashboard() {
    // Set active section
    const hash = window.location.hash.substring(1) || 'dashboard';
    switchSection(hash);

    // Update page title
    updatePageTitle(hash);
}

// Set up all event listeners
function setupEventListeners() {
    // Sidebar menu items
    const menuItems = document.querySelectorAll('.menu-item');
    menuItems.forEach(item => {
        item.addEventListener('click', function(e) {
            // e.preventDefault();
            const section = this.getAttribute('data-section');
            switchSection(section);
            updateActiveMenuItem(this);
            updatePageTitle(section);
        });
    });
    
    // Mobile menu toggle
    const mobileMenuBtn = document.getElementById('mobileMenuBtn');
    const sidebar = document.querySelector('.sidebar');
    
    if (mobileMenuBtn) {
        mobileMenuBtn.addEventListener('click', function() {
            sidebar.classList.toggle('active');
        });
    }
    
    // Close sidebar when clicking outside on mobile
    document.addEventListener('click', function(e) {
        if (window.innerWidth <= 768) {
            if (!sidebar.contains(e.target) && !mobileMenuBtn.contains(e.target)) {
                sidebar.classList.remove('active');
            }
        }
    });
    
    // Filter tabs
    const filterTabs = document.querySelectorAll('.filter-tab');
    filterTabs.forEach(tab => {
        tab.addEventListener('click', function() {
            // Remove active class from all tabs
            filterTabs.forEach(t => t.classList.remove('active'));
            // Add active class to clicked tab
            this.classList.add('active');
            
            // Filter events (would implement actual filtering logic here)
            const filter = this.getAttribute('data-filter');
            filterEvents(filter);
        });
    });
    
    // Create event form
    const createEventForm = document.getElementById('createEventForm');
    if (createEventForm) {
        createEventForm.addEventListener('submit', function(e) {
            e.preventDefault();
            handleCreateEvent();
        });
    }
    
    // File upload handling
    const fileUpload = document.querySelector('.file-upload');
    const fileInput = document.getElementById('eventImage');
    
    if (fileUpload && fileInput) {
        fileUpload.addEventListener('click', function() {
            fileInput.click();
        });
        
        fileInput.addEventListener('change', function(e) {
            const file = e.target.files[0];
            if (file) {
                handleFileUpload(file);
            }
        });
        
        // Drag and drop
        fileUpload.addEventListener('dragover', function(e) {
            e.preventDefault();
            this.style.backgroundColor = '#f0f7ff';
        });
        
        fileUpload.addEventListener('dragleave', function(e) {
            e.preventDefault();
            this.style.backgroundColor = '';
        });
        
        fileUpload.addEventListener('drop', function(e) {
            e.preventDefault();
            this.style.backgroundColor = '';
            const file = e.dataTransfer.files[0];
            if (file && file.type.startsWith('image/')) {
                handleFileUpload(file);
            }
        });
    }
    
    // Logout button event listener
    const logoutBtn = document.querySelector('.logout-btn');
    if (logoutBtn) {
        // Remove onclick attribute to avoid double firing
        logoutBtn.removeAttribute('onclick');
        logoutBtn.addEventListener('click', function(e) {
            e.preventDefault();
            handleLogout();
        });
        console.log('Logout button event listener attached successfully');
    } else {
        console.warn('Logout button not found');
    }
}

// Load and display user data
function loadUserData() {
    const currentUser = localStorage.getItem('currentUser');
    if (currentUser) {
        const userData = JSON.parse(currentUser);
        
        // Update user name in header
        const userNameElement = document.getElementById('userName');
        if (userNameElement) {
            userNameElement.textContent = userData.name || 'Người dùng';
        }
        
        // Update user avatar with first letter of name
        const userAvatarElement = document.getElementById('userAvatar');
        if (userAvatarElement) {
            const firstLetter = (userData.name || 'U').charAt(0).toUpperCase();
            userAvatarElement.textContent = firstLetter;
        }
    }
}

// Switch between different sections
function switchSection(sectionName) {
    console.log('🔄 Switching to section:', sectionName);
    
    // Hide all sections
    const sections = document.querySelectorAll('.content-section');
    sections.forEach(section => {
        section.classList.remove('active');
        section.style.display = 'none'; // Ẩn tất cả sections
    });
    
    // Show selected section
    const targetSection = document.getElementById(sectionName + '-section');
    if (targetSection) {
        targetSection.classList.add('active');
        targetSection.style.display = 'block'; // Hiển thị section được chọn
        console.log('✅ Đã hiển thị section:', sectionName);
    } else {
        console.warn('⚠️ Không tìm thấy section:', sectionName + '-section');
    }
    
    // Update URL hash
    window.history.pushState(null, null, '#' + sectionName);

    // ===================================================
    // <<< LOGIC KÍCH HOẠT TẢI SỰ KIỆN ĐÃ THÊM VÀO ĐÂY >>>
    // ===================================================
    if (sectionName === 'events') {
        console.log('📅 Đang khởi tạo trang events...');
        // Kiểm tra xem hàm initEventsPage đã được load từ events.js chưa.
        // Đây là bước quan trọng để đảm bảo logic chỉ chạy trên trang sự kiện.
        if (typeof initEventsPage === 'function') {
            // Đợi một chút để đảm bảo DOM đã render xong
            setTimeout(() => {
                console.log('🚀 Gọi initEventsPage()...');
                initEventsPage(); // Lệnh gọi hàm đã được sửa trong events.js
            }, 200);
        } else {
            console.error('❌ Hàm initEventsPage không tồn tại. Kiểm tra xem events.js đã được load chưa.');
            // Thử load lại sau 500ms
            setTimeout(() => {
                if (typeof initEventsPage === 'function') {
                    console.log('✅ Đã tìm thấy initEventsPage sau khi retry');
                    setTimeout(() => initEventsPage(), 200);
                } else {
                    console.error('❌ Vẫn không tìm thấy initEventsPage sau retry');
                }
            }, 500);
        }
    }
}

// Update active menu item
function updateActiveMenuItem(clickedItem) {
    // Remove active class from all menu items
    const menuItems = document.querySelectorAll('.menu-item');
    menuItems.forEach(item => {
        item.classList.remove('active');
    });
    
    // Add active class to clicked item
    clickedItem.classList.add('active');
}

// Update page title
function updatePageTitle(section) {
    const pageTitle = document.getElementById('pageTitle');
    if (!pageTitle) return;
    
    // ⚠️ Check nếu title trong HTML đã khác "Dashboard", KHÔNG override
    // Nghĩa là đang ở trang riêng (events.html, profile.html...) đã có title sẵn
    const currentTitle = pageTitle.textContent.trim();
    const protectedTitles = ['Sự Kiện của tôi', 'Tạo Sự Kiện', 'QR Check-in', 'Thống Kê', 'Feedback', 'Hồ Sơ'];
    
    if (protectedTitles.includes(currentTitle)) {
        console.log('✅ Giữ nguyên title:', currentTitle);
        return; // Không thay đổi title
    }
    
    // Chỉ update khi ở trang dashboard index.html (multi-section)
    const titles = {
        'dashboard': 'Dashboard',
        'events': 'Quản Lý Sự Kiện',
        'create-event': 'Tạo Sự Kiện Mới',
        'my-tickets': 'Vé của tôi',
        'participants': 'Quản Lý Người Tham Dự',
        'analytics': 'Thống Kê & Báo Cáo',
        'profile': 'Thông Tin Cá Nhân',
        'qr-checkin': 'QR Check-in',
        'suggest-events': 'Gợi ý Sự Kiện',
        'feedback': 'Feedback'
    };

    pageTitle.textContent = titles[section] || 'Dashboard';
}

// Filter events based on status
function filterEvents(filter) {
    // This would implement actual event filtering logic
    console.log('Filtering events by:', filter);
    
    // For demo purposes, just show a notification
    showNotification(`Đang lọc sự kiện: ${getFilterName(filter)}`, 'info');
}

function getFilterName(filter) {
    const names = {
        'all': 'Tất cả',
        'upcoming': 'Sắp diễn ra',
        'ongoing': 'Đang diễn ra',
        'completed': 'Đã hoàn thành'
    };
    return names[filter] || 'Tất cả';
}

// Handle create event form submission
async function handleCreateEvent() {
    // Lấy organizerId từ user hiện tại
    const user = JSON.parse(localStorage.getItem('currentUser'));
    if (!user || !user.user_id) {
        showNotification('Vui lòng đăng nhập để tạo sự kiện!', 'error');
        return;
    }
    
    const organizerId = user.user_id;
    
    // Get form data
    const form = document.getElementById('createEventForm');
    if (!form) {
        showNotification('Không tìm thấy form tạo sự kiện', 'error');
        return;
    }
    
    const formData = new FormData();
    formData.append('title', document.getElementById('eventTitle').value);
    formData.append('category', document.getElementById('eventCategory').value);
    formData.append('eventDate', document.getElementById('eventDate').value);
    formData.append('duration', document.getElementById('eventDuration').value);
    formData.append('location', document.getElementById('eventLocation').value);
    formData.append('maxParticipants', document.getElementById('maxParticipants').value);
    formData.append('description', document.getElementById('eventDescription').value);
    formData.append('organizerId', organizerId);
    
    // Validate required fields
    if (!formData.get('title') || !formData.get('eventDate') || !formData.get('location')) {
        showNotification('Vui lòng điền đầy đủ thông tin bắt buộc', 'error');
        return;
    }
    
    // Validate date (must be in future)
    const eventDate = new Date(formData.get('eventDate'));
    const now = new Date();
    if (eventDate <= now) {
        showNotification('Ngày sự kiện phải trong tương lai', 'error');
        return;
    }
    
    // Handle image upload if exists
    const imageInput = document.getElementById('eventImage');
    if (imageInput && imageInput.files.length > 0) {
        formData.append('eventImage', imageInput.files[0]);
    }
    
    // Show loading
    showNotification('Đang tạo sự kiện...', 'info');
    
    try {
        const response = await fetch('http://localhost:8080/api/events', {
            method: 'POST',
            headers: {
                'X-Organizer-Id': organizerId.toString()
            },
            body: formData
        });
        
        if (response.ok) {
            showNotification('Sự kiện đã được tạo thành công!', 'success');
            
            // Reset form
            form.reset();
            
            // Switch to events section
            setTimeout(() => {
                switchSection('events');
                // Reload events if there's a function to do so
                if (typeof fetchEvents === 'function') {
                    fetchEvents();
                }
            }, 1500);
        } else {
            const errorText = await response.text();
            showNotification(`Lỗi tạo sự kiện: ${errorText}`, 'error');
            console.error('Lỗi tạo sự kiện:', errorText);
        }
    } catch (error) {
        console.error('Lỗi Network hoặc Server:', error);
        showNotification('Đã xảy ra lỗi khi kết nối đến server.', 'error');
    }
}

// Handle file upload
function handleFileUpload(file) {
    const fileUploadArea = document.querySelector('.file-upload-area');
    
    // Show preview
    const reader = new FileReader();
    reader.onload = function(e) {
        fileUploadArea.innerHTML = `
            <img src="${e.target.result}" alt="Preview" style="max-width: 200px; max-height: 150px; border-radius: 8px;">
            <p>Hình ảnh đã được chọn: ${file.name}</p>
        `;
    };
    reader.readAsDataURL(file);
    
    showNotification('Hình ảnh đã được tải lên', 'success');
}

// Handle logout
function handleLogout() {
    if (confirm('Bạn có chắc chắn muốn đăng xuất?')) {
        try {
            // Clear user data
            localStorage.removeItem('currentUser');
            localStorage.removeItem('userEvents');
            localStorage.removeItem('authToken');
            sessionStorage.clear();
            
            // Show notification if function exists
            if (typeof showNotification === 'function') {
                showNotification('Đã đăng xuất thành công', 'success');
            }
            
            // Redirect to home page (../../index.html for pages/admin/* or pages/user/*)
            setTimeout(() => {
                window.location.href = '../../index.html';
            }, 500);
        } catch (error) {
            console.error('Logout error:', error);
            // Force redirect even if there's an error
            window.location.href = '../../index.html';
        }
    }
}

// Utility functions
function generateId() {
    return Date.now().toString(36) + Math.random().toString(36).substr(2);
}

// Notification system (reused from main.js)
function showNotification(message, type = 'info') {
    // Remove existing notifications
    const existingNotifications = document.querySelectorAll('.notification');
    existingNotifications.forEach(notification => notification.remove());

    // Create notification element
    const notification = document.createElement('div');
    notification.className = `notification notification-${type}`;
    notification.innerHTML = `
        <div class="notification-content">
            <i class="fas ${getNotificationIcon(type)}"></i>
            <span>${message}</span>
            <button class="notification-close" onclick="this.parentElement.parentElement.remove()">
                <i class="fas fa-times"></i>
            </button>
        </div>
    `;

    // Add styles
    notification.style.cssText = `
        position: fixed;
        top: 20px;
        right: 20px;
        z-index: 10000;
        min-width: 300px;
        max-width: 500px;
        background: white;
        border-radius: 8px;
        box-shadow: 0 10px 25px rgba(0, 0, 0, 0.15);
        border-left: 4px solid ${getNotificationColor(type)};
        animation: slideInRight 0.3s ease-out;
    `;

    const content = notification.querySelector('.notification-content');
    content.style.cssText = `
        display: flex;
        align-items: center;
        gap: 10px;
        padding: 15px 20px;
        color: #333;
    `;

    const icon = notification.querySelector('i');
    icon.style.color = getNotificationColor(type);

    const closeBtn = notification.querySelector('.notification-close');
    closeBtn.style.cssText = `
        margin-left: auto;
        background: none;
        border: none;
        cursor: pointer;
        padding: 5px;
        border-radius: 4px;
        color: #666;
    `;

    document.body.appendChild(notification);

    // Auto remove after 5 seconds
    setTimeout(() => {
        if (notification.parentElement) {
            notification.style.animation = 'slideOutRight 0.3s ease-in forwards';
            setTimeout(() => notification.remove(), 300);
        }
    }, 5000);
}

function getNotificationIcon(type) {
    switch (type) {
        case 'success': return 'fa-check-circle';
        case 'error': return 'fa-exclamation-circle';
        case 'warning': return 'fa-exclamation-triangle';
        default: return 'fa-info-circle';
    }
}

function getNotificationColor(type) {
    switch (type) {
        case 'success': return '#10b981';
        case 'error': return '#ef4444';
        case 'warning': return '#f59e0b';
        default: return '#3b82f6';
    }
}

// Handle window resize
window.addEventListener('resize', function() {
    const sidebar = document.querySelector('.sidebar');
    if (window.innerWidth > 768) {
        sidebar.classList.remove('active');
    }
});

// Handle back/forward browser buttons
window.addEventListener('popstate', function() {
    const hash = window.location.hash.substring(1) || 'dashboard';
    switchSection(hash);
    updatePageTitle(hash);
    
    // Update active menu item
    const menuItems = document.querySelectorAll('.menu-item');
    menuItems.forEach(item => {
        item.classList.remove('active');
        if (item.getAttribute('data-section') === hash) {
            item.classList.add('active');
        }
    });
});

// ===== DASHBOARD STATISTICS FUNCTIONS =====

/**
 * Load dashboard statistics from API
 */
// LINH
async function loadDashboardStatistics() {
    try {
        const currentUser = JSON.parse(localStorage.getItem('currentUser'));
        const organizerId = currentUser ? currentUser.user_id : null;
        
        // Build API URL
        let apiUrl = 'http://localhost:8080/api/dashboard/statistics';
        if (organizerId) {
            apiUrl += `?organizerId=${organizerId}`;
        }
        
        console.log('📊 Fetching dashboard statistics from:', apiUrl);
        
        const response = await fetch(apiUrl);
        
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }
        
        const stats = await response.json();
        console.log('✅ Dashboard statistics loaded:', stats);
        
        // Update UI with real data
        updateDashboardStats(stats);
        
    } catch (error) {
        console.error('❌ Error loading dashboard statistics:', error);
        showNotification('Không thể tải thống kê dashboard', 'error');
    }
}

/**
 * Update dashboard statistics in UI
 */
function updateDashboardStats(stats) {
    // Update active events count
    const activeEventsElement = document.querySelector('.stat-card:nth-child(1) .stat-info h3');
    if (activeEventsElement) {
        activeEventsElement.textContent = stats.activeEvents || 0;
    }
    
    // Update total attendees count
    const attendeesElement = document.querySelector('.stat-card:nth-child(2) .stat-info h3');
    if (attendeesElement) {
        attendeesElement.textContent = formatNumber(stats.totalAttendees || 0);
    }
    
    // Update tickets sold count
    const ticketsElement = document.querySelector('.stat-card:nth-child(3) .stat-info h3');
    if (ticketsElement) {
        ticketsElement.textContent = formatNumber(stats.totalTicketsSold || 0);
    }
    
    // Update revenue
    const revenueElement = document.querySelector('.stat-card:nth-child(4) .stat-info h3');
    if (revenueElement) {
        revenueElement.textContent = formatRevenue(stats.totalRevenue || 0);
    }
}
// LINH

/**
 * Load recent events from API
 */
async function loadRecentEvents() {
    try {
        const currentUser = JSON.parse(localStorage.getItem('currentUser'));
        const organizerId = currentUser ? currentUser.user_id : null;
        
        // Build API URL
        let apiUrl = 'http://localhost:8080/api/dashboard/recent-events?limit=5';
        if (organizerId) {
            apiUrl += `&organizerId=${organizerId}`;
        }
        
        console.log('📅 Fetching recent events from:', apiUrl);
        
        const response = await fetch(apiUrl);
        
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }
        
        const events = await response.json();
        console.log('✅ Recent events loaded:', events);
        
        // Update UI with real events
        updateRecentEventsList(events);
        
    } catch (error) {
        console.error('❌ Error loading recent events:', error);
        showNotification('Không thể tải danh sách sự kiện gần đây', 'error');
    }
}

/**
 * Update recent events list in UI
 */
function updateRecentEventsList(events) {
    const eventsList = document.querySelector('.events-list');
    if (!eventsList) return;
    
    if (!events || events.length === 0) {
        eventsList.innerHTML = `
            <div style="text-align: center; padding: 40px; color: #999;">
                <i class="fas fa-calendar-times" style="font-size: 48px; margin-bottom: 16px;"></i>
                <p>Chưa có sự kiện nào</p>
            </div>
        `;
        return;
    }
    
    // Clear current list
    eventsList.innerHTML = '';
    
    // Add each event
    events.forEach(event => {
        const eventItem = createEventItem(event);
        eventsList.appendChild(eventItem);
    });
}

/**
 * Create event item element
 */
function createEventItem(event) {
    const eventItem = document.createElement('div');
    eventItem.className = 'event-item';
    
    // Format date
    const eventDate = event.startTime ? new Date(event.startTime) : new Date();
    const day = eventDate.getDate();
    const month = eventDate.toLocaleDateString('vi-VN', { month: 'short' });
    
    // Determine event status
    const now = new Date();
    const startTime = new Date(event.startTime);
    const endTime = new Date(event.endTime);
    
    let status = 'upcoming';
    let statusText = 'Sắp diễn ra';
    
    if (now > endTime) {
        status = 'completed';
        statusText = 'Đã hoàn thành';
    } else if (now >= startTime && now <= endTime) {
        status = 'ongoing';
        statusText = 'Đang diễn ra';
    }
    
    // Count participants (from maxParticipants or default)
    const maxParticipants = event.maxParticipants || 0;
    
    eventItem.innerHTML = `
        <div class="event-date">
            <span class="day">${day}</span>
            <span class="month">${month}</span>
        </div>
        <div class="event-info">
            <h4>${event.title || 'Sự kiện'}</h4>
            <p><i class="fas fa-map-marker-alt"></i> ${event.location || 'Chưa có địa điểm'}</p>
            <p><i class="fas fa-users"></i> ${maxParticipants} người tham dự</p>
        </div>
        <div class="event-status">
            <span class="status ${status}">${statusText}</span>
        </div>
    `;
    
    return eventItem;
}

/**
 * Format number with thousand separators
 */
function formatNumber(num) {
    return num.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ",");
}

/**
 * Format revenue with VND suffix
 */
function formatRevenue(revenue) {
    if (revenue >= 1000000) {
        return (revenue / 1000000).toFixed(1) + 'M';
    } else if (revenue >= 1000) {
        return (revenue / 1000).toFixed(1) + 'K';
    }
    return revenue.toString();
}
