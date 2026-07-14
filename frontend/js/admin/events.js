/* =========================================================
   EVENTS PAGE JS — LOGIC KẾT NỐI API VÀO HTML MỚI
   ========================================================= */

// Update user name in header
document.addEventListener('DOMContentLoaded', function() {
    const userData = getCurrentUser();
    if (userData) {
        const userNameElement = document.getElementById('userName');
        if (userNameElement) {
            userNameElement.textContent = userData.name || 'Người dùng';
        }
    }
});

// --- CẤU HÌNH (CONSTANTS) ---
// Tránh duplicate declaration
if (typeof window.EVENTS_API_BASE_URL === 'undefined') {
    window.EVENTS_API_BASE_URL = 'http://localhost:8080/api/events';
}
// Dùng tên biến riêng để tránh xung đột với các file khác
const EVENTS_API_BASE_URL = window.EVENTS_API_BASE_URL;
// Đường dẫn cơ sở để ghép với tên file ảnh (ví dụ: my_event.jpg)
const IMAGE_BASE_URL = 'http://localhost:8080/images/'; 
// Ảnh mặc định nếu không có ảnh hoặc lỗi
const FALLBACK_IMAGE_URL = "https://images.unsplash.com/photo-1525182008055-f88b95ff7980?q=80&auto=format&fit=crop&w=1200";

// Danh sách sự kiện toàn cục để tiện truy xuất khi mở modal
let allEventsData = [];

// Lấy thông tin user hiện tại
function getCurrentOrganizerId() {
    const user = JSON.parse(localStorage.getItem('currentUser'));
    if (!user) {
        console.error('Chưa đăng nhập!');
        return null;
    }
    return user.user_id; // user_id từ Account model
}

// ************************************************************
// HÀM TIỆN ÍCH (HELPERS)
// ************************************************************

/**
 * Định dạng thời gian ISO sang VN (dd/mm/yyyy, HH:MM)
 * @param {string} isoString Chuỗi thời gian ISO 8601
 */
function formatTime(isoString) {
   if (!isoString) return 'N/A';
    try {
        const date = new Date(isoString);
        // Kiểm tra tính hợp lệ của ngày tháng
        if (isNaN(date.getTime())) return 'Invalid Date'; 
        return date.toLocaleDateString('vi-VN') + ', ' + date.toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit' });
    } catch (e) {
        return 'Invalid Date';
    }
}

/**
 * Thêm các Listener cho nút Chi tiết/Chỉnh sửa sau khi render
 */
function attachEventListeners() {
    // Hiện tại chỉ cần gọi hàm này khi render xong để đảm bảo nút hoạt động
    // Logic của nút được gọi trực tiếp trong HTML (onclick="openDetailModal()")
    // Nếu bạn muốn dùng addEventListener, cần cập nhật logic ở đây
}

// ************************************************************
// 1. HÀM LẤY DỮ LIỆU TỪ API
// ************************************************************

