/* ==========================================
   FEEDBACK PAGE INTERACTIONS
   ========================================== */
(function () {
    // Update user name in header
    document.addEventListener("DOMContentLoaded", function() {
        const currentUser = JSON.parse(localStorage.getItem('currentUser'));
        if (currentUser) {
            const userNameElement = document.getElementById('userName');
            if (userNameElement) {
                userNameElement.textContent = currentUser.name || 'Người dùng';
            }
        }
    });

    const root = document.getElementById("feedback-section");
    if (!root) return;

    const starsWrap = root.querySelector("#stars");
    const rateText = root.querySelector("#rate-text");
    const idea = root.querySelector("#idea");
    const nameInput = root.querySelector('input[type="text"]');
    const emailInput = root.querySelector('input[type="email"]');
    const submitBtn = root.querySelector("#submitBtn");
    const toast = root.querySelector("#toast");
    const eventSelect = root.querySelector("#eventSelectFeedback");
    const eventQuestion = root.querySelector("#eventQuestion");
    const selectedEventTitle = root.querySelector("#selectedEventTitle");
    const feedbackForm = root.querySelector("#feedbackForm");
    const noEventsMessage = root.querySelector("#noEventsMessage");

    // Map cảm xúc theo điểm
    const feelMap = {
        0: "Mời bạn đánh giá ⭐",
        1: "Tệ 😞",
        2: "Chưa tốt 😕",
        3: "Ổn 👍",
        4: "Tốt! 👏",
        5: "Tuyệt vời! 🎉"
    };

    let selected = 0;
    let selectedEventId = null;
    let completedEvents = [];

    // Helper: show toast
    function showToast(msg = "Cảm ơn bạn đã gửi phản hồi 💙", type = "success") {
        if (!toast) return;
        toast.textContent = msg;
        toast.className = `toast ${type}`;
        toast.classList.add("show");
        setTimeout(() => toast.classList.remove("show"), 3000);
    }

    // Render fill sao theo value
    function renderStars(value, hover = false) {
        const items = starsWrap.querySelectorAll(".fa-star");
        items.forEach((el) => {
            const v = Number(el.getAttribute("data-value") || "0");
            const filled = v <= value;
            el.classList.toggle("fa-solid", filled);
            el.classList.toggle("fa-regular", !filled);
            el.classList.toggle("is-filled", filled);
        });

        if (rateText) {
            rateText.textContent = feelMap[value || 0] || "";
            // Đổi màu cảm xúc theo điểm
            if (value <= 2) rateText.style.color = "#ef4444";
            else if (value === 3) rateText.style.color = "#10b981";
            else if (value >= 4) rateText.style.color = "#f59e0b";
            if (value === 5) rateText.style.color = "#f59e0b";
            if (value === 0) rateText.style.color = "var(--muted)";
        }
    }

    // Gắn sự kiện lên sao
    if (starsWrap) {
        starsWrap.addEventListener("mouseover", (e) => {
            const star = e.target.closest(".fa-star");
            if (!star) return;
            renderStars(Number(star.getAttribute("data-value") || "0"), true);
        });
        starsWrap.addEventListener("mouseout", () => {
            renderStars(selected);
        });
        starsWrap.addEventListener("click", (e) => {
            const star = e.target.closest(".fa-star");
            if (!star) return;
            selected = Number(star.getAttribute("data-value") || "0");
            renderStars(selected);
        });

        // Khởi tạo
        renderStars(selected);
    }

    // Load danh sách sự kiện đã kết thúc
    async function loadCompletedEvents() {
        const user = JSON.parse(localStorage.getItem("currentUser"));
        if (!user) {
            console.warn('⚠️ Chưa đăng nhập');
            showToast("Vui lòng đăng nhập để đánh giá sự kiện", "error");
            return;
        }

        console.log('🔍 Đang tải danh sách sự kiện đã kết thúc cho user:', user.user_id);

        try {
            const response = await fetch(`http://localhost:8080/api/feedback/completed-events/${user.user_id}`);
            
            if (!response.ok) {
                const errorText = await response.text();
                console.error('❌ HTTP Error:', response.status, errorText);
                throw new Error(`Không thể tải danh sách sự kiện (${response.status})`);
            }

            completedEvents = await response.json();
            console.log('📋 Completed events từ backend:', completedEvents);
            console.log('📊 Tổng số sự kiện đã kết thúc:', completedEvents.length);

            // Log chi tiết từng event
            completedEvents.forEach((event, index) => {
                console.log(`  ${index + 1}. Event ID: ${event.eventId}, Title: ${event.title}, Status: ${event.status}, EndTime: ${event.endTime}, HasFeedback: ${event.hasFeedback}`);
            });

            // Filter: chỉ hiển thị sự kiện chưa feedback
            const notFeedbackedEvents = completedEvents.filter(e => !e.hasFeedback);
            console.log('✅ Số sự kiện chưa feedback:', notFeedbackedEvents.length);

            if (notFeedbackedEvents.length === 0) {
                // Không có sự kiện nào để feedback
                console.warn('⚠️ Không có sự kiện nào để feedback');
                if (eventSelect) eventSelect.style.display = 'none';
                if (noEventsMessage) noEventsMessage.style.display = 'block';
                return;
            }

            // Populate dropdown
            if (eventSelect) {
                eventSelect.innerHTML = '<option value="">-- Chọn sự kiện --</option>';
                notFeedbackedEvents.forEach(event => {
                    const option = document.createElement('option');
                    option.value = event.eventId;
                    option.textContent = `${event.title} (${formatDate(event.endTime)})`;
                    if (event.hasFeedback) {
                        option.textContent += ' - Đã đánh giá';
                        option.disabled = true;
                    }
                    eventSelect.appendChild(option);
                });
                console.log('✅ Đã populate dropdown với', notFeedbackedEvents.length, 'sự kiện');
            }

        } catch (error) {
            console.error('❌ Lỗi load completed events:', error);
            showToast("Không thể tải danh sách sự kiện: " + error.message, "error");
        }
    }

    // Format date
    function formatDate(dateString) {
        if (!dateString) return '';
        const date = new Date(dateString);
        return date.toLocaleDateString('vi-VN');
    }

    // Khi chọn sự kiện
    if (eventSelect) {
        eventSelect.addEventListener('change', (e) => {
            const eventId = e.target.value;
            
            if (!eventId) {
                // Reset form
                selectedEventId = null;
                selected = 0;
                renderStars(0);
                if (eventQuestion) eventQuestion.style.display = 'none';
                if (starsWrap) starsWrap.style.display = 'none';
                if (rateText) rateText.style.display = 'none';
                if (feedbackForm) feedbackForm.style.display = 'none';
                return;
            }

            // Tìm event
            const event = completedEvents.find(e => e.eventId == eventId);
            if (!event) return;

            selectedEventId = event.eventId;

            // Hiển thị form
            if (selectedEventTitle) selectedEventTitle.textContent = event.title;
            if (eventQuestion) eventQuestion.style.display = 'block';
            if (starsWrap) starsWrap.style.display = 'inline-flex';
            if (rateText) {
                rateText.style.display = 'block';
                rateText.textContent = feelMap[0];
            }
            if (feedbackForm) feedbackForm.style.display = 'block';

            // Reset rating
            selected = 0;
            renderStars(0);
        });
    }

    // Submit
    // LINH
    if (submitBtn) {
        submitBtn.addEventListener("click", async () => {
            // Validate
            if (!selectedEventId) {
                showToast("Vui lòng chọn sự kiện", "error");
                return;
            }

            if (selected === 0) {
                showToast("Bạn vui lòng chọn số sao đánh giá nhé!", "error");
                return;
            }

            const user = JSON.parse(localStorage.getItem("currentUser"));
            if (!user) {
                showToast("Vui lòng đăng nhập", "error");
                return;
            }

            // Build payload
            const payload = {
                eventId: selectedEventId,
                userId: user.user_id,
                rating: selected,
                comment: idea?.value?.trim() || ""
            };

            try {
                submitBtn.disabled = true;
                submitBtn.textContent = "Đang gửi...";

                const response = await fetch('http://localhost:8080/api/feedback', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify(payload)
                });

                const data = await response.json();

                if (response.ok && data.success) {
                    showToast("Cảm ơn bạn đã đánh giá! 💙", "success");
                    
                    // Reset form
                    selected = 0;
                    renderStars(0);
                    if (idea) idea.value = "";
                    if (nameInput) nameInput.value = "";
                    if (emailInput) emailInput.value = "";
                    if (eventSelect) eventSelect.value = "";
                    
                    // Ẩn form
                    if (eventQuestion) eventQuestion.style.display = 'none';
                    if (starsWrap) starsWrap.style.display = 'none';
                    if (rateText) rateText.style.display = 'none';
                    if (feedbackForm) feedbackForm.style.display = 'none';
                    
                    // Reload events (để cập nhật hasFeedback) và reload feedbacks list
                    await loadCompletedEvents();
                    setTimeout(loadUserFeedbacks, 500); // Delay để đảm bảo backend đã cập nhật
                } else {
                    showToast(data.message || "Gửi phản hồi thất bại", "error");
                }
            } catch (error) {
                console.error('❌ Lỗi submit feedback:', error);
                showToast("Không thể kết nối server", "error");
            } finally {
                submitBtn.disabled = false;
                submitBtn.textContent = "GỬI PHẢN HỒI";
            }
        });
    }
    // LINH

    // A11y: Enter để gửi nếu đang focus submit
    submitBtn?.addEventListener("keydown", (e) => {
        if (e.key === "Enter" || e.key === " ") {
            e.preventDefault();
            submitBtn.click();
        }
    });

    // Load user feedbacks với organizer replies
    async function loadUserFeedbacks() {
        const user = JSON.parse(localStorage.getItem("currentUser"));
        if (!user) {
            console.warn('⚠️ Chưa đăng nhập');
            return;
        }

        const feedbacksList = document.getElementById('userFeedbacksList');
        const noFeedbacksMessage = document.getElementById('noFeedbacksMessage');
        
        if (!feedbacksList) return;

        try {
            const response = await fetch(`http://localhost:8080/api/feedback/user/${user.user_id}`);
            
            if (!response.ok) {
                throw new Error(`HTTP ${response.status}`);
            }

            const feedbacks = await response.json();
            console.log('📋 User feedbacks:', feedbacks);

            if (!Array.isArray(feedbacks) || feedbacks.length === 0) {
                feedbacksList.style.display = 'none';
                if (noFeedbacksMessage) noFeedbacksMessage.style.display = 'block';
                return;
            }

            feedbacksList.style.display = 'block';
            if (noFeedbacksMessage) noFeedbacksMessage.style.display = 'none';

            // Render feedbacks
            feedbacksList.innerHTML = feedbacks.map((feedback, index) => {
                // Create rating stars
                const stars = '★'.repeat(feedback.rating || 0) + '☆'.repeat(5 - (feedback.rating || 0));
                
                // Format date
                const feedbackDate = feedback.createdAt ? new Date(feedback.createdAt) : new Date();
                const dateStr = formatDateForDisplay(feedbackDate);
                
                // Check if has organizer reply
                const hasReply = feedback.organizerReply && feedback.organizerReply.trim().length > 0;
                const replyDate = feedback.organizerReplyAt ? new Date(feedback.organizerReplyAt) : null;
                const replyDateStr = replyDate ? formatDateForDisplay(replyDate) : '';

                return `
                    <div class="user-feedback-item ${hasReply ? 'has-reply' : ''}" data-event-id="${feedback.eventId}" data-feedback-index="${index}">
                        <div class="user-feedback-header">
                            <div class="stars">${stars}</div>
                            <small>${dateStr}</small>
                        </div>
                        <p class="user-feedback-event">Sự kiện: <strong>${feedback.eventTitle || 'N/A'}</strong></p>
                        <p class="user-feedback-comment">${feedback.comment || '<em style="color: #94a3b8;">Không có bình luận</em>'}</p>
                        ${hasReply ? `
                            <div class="organizer-reply-box">
                                <div class="organizer-reply-header">
                                    <i class="fas fa-reply" style="margin-right: 6px;"></i>
                                    <strong>Phản hồi từ tổ chức viên</strong>
                                </div>
                                <p class="organizer-reply-text">${feedback.organizerReply}</p>
                                <small class="organizer-reply-date">${replyDateStr}</small>
                            </div>
                        ` : `
                            <div class="waiting-reply">
                                <small style="color: #94a3b8; font-style: italic;">
                                    <i class="fas fa-clock"></i> Đang chờ phản hồi từ tổ chức viên
                                </small>
                            </div>
                        `}
                        <div class="view-others-feedback" onclick="loadOtherFeedbacks(this, ${feedback.eventId}, '${feedback.eventTitle || 'Sự kiện'}')">
                            <i class="fas fa-comments"></i>
                            <span>Xem đánh giá của người khác</span>
                            <i class="fas fa-chevron-down toggle-icon"></i>
                        </div>
                        <div class="other-feedbacks-container" id="other-feedbacks-${feedback.eventId}" style="display: none;">
                            <div class="other-feedbacks-loading">
                                <i class="fas fa-spinner fa-spin"></i> Đang tải...
                            </div>
                        </div>
                    </div>
                `;
            }).join('');

        } catch (error) {
            console.error('❌ Lỗi load user feedbacks:', error);
            feedbacksList.innerHTML = `
                <div style="text-align: center; padding: 20px; color: #ef4444;">
                    <i class="fas fa-exclamation-triangle"></i>
                    <p style="font-size: 14px; margin-top: 8px;">Không thể tải feedback</p>
                </div>
            `;
        }
    }

    // Format date for display
    function formatDateForDisplay(date) {
        const now = new Date();
        const diffTime = Math.abs(now - date);
        const diffDays = Math.floor(diffTime / (1000 * 60 * 60 * 24));
        
        if (diffDays === 0) {
            return 'Hôm nay';
        } else if (diffDays === 1) {
            return 'Hôm qua';
        } else if (diffDays < 7) {
            return `${diffDays} ngày trước`;
        } else {
            return date.toLocaleDateString('vi-VN');
        }
    }

    // Load feedbacks of others for an event
    window.loadOtherFeedbacks = async function(buttonElement, eventId, eventTitle) {
        const container = document.getElementById(`other-feedbacks-${eventId}`);
        const toggleIcon = buttonElement.querySelector('.toggle-icon');
        
        if (!container) return;
        
        // Get current user ID
        const user = JSON.parse(localStorage.getItem("currentUser"));
        if (!user) {
            console.warn('⚠️ Chưa đăng nhập');
            return;
        }
        const currentUserId = user.user_id;
        
        // Toggle show/hide
        const isHidden = container.style.display === 'none';
        
        if (isHidden) {
            container.style.display = 'block';
            toggleIcon.classList.remove('fa-chevron-down');
            toggleIcon.classList.add('fa-chevron-up');
            
            // Check if already loaded
            if (container.dataset.loaded === 'true') {
                return;
            }
            
            try {
                // Show loading
                container.innerHTML = '<div class="other-feedbacks-loading"><i class="fas fa-spinner fa-spin"></i> Đang tải...</div>';
                
                const response = await fetch(`http://localhost:8080/api/feedback/event/${eventId}`);
                
                if (!response.ok) {
                    throw new Error(`HTTP ${response.status}`);
                }
                
                const allFeedbacks = await response.json();
                
                // Filter out current user's feedback
                const otherFeedbacks = Array.isArray(allFeedbacks) 
                    ? allFeedbacks.filter(f => f.userId != currentUserId) // Use != to handle type coercion
                    : [];
                
                console.log(`📋 Loaded ${otherFeedbacks.length} feedbacks từ người khác cho event ${eventId}`);
                
                if (otherFeedbacks.length === 0) {
                    container.innerHTML = `
                        <div class="other-feedbacks-empty">
                            <i class="fas fa-comments" style="opacity: 0.3;"></i>
                            <p>Chưa có đánh giá nào từ người khác</p>
                        </div>
                    `;
                    container.dataset.loaded = 'true';
                    return;
                }
                
                // Render other feedbacks
                container.innerHTML = `
                    <div class="other-feedbacks-header">
                        <strong>${otherFeedbacks.length} đánh giá từ người khác</strong>
                    </div>
                    ${otherFeedbacks.map(feedback => {
                        const stars = '★'.repeat(feedback.rating || 0) + '☆'.repeat(5 - (feedback.rating || 0));
                        const feedbackDate = feedback.createdAt ? new Date(feedback.createdAt) : new Date();
                        const dateStr = formatDateForDisplay(feedbackDate);
                        
                        return `
                            <div class="other-feedback-item">
                                <div class="other-feedback-header">
                                    <div>
                                        <strong>${feedback.userName || 'Người dùng ẩn danh'}</strong>
                                        <span class="other-feedback-stars">${stars}</span>
                                    </div>
                                    <small>${dateStr}</small>
                                </div>
                                <p class="other-feedback-comment">${feedback.comment || '<em style="color: #94a3b8;">Không có bình luận</em>'}</p>
                            </div>
                        `;
                    }).join('')}
                `;
                
                container.dataset.loaded = 'true';
                
            } catch (error) {
                console.error('❌ Lỗi load other feedbacks:', error);
                container.innerHTML = `
                    <div class="other-feedbacks-error">
                        <i class="fas fa-exclamation-triangle"></i>
                        <p>Không thể tải đánh giá</p>
                    </div>
                `;
            }
        } else {
            // Hide
            container.style.display = 'none';
            toggleIcon.classList.remove('fa-chevron-up');
            toggleIcon.classList.add('fa-chevron-down');
        }
    };

    // Load events khi page load
    document.addEventListener('DOMContentLoaded', () => {
        loadCompletedEvents();
        loadUserFeedbacks();
        
        // Reload feedbacks khi nhận notification về feedback reply
        if (typeof addNotificationToList !== 'undefined') {
            const originalAdd = addNotificationToList;
            window.addNotificationToList = function(notification) {
                originalAdd(notification);
                if (notification.type === 'feedback_reply') {
                    // Reload feedbacks để hiển thị reply mới
                    setTimeout(loadUserFeedbacks, 500);
                }
            };
        }
    });
})();
