
        // Check authentication for manage role only
        document.addEventListener('DOMContentLoaded', function() {
            const userData = initAuth();
            if (!userData) return;

            // Load user data
            const userNameElement = document.getElementById('userName');
            if (userNameElement) {
                userNameElement.textContent = userData.name || 'Người dùng';
            }

            const userAvatarElement = document.getElementById('userAvatar');
            if (userAvatarElement) {
                const firstLetter = (userData.name || 'U').charAt(0).toUpperCase();
                userAvatarElement.textContent = firstLetter;
            }

            // Handle mobile menu
            const mobileMenuBtn = document.getElementById('mobileMenuBtn');
            const sidebar = document.querySelector('.sidebar');

            if (mobileMenuBtn) {
                mobileMenuBtn.addEventListener('click', function() {
                    sidebar.classList.toggle('active');
                });
            }

            // Handle create event form
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
            }
        });

   function collectFormData(formElement) {
    const formData = new FormData();
    
    // Tên field phải khớp với EventRequest.java DTO.
    formData.append('title', formElement.querySelector('#eventTitle').value);
    formData.append('category', formElement.querySelector('#eventCategory').value);
    // eventDate phải khớp với tên trường EventRequest.java
    formData.append('eventDate', formElement.querySelector('#eventDate').value); 
    formData.append('duration', formElement.querySelector('#eventDuration').value);
    formData.append('location', formElement.querySelector('#eventLocation').value);
    formData.append('maxParticipants', formElement.querySelector('#maxParticipants').value);
    formData.append('description', formElement.querySelector('#eventDescription').value);
    
    // Thu thập file ảnh
    const imageInput = formElement.querySelector('#eventImage');
    if (imageInput.files.length > 0) {
        // 'eventImage' phải khớp với tên trường MultipartFile trong DTO (EventRequest.java)
        formData.append('eventImage', imageInput.files[0]); 
    }
    
    return formData;
}
const EVENTS_API_URL = 'http://localhost:8080/api/events';
      // HÀM CẬP NHẬT: Gửi request POST đến Backend
async function handleCreateEvent() {
  const form = document.getElementById('createEventForm');
    
    // Lấy organizerId từ user hiện tại
    const user = JSON.parse(localStorage.getItem('currentUser'));
    if (!user || !user.user_id) {
        showNotification('Vui lòng đăng nhập để tạo sự kiện!', 'error');
        return;
    }
    
    const organizerId = user.user_id;
    
    // Thu thập dữ liệu từ form
    const formData = new FormData();
    formData.append('title', form.querySelector('#eventTitle').value);
    formData.append('category', form.querySelector('#eventCategory').value);
    formData.append('eventDate', form.querySelector('#eventDate').value); 
    formData.append('duration', form.querySelector('#eventDuration').value);
    formData.append('location', form.querySelector('#eventLocation').value);
    formData.append('maxParticipants', form.querySelector('#maxParticipants').value);
    formData.append('description', form.querySelector('#eventDescription').value);
    formData.append('organizerId', organizerId); // Thêm organizerId vào formData
    
    // Thêm status: mặc định là DRAFT, hoặc UPCOMING nếu có thời gian trong tương lai
    const eventDate = form.querySelector('#eventDate').value;
    if (eventDate) {
        const eventDateTime = new Date(eventDate);
        const now = new Date();
        if (eventDateTime > now) {
            formData.append('status', 'UPCOMING'); // Sắp diễn ra
        } else {
            formData.append('status', 'DRAFT'); // Bản nháp nếu thời gian đã qua
        }
    } else {
        formData.append('status', 'DRAFT'); // Mặc định là bản nháp
    }

    const imageInput = form.querySelector('#eventImage');
    if (imageInput && imageInput.files.length > 0) {
        formData.append('eventImage', imageInput.files[0]);
    }

    showNotification('Đang tạo sự kiện...', 'info');

    try {
        const response = await fetch(EVENTS_API_URL, {
            method: 'POST',
            headers: {
                'X-Organizer-Id': organizerId.toString() // Gửi trong header
            },
            body: formData 
        });

        if (response.ok) { // Kiểm tra mã trạng thái 200-299 (Bao gồm 201 CREATED)
            showNotification('Sự kiện đã được tạo thành công!', 'success');
            createEventForm.reset();
            const modalToggle = document.getElementById('modal-toggle-create');
    if (modalToggle) {
        modalToggle.checked = false; // Đóng modal
    }
            
            // ===================================================
            // <<< LOGIC CHUYỂN HƯỚNG ĐÃ THÊM VÀO ĐÂY >>>
            // Chuyển hướng người dùng về trang events.html để xem danh sách
            window.location.href = './events.html'; 
            // ===================================================
            
        } else {
            const errorText = await response.text();
            showNotification(`Lỗi tạo sự kiện: ${errorText}`, 'error');
            console.error('Lỗi tạo sự kiện server:', errorText);
        }
    } catch (error) {
        console.error('Lỗi Network hoặc Server:', error);
        showNotification('Đã xảy ra lỗi khi kết nối đến server.', 'error');
    }
}

        // Handle file upload
        function handleFileUpload(file) {
            const fileUploadArea = document.querySelector('.file-upload-area');
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
                localStorage.removeItem('currentUser');
                localStorage.removeItem('userEvents');
                localStorage.removeItem('authToken');
                sessionStorage.clear();
                window.location.href = '../../index.html';
            }
        }

        // Notification system
        function showNotification(message, type = 'info') {
            const existingNotifications = document.querySelectorAll('.notification');
            existingNotifications.forEach(notification => notification.remove());

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
    