async function fetchEvents() {
    const container = document.getElementById('events-grid-container');
    if (!container) {
        console.error('❌ Không tìm thấy container events-grid-container');
        return;
    }

    container.innerHTML = '<p style="text-align: center; width: 100%; grid-column: 1/-1; padding: 20px;"><i class="fas fa-spinner fa-spin"></i> Đang tải dữ liệu từ máy chủ...</p>';

    try {
        // Lấy organizerId từ user hiện tại
        const organizerId = getCurrentOrganizerId();
        if (!organizerId) {
            console.warn('⚠️ Chưa có organizerId');
            container.innerHTML = '<p style="text-align:center; grid-column: 1/-1; color:red;">Vui lòng đăng nhập để xem sự kiện của bạn.</p>';
            return;
        }

        console.log('📡 Đang gọi API với organizerId:', organizerId);
        console.log('📡 EVENTS_API_BASE_URL:', EVENTS_API_BASE_URL);

        // Gọi API /api/events/my-events với organizerId
        const url = `${EVENTS_API_BASE_URL}/my-events?organizerId=${organizerId}`;
        console.log('🔗 URL:', url);
        
        const response = await fetch(url).catch(err => {
            console.error('❌ Lỗi fetch:', err);
            throw new Error('Không thể kết nối đến server. Vui lòng kiểm tra backend có đang chạy không.');
        });
        
        console.log('📥 Response status:', response.status, response.statusText);
        
        // Xử lý mảng rỗng
        if (response.status === 200) {
            const contentType = response.headers.get('content-type');
            console.log('📄 Content-Type:', contentType);
            
            // Kiểm tra nếu response rỗng
            const text = await response.text();
            console.log('📝 Response text:', text);
            
            if (!text || text.trim() === '') {
                container.innerHTML = '<p style="text-align:center; grid-column: 1/-1; padding: 20px;">Chưa có sự kiện nào. Hãy tạo sự kiện đầu tiên của bạn!</p>';
                allEventsData = [];
                return;
            }
            
            // Parse JSON
            try {
                allEventsData = JSON.parse(text);
                console.log('✅ Parsed data:', allEventsData);
                
                // Kiểm tra nếu dữ liệu không phải là mảng
                if (!Array.isArray(allEventsData)) {
                    console.error('❌ Dữ liệu không phải mảng:', typeof allEventsData, allEventsData);
                    throw new Error('Server trả về cấu trúc dữ liệu không hợp lệ (không phải mảng).');
                }
                
                // Nếu mảng rỗng
                if (allEventsData.length === 0) {
                    container.innerHTML = '<p style="text-align:center; grid-column: 1/-1; padding: 20px;">Chưa có sự kiện nào. Hãy tạo sự kiện đầu tiên của bạn!</p>';
                    allEventsData = [];
                    return;
                }
                
                // Render events
                console.log('🎨 Đang render', allEventsData.length, 'sự kiện');
                renderEvents(allEventsData);
                
            } catch (parseError) {
                console.error('❌ Lỗi parse JSON:', parseError);
                throw new Error('Không thể đọc dữ liệu từ server: ' + parseError.message);
            }
        } else if (response.status === 204) {
            // No Content
            container.innerHTML = '<p style="text-align:center; grid-column: 1/-1; padding: 20px;">Chưa có sự kiện nào. Hãy tạo sự kiện đầu tiên của bạn!</p>';
            allEventsData = [];
            return;
        } else {
            // Lỗi HTTP
            const errorText = await response.text().catch(() => 'Lỗi không xác định');
            console.error('❌ HTTP Error:', response.status, errorText);
            throw new Error(`Lỗi HTTP ${response.status}: ${errorText}`);
        }

    } catch (error) {
        console.error("❌ LỖI KẾT NỐI/TẢI DỮ LIỆU:", error);
        container.innerHTML = `
            <div style="text-align:center; color:red; grid-column: 1/-1; padding: 20px;">
                <h3>❌ Không thể tải danh sách sự kiện!</h3>
                <p><strong>Chi tiết lỗi:</strong> ${error.message}</p>
                <p style="margin-top: 10px; font-size: 14px; color: #666;">
                    Vui lòng kiểm tra:<br>
                    - Backend đang chạy tại http://localhost:8080<br>
                    - Bạn đã đăng nhập với tài khoản organizer<br>
                    - Console để xem chi tiết lỗi
                </p>
            </div>
        `;
    }
}

// ************************************************************
// 2. HÀM HIỂN THỊ HTML
// ************************************************************

function renderEvents(events) {
    const container = document.getElementById('events-grid-container');
    
    if (!container) {
        console.error('❌ Không tìm thấy container events-grid-container');
        return;
    }
    
    // Clear container
    container.innerHTML = ''; 
    
    if (!Array.isArray(events) || events.length === 0) {
        container.innerHTML = '<p style="text-align:center; grid-column: 1/-1; padding: 20px;">Chưa có sự kiện nào. Hãy tạo sự kiện đầu tiên của bạn!</p>';
        return;
    }

    console.log('🎨 Bắt đầu render', events.length, 'sự kiện');

    // Tạo HTML string cho tất cả events
    let cardsHTML = '';

    // FIX: Đảm bảo chỉ lặp qua mảng các đối tượng Event
    events.forEach(evt => { 
        
        // ⚠️ FIX: Kiểm tra an toàn cho từng phần tử
        // Backend trả về eventId (camelCase), không phải id
        const eventId = evt.eventId || evt.id;
        if (!evt || !eventId) {
            console.warn("⚠️ Bỏ qua phần tử sự kiện rỗng hoặc không có ID:", evt);
            return; 
        }
        
        console.log('📝 Rendering event:', eventId, evt.title);

        // --- 1. Logic Xây dựng Đường dẫn Ảnh ---
        const rawImageUrl = evt.imageUrl; 
        let finalImageUrl = FALLBACK_IMAGE_URL; 

        if (rawImageUrl) {
            if (rawImageUrl.startsWith('http://') || rawImageUrl.startsWith('https://')) {
                // Đã là full URL
                finalImageUrl = rawImageUrl;
            } else if (rawImageUrl.startsWith('/')) {
                // Relative path bắt đầu bằng /, ghép với base URL của backend
                finalImageUrl = 'http://localhost:8080' + rawImageUrl;
            } else {
                // Relative path không có /, ghép với IMAGE_BASE_URL
                finalImageUrl = IMAGE_BASE_URL + rawImageUrl;
            }
        }
        // --- End Logic Ảnh ---

        // Xử lý các trường dữ liệu khác
        const title = evt.title || 'Chưa đặt tên';
        const location = evt.location || 'Địa điểm chưa rõ';
        const startTime = formatTime(evt.startTime); 
        const maxParticipants = evt.maxParticipants || 'N/A';
        
        // Logic xử lý status và badge
        // Tôn trọng status từ database, chỉ tự động cập nhật cho PUBLISHED cũ
        let statusText = evt.status || 'DRAFT';
        let badgeClass = 'badge-muted';
        const now = new Date();
        
        // Chỉ tự động xác định status nếu:
        // 1. Status là null/empty
        // 2. Status là PUBLISHED (status cũ cần migrate)
        // Với các status mới (UPCOMING, ONGOING, COMPLETED, DRAFT, CANCELLED), giữ nguyên từ database
        const upperStatus = statusText.toUpperCase();
        if (!statusText || statusText.trim() === '' || upperStatus === 'PUBLISHED') {
            // Tự động xác định dựa trên thời gian
            if (evt.startTime && evt.endTime) {
                const startTime = new Date(evt.startTime);
                const endTime = new Date(evt.endTime);
                
                if (endTime < now) {
                    statusText = 'COMPLETED';
                } else if (startTime <= now && now < endTime) {
                    statusText = 'ONGOING';
                } else if (startTime > now) {
                    statusText = 'UPCOMING';
                } else {
                    statusText = 'DRAFT';
                }
            } else {
                statusText = 'DRAFT';
            }
        }
        // Nếu status đã có giá trị hợp lệ (UPCOMING, ONGOING, COMPLETED, DRAFT, CANCELLED), giữ nguyên
        
        // Map status sang text và badge class
        switch (statusText.toUpperCase()) {
            case 'DRAFT':
                badgeClass = 'badge-muted';
                statusText = 'Bản nháp';
                break;
            case 'UPCOMING':
                badgeClass = 'badge-info';
                statusText = 'Sắp diễn ra';
                break;
            case 'ONGOING':
                badgeClass = 'badge-success';
                statusText = 'Đang diễn ra';
                break;
            case 'COMPLETED':
                badgeClass = 'badge-muted';
                statusText = 'Đã kết thúc';
                break;
            case 'CANCELLED':
                badgeClass = 'badge-danger';
                statusText = 'Đã hủy';
                break;
            default:
                // Fallback cho các status cũ
                if (statusText.toUpperCase() === 'PUBLISHED') {
                    // Nếu là PUBLISHED cũ, tự động xác định
                    if (evt.startTime && evt.endTime) {
                        const startTime = new Date(evt.startTime);
                        const endTime = new Date(evt.endTime);
                        if (endTime < now) {
                            statusText = 'Đã kết thúc';
                            badgeClass = 'badge-muted';
                        } else if (startTime <= now && now < endTime) {
                            statusText = 'Đang diễn ra';
                            badgeClass = 'badge-success';
                        } else {
                            statusText = 'Sắp diễn ra';
                            badgeClass = 'badge-info';
                        }
                    } else {
                        statusText = 'Sắp diễn ra';
                        badgeClass = 'badge-info';
                    }
                } else {
                    statusText = 'Bản nháp';
                    badgeClass = 'badge-muted';
                }
                break;
        }

        // 2. TẠO THẺ HTML VỚI ẢNH ĐỘC LẬP
        const cardHTML = `
            <article class="event-card" data-event-id="${eventId}">
                <div class="event-media">
                    <img src="${finalImageUrl}" alt="${title}" 
                         onerror="this.onerror=null; this.src='${FALLBACK_IMAGE_URL}';">
                    <span class="badge ${badgeClass}">${statusText}</span>
                </div>
                <div class="event-body">
                    <h3 class="event-title">${title}</h3>
                    <p class="meta"><i class="fas fa-calendar"></i> ${startTime}</p>
                    <p class="meta"><i class="fas fa-map-marker-alt"></i> ${location}</p>
                    <div class="stats">
                        <span><i class="fas fa-users"></i> 0/${maxParticipants}</span>
                        <span><i class="fas fa-ticket-alt"></i> Vé</span>
                    </div>
                    <div class="actions">
                        <button class="btn btn-outline detail-btn" onclick="openDetailModal(${eventId})">Chi tiết</button>
                        <button class="btn btn-primary edit-btn" onclick="openEditModal(${eventId})">Chỉnh sửa</button>
                        <button class="btn btn-danger delete-btn" onclick="deleteEvent(${eventId})">Xóa</button>
                    </div>
                </div>
            </article>
        `;
        cardsHTML += cardHTML;
    });
    
    // Set tất cả HTML một lần (hiệu quả hơn)
    container.innerHTML = cardsHTML;
    console.log('✅ Đã render xong', events.length, 'sự kiện');
    
    // Attach event listeners sau khi render
    attachEventListeners();
}

// ************************************************************
// 3. HÀM XỬ LÝ MODAL (DETAIL & EDIT)
// ************************************************************

function openDetailModal(id) {
    const evt = allEventsData.find(e => (e.eventId || e.id) == id);
    if (!evt) return;

    // Lấy finalImageUrl như trong renderEvents
    const rawImageUrl = evt.imageUrl; 
    let finalImageUrl = FALLBACK_IMAGE_URL; 
    if (rawImageUrl) {
        if (rawImageUrl.startsWith('http://') || rawImageUrl.startsWith('https://')) {
            finalImageUrl = rawImageUrl;
        } else if (rawImageUrl.startsWith('/')) {
            finalImageUrl = 'http://localhost:8080' + rawImageUrl;
        } else {
            finalImageUrl = IMAGE_BASE_URL + rawImageUrl;
        }
    }
    
    // Xác định status text để hiển thị - tôn trọng status từ database
    let statusText = evt.status || 'DRAFT';
    const now = new Date();
    
    // Chỉ tự động xác định nếu status là null/empty hoặc PUBLISHED cũ
    const upperStatus = statusText.toUpperCase();
    if (!statusText || statusText.trim() === '' || upperStatus === 'PUBLISHED') {
        // Tự động xác định dựa trên thời gian
        if (evt.startTime && evt.endTime) {
            const startTime = new Date(evt.startTime);
            const endTime = new Date(evt.endTime);
            
            if (endTime < now) {
                statusText = 'COMPLETED';
            } else if (startTime <= now && now < endTime) {
                statusText = 'ONGOING';
            } else if (startTime > now) {
                statusText = 'UPCOMING';
            } else {
                statusText = 'DRAFT';
            }
        } else {
            statusText = 'DRAFT';
        }
    }
    
    // Map status sang text
    switch (statusText.toUpperCase()) {
        case 'DRAFT': statusText = 'Bản nháp'; break;
        case 'UPCOMING': statusText = 'Sắp diễn ra'; break;
        case 'ONGOING': statusText = 'Đang diễn ra'; break;
        case 'COMPLETED': statusText = 'Đã kết thúc'; break;
        case 'CANCELLED': statusText = 'Đã hủy'; break;
        case 'PUBLISHED':
            // Fallback cho PUBLISHED cũ
            if (evt.startTime && evt.endTime) {
                const startTime = new Date(evt.startTime);
                const endTime = new Date(evt.endTime);
                if (endTime < now) {
                    statusText = 'Đã kết thúc';
                } else if (startTime <= now && now < endTime) {
                    statusText = 'Đang diễn ra';
                } else {
                    statusText = 'Sắp diễn ra';
                }
            } else {
                statusText = 'Sắp diễn ra';
            }
            break;
        default: statusText = 'Bản nháp'; break;
    }

    document.getElementById('modal-detail-title').textContent = evt.title || 'N/A';
    document.getElementById('modal-detail-status').textContent = statusText;
    document.getElementById('modal-detail-start').textContent = formatTime(evt.startTime);
    document.getElementById('modal-detail-end').textContent = formatTime(evt.endTime);
    document.getElementById('modal-detail-location').textContent = evt.location || 'N/A';
    document.getElementById('modal-detail-participants').textContent = evt.maxParticipants || 'N/A';
    document.getElementById('modal-detail-desc').textContent = evt.description || 'Không có mô tả';
    document.getElementById('modal-detail-img').src = finalImageUrl;

    document.getElementById('global-detail-modal').checked = true;
}

function openEditModal(id) {
    const evt = allEventsData.find(e => (e.eventId || e.id) == id);
    if (!evt) return;

    const eventId = evt.eventId || evt.id;
    document.getElementById('edit-event-id').value = eventId;
    document.getElementById('modal-edit-subtitle').textContent = `ID: ${eventId}`;
    document.getElementById('edit-title').value = evt.title || '';
    document.getElementById('edit-location').value = evt.location || '';
    
    // Đảm bảo format chuẩn ISO cho input type="datetime-local" (chỉ lấy 16 ký tự đầu)
    if(evt.startTime) document.getElementById('edit-start').value = evt.startTime.substring(0, 16);
    if(evt.endTime) document.getElementById('edit-end').value = evt.endTime.substring(0, 16);
    
    document.getElementById('edit-max-participants').value = evt.maxParticipants || 0;
    document.getElementById('edit-image-url').value = evt.imageUrl || '';
    
    // Xác định status hiển thị: nếu là UPCOMING/ONGOING/COMPLETED tự động, giữ nguyên
    // Nếu là PUBLISHED cũ, chuyển thành UPCOMING
    let displayStatus = evt.status || 'DRAFT';
    if (displayStatus.toUpperCase() === 'PUBLISHED') {
        // Chuyển PUBLISHED cũ thành UPCOMING
        displayStatus = 'UPCOMING';
    }
    document.getElementById('edit-status').value = displayStatus;
    
    document.getElementById('edit-desc').value = evt.description || '';

    document.getElementById('global-edit-modal').checked = true;
}

// ************************************************************
// 4. HÀM LƯU THAY ĐỔI
// ************************************************************

async function saveEventChanges() {
    const id = document.getElementById('edit-event-id').value;
    const organizerId = getCurrentOrganizerId();
    
    if (!organizerId) {
        alert('Vui lòng đăng nhập!');
        return;
    }
    
    const formData = new FormData();
    
    // Gửi dữ liệu theo format Spring Boot yêu cầu (ModelAttribute)
    formData.append('title', document.getElementById('edit-title').value);
    formData.append('location', document.getElementById('edit-location').value);
    
    // Gửi eventDate (startTime) và tính duration từ startTime và endTime
    const startTime = document.getElementById('edit-start').value;
    const endTime = document.getElementById('edit-end').value;
    
    if (startTime) {
        formData.append('eventDate', startTime); // Backend mong đợi eventDate
    }
    
    // Tính duration từ startTime và endTime (tính bằng giờ)
    if (startTime && endTime) {
        const start = new Date(startTime);
        const end = new Date(endTime);
        const durationHours = Math.round((end - start) / (1000 * 60 * 60)); // Chuyển từ ms sang giờ
        formData.append('duration', durationHours.toString());
    }
    
    formData.append('maxParticipants', document.getElementById('edit-max-participants').value);
    formData.append('imageUrl', document.getElementById('edit-image-url').value);
    
    // QUAN TRỌNG: Gửi status để backend lưu đúng
    const selectedStatus = document.getElementById('edit-status').value;
    formData.append('status', selectedStatus);
    console.log('📤 Gửi status lên backend:', selectedStatus); // Debug log
    
    formData.append('description', document.getElementById('edit-desc').value);

    try {
        const response = await fetch(`${EVENTS_API_BASE_URL}/${id}?organizerId=${organizerId}`, {
            method: 'PUT',
            headers: {
                'X-Organizer-Id': organizerId.toString()
            },
            body: formData 
        });

        if (response.ok) {
            alert('Cập nhật thành công!');
            document.getElementById('global-edit-modal').checked = false; 
            fetchEvents(); // Tải lại danh sách
        } else {
            // Lưu response text trước khi parse JSON
            const responseText = await response.text();
            let errorData;
            try {
                errorData = JSON.parse(responseText);
            } catch (e) {
                errorData = { message: responseText || 'Không thể cập nhật sự kiện' };
            }
            alert('Lỗi: ' + (errorData.message || 'Không thể cập nhật sự kiện'));
        }
    } catch (error) {
        console.error(error);
        alert('Có lỗi xảy ra khi kết nối server.');
    }
}

// ************************************************************
// 5. HÀM XÓA SỰ KIỆN
// ************************************************************

/**
 * Xóa sự kiện (global function for onclick)
 */
window.deleteEvent = async function(eventId) {
    // Xác nhận trước khi xóa
    const event = allEventsData.find(e => (e.eventId || e.id) == eventId);
    const eventTitle = event ? (event.title || 'sự kiện này') : 'sự kiện này';
    
    const confirmed = confirm(`Bạn có chắc chắn muốn xóa "${eventTitle}"?\n\nHành động này không thể hoàn tác và sẽ xóa tất cả dữ liệu liên quan (feedback, check-in, vé).`);
    
    if (!confirmed) {
        return;
    }
    
    try {
        console.log('🗑️ Đang xóa sự kiện:', eventId);
        
        // Gọi API xóa sự kiện (sử dụng admin endpoint)
        const response = await fetch(`http://localhost:8080/api/admin/events/${eventId}`, {
            method: 'DELETE'
        });
        
        const responseText = await response.text();
        let responseData;
        
        try {
            responseData = JSON.parse(responseText);
        } catch (e) {
            responseData = { message: responseText || 'Lỗi không xác định' };
        }
        
        if (response.ok) {
            alert(responseData.message || 'Đã xóa sự kiện thành công!');
            
            // Xóa sự kiện khỏi danh sách local
            allEventsData = allEventsData.filter(e => (e.eventId || e.id) != eventId);
            
            // Tải lại danh sách sự kiện
            fetchEvents();
        } else {
            alert('Lỗi: ' + (responseData.message || 'Không thể xóa sự kiện'));
        }
    } catch (error) {
        console.error('❌ Lỗi khi xóa sự kiện:', error);
        alert('Có lỗi xảy ra khi kết nối server: ' + error.message);
    }
}

// ************************************************************
// HÀM KHỞI TẠO CHÍNH (GỌI KHI TẢI TRANG)
// ************************************************************

/**
 * Hàm khởi tạo trang events - được gọi từ dashboard.js khi switch section
 * Hoặc có thể gọi trực tiếp khi trang events.html được load độc lập
 */
function initEventsPage() {
    console.log('🚀 Khởi tạo trang events...');
    
    // Kiểm tra xem container có tồn tại không
    const container = document.getElementById('events-grid-container');
    if (!container) {
        console.warn('⚠️ Không tìm thấy container events-grid-container. Có thể đang ở trang khác.');
        return;
    }
    
    // Gọi fetchEvents để load dữ liệu
    fetchEvents();
}

// Tự động gọi khi DOM ready (cho trang events.html độc lập)
document.addEventListener('DOMContentLoaded', () => {
    // Chỉ gọi fetchEvents nếu trang events.html được tải độc lập
    // Nếu trang này được nhúng (index.html -> dashboard.js gọi), dashboard.js sẽ gọi initEventsPage()
    const container = document.getElementById('events-grid-container');
    if (container) {
        // Nếu có container, có nghĩa là đang ở trang events
        initEventsPage();
    }
});