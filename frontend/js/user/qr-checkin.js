// =====================================================
// QR CHECK-IN — BẢN HOÀN CHỈNH CHUẨN EVENTQR
// =====================================================

let qrScannerInterval = null;
let currentQRPayload = null;

// Khi DOM load
document.addEventListener("DOMContentLoaded", () => {
    // Update user name in header
    const currentUser = JSON.parse(localStorage.getItem('currentUser'));
    if (currentUser) {
        const userNameElement = document.getElementById('userName');
        if (userNameElement) {
            userNameElement.textContent = currentUser.name || 'Người dùng';
        }
    }
    
    setupTabs();
    loadCheckinHistory();
    updateCheckinStats();
    loadOrganizerEvents(); // Load các sự kiện của organizer
});

// =====================================================
// TAB SWITCHER
// =====================================================
function setupTabs() {
    const tabs = document.querySelectorAll(".qr-tab");
    const contents = document.querySelectorAll(".qr-tab-content");

    if (tabs.length === 0 || contents.length === 0) {
        console.log("ℹ️ Không tìm thấy tabs trên trang này, bỏ qua setupTabs");
        return;
    }

    tabs.forEach(tab => {
        tab.addEventListener("click", () => {
            const name = tab.dataset.tab;

            tabs.forEach(t => t.classList.remove("active"));
            contents.forEach(c => c.classList.remove("active"));

            tab.classList.add("active");
            const tabContent = document.getElementById(name + "-tab");
            if (tabContent) {
                tabContent.classList.add("active");
            }

            // Rời tab Scan thì tắt camera
            if (name !== "scan") stopQRScanner();
        });
    });
}

// =====================================================
// QUÉT QR TỪ CAMERA
// =====================================================
async function startQRScanner() {
    const video = document.getElementById("qrVideo");
    const startBtn = document.getElementById("startScanBtn");
    const stopBtn = document.getElementById("stopScanBtn");

    if (!video || !startBtn || !stopBtn) {
        console.log("ℹ️ Các element QR scanner không tồn tại trên trang này");
        return;
    }

    try {
        const stream = await navigator.mediaDevices.getUserMedia({
            video: { facingMode: "environment" }
        });

        video.srcObject = stream;
        video.play();
        video.style.display = "block";

        startBtn.style.display = "none";
        stopBtn.style.display = "inline-flex";

        scanVideoLoop(video);

        showNotification("Máy quét QR đã bật!", "success");
    } catch (e) {
        showNotification("Không thể bật camera!", "error");
    }
}

// Dừng quét
function stopQRScanner() {
    const video = document.getElementById("qrVideo");
    const startBtn = document.getElementById("startScanBtn");
    const stopBtn = document.getElementById("stopScanBtn");

    if (!video) {
        console.log("ℹ️ Element qrVideo không tồn tại");
        return;
    }

    if (video.srcObject) {
        video.srcObject.getTracks().forEach(t => t.stop());
    }

    video.srcObject = null;
    video.style.display = "none";

    if (startBtn) startBtn.style.display = "inline-flex";
    if (stopBtn) stopBtn.style.display = "none";

    clearInterval(qrScannerInterval);
}

// Loop lấy hình & decode
function scanVideoLoop(video) {
    const canvas = document.createElement("canvas");
    const ctx = canvas.getContext("2d");

    qrScannerInterval = setInterval(() => {
        if (!video.srcObject) return;

        canvas.width = video.videoWidth;
        canvas.height = video.videoHeight;

        ctx.drawImage(video, 0, 0, canvas.width, canvas.height);
        const img = ctx.getImageData(0, 0, canvas.width, canvas.height);

        const qr = jsQR(img.data, img.width, img.height);

        if (qr) {
            stopQRScanner();
            onQRDetected(qr.data);
        }
    }, 250);
}

// =====================================================
// XỬ LÝ QR SAU KHI QUÉT
// =====================================================
async function onQRDetected(qrString) {
    let decoded;

    try {
        decoded = JSON.parse(atob(qrString)); // Base64 JSON đúng chuẩn của bạn
    } catch (e) {
        return showNotification("QR không hợp lệ!", "error");
    }

    currentQRPayload = decoded;

    // Gửi đến Backend đúng chuẩn
    try {
        const url = `http://localhost:8080/api/checkin?payload=${encodeURIComponent(qrString)}`;
        const res = await fetch(url);

        if (!res.ok) throw new Error("Không tìm thấy vé!");

        const data = await res.json();

        updateScanResultSuccess(data);
        fillCheckinForm(data);

    } catch (e) {
        return showNotification("QR không tồn tại trong hệ thống!", "error");
    }
}

// Hiển thị khi quét thành công
function updateScanResultSuccess(data) {
    const scanResult = document.getElementById("qrScanResult");
    if (!scanResult) {
        console.log("ℹ️ Element qrScanResult không tồn tại");
        return;
    }
    scanResult.style.display = "block";
    scanResult.innerHTML = `
        <div class="scan-success">
            <i class="fas fa-check-circle" style="color:#10b981;font-size:2rem;margin-bottom:10px"></i>
            <p><strong>QR hợp lệ!</strong></p>
            <p>Vé: TCK-${data.ticket.ticketId}</p>
            <p>Sự kiện: ${data.event.title}</p>
        </div>
    `;
}

// =====================================================
// NHẬP MÃ TAY
// =====================================================
function processManualQRCode() {
    const manualQRCodeEl = document.getElementById("manualQRCode");
    if (!manualQRCodeEl) {
        console.log("ℹ️ Element manualQRCode không tồn tại");
        return;
    }
    const code = manualQRCodeEl.value.trim();
    if (!code) return showNotification("Chưa nhập mã!", "error");

    fetch(`http://localhost:8080/api/checkin-by-code?code=${encodeURIComponent(code)}`)
        .then(res => res.json())
        .then(data => {
            if (data.success === false) {
                return showNotification(data.message, "error");
            }

            updateScanResultSuccess(data);
            fillCheckinForm(data);
        })
        .catch(() => showNotification("Mã QR không hợp lệ!", "error"));
}

// =====================================================
// ĐỔ FORM CHECK-IN
// =====================================================
function fillCheckinForm(data) {
    const form = document.getElementById("checkinForm");
    const participantName = document.getElementById("participantName");
    const participantEmail = document.getElementById("participantEmail");
    const participantPhone = document.getElementById("participantPhone");
    const checkinEventName = document.getElementById("checkinEventName");
    const checkinTime = document.getElementById("checkinTime");

    if (!form || !participantName || !participantEmail || !participantPhone || !checkinEventName || !checkinTime) {
        console.log("ℹ️ Các element form check-in không tồn tại trên trang này");
        return;
    }

    participantName.value = data.user.name;
    participantEmail.value = data.user.email;
    participantPhone.value = data.phone || data.ticket.phone || "";

    checkinEventName.textContent = data.event.title;
    checkinTime.textContent = new Date().toLocaleString("vi-VN");

    form.dataset.apiData = JSON.stringify(data);

    form.style.display = "block";
    form.scrollIntoView({ behavior: "smooth" });
}

// =====================================================
// XÁC NHẬN CHECK-IN
// =====================================================
async function confirmCheckin() {
    const form = document.getElementById("checkinForm");
    const participantName = document.getElementById("participantName");
    const participantEmail = document.getElementById("participantEmail");
    const participantPhone = document.getElementById("participantPhone");

    if (!form || !participantName || !participantEmail || !participantPhone) {
        console.log("ℹ️ Các element form không tồn tại");
        return;
    }

    const data = JSON.parse(form.dataset.apiData);

    const name = participantName.value.trim();
    const email = participantEmail.value.trim();
    const phone = participantPhone.value.trim();

    if (!name || !email) return showNotification("Nhập đầy đủ thông tin!", "error");

    // Backend đã lưu check-in rồi (khi gọi /api/checkin hoặc /api/checkin-by-code)
    // Giờ chỉ cần reload lại danh sách
    
    showNotification("Check-in thành công!", "success");
    cancelCheckin();
    
    // Reload lại lịch sử và thống kê từ API
    await loadCheckinHistory();
    await updateCheckinStats();
}

function cancelCheckin() {
    const checkinForm = document.getElementById("checkinForm");
    const qrScanResult = document.getElementById("qrScanResult");
    const manualQRCode = document.getElementById("manualQRCode");
    
    if (checkinForm) checkinForm.style.display = "none";
    if (qrScanResult) qrScanResult.style.display = "none";
    if (manualQRCode) manualQRCode.value = "";
}

// =====================================================
// LỊCH SỬ CHECK-IN - GỌI TỪ API
// =====================================================
function storeCheckinRecord(record) {
    // Không còn lưu localStorage nữa - backend tự lưu rồi
    console.log("✅ Check-in đã được lưu vào database");
}

async function loadCheckinHistory() {
    // Kiểm tra xem element có tồn tại không (tránh lỗi khi load ở trang khác)
    const body = document.getElementById("checkinTableBody");
    if (!body) {
        console.log("ℹ️ Element checkinTableBody không tồn tại trên trang này, bỏ qua loadCheckinHistory");
        return;
    }
    
    // Lấy organizerId từ localStorage
    const currentUser = localStorage.getItem("currentUser");
    if (!currentUser) {
        console.warn("⚠️ Chưa đăng nhập");
        showNotification("Vui lòng đăng nhập để xem lịch sử check-in", "error");
        return;
    }
    
    let organizerId;
    try {
        const userData = JSON.parse(currentUser);
        organizerId = userData.user_id;
        
        console.log("👤 User data:", userData);
        console.log("🆔 Organizer ID:", organizerId);
        
        if (!organizerId) {
            console.warn("⚠️ Không tìm thấy organizerId");
            showNotification("Không tìm thấy thông tin organizer", "error");
            return;
        }
    } catch (error) {
        console.error("❌ Lỗi parse user data:", error);
        showNotification("Lỗi xác thực người dùng", "error");
        return;
    }
    
    try {
        // Gọi API lấy check-in history
        const url = `http://localhost:8080/api/checkin-history?organizerId=${organizerId}`;
        console.log("🌐 Gọi API:", url);
        
        const response = await fetch(url, {
            method: "GET",
            headers: {
                "Content-Type": "application/json"
            }
        });
        
        console.log("📡 Response status:", response.status);
        console.log("📡 Response OK:", response.ok);
        
        if (!response.ok) {
            const errorText = await response.text();
            console.error("❌ Error response:", errorText);
            throw new Error(`HTTP ${response.status}: ${errorText || 'Không thể tải lịch sử check-in'}`);
        }
        
        const checkins = await response.json();
        console.log("✅ Đã tải", checkins.length, "check-in từ server");
        console.log("📦 Dữ liệu check-in:", checkins);
        
        // Clear bảng và load lại
        body.innerHTML = "";
        
        if (checkins.length === 0) {
            body.innerHTML = `
                <tr>
                    <td colspan="6" style="text-align: center; padding: 30px; color: #999;">
                        <i class="fas fa-inbox" style="font-size: 2rem; margin-bottom: 10px; display: block;"></i>
                        Chưa có check-in nào
                    </td>
                </tr>
            `;
        } else {
            // Hiển thị từng check-in
            checkins.forEach(checkin => {
                addCheckinToHistoryFromAPI(checkin);
            });
        }
        
    } catch (error) {
        console.error("❌ Lỗi khi tải lịch sử check-in:", error);
        showNotification("Không thể tải lịch sử check-in: " + error.message, "error");
    }
}

function addCheckinToHistory(rec, scroll = true) {
    const body = document.getElementById("checkinTableBody");

    const row = document.createElement("tr");
    row.innerHTML = `
        <td>${rec.participant.name}</td>
        <td>${rec.participant.email}</td>
        <td>${rec.eventName}</td>
        <td>${new Date(rec.checkinTime).toLocaleString("vi-VN")}</td>
        <td><span class="status-badge success">Đã check-in</span></td>
        <td><button class="btn btn-sm btn-outline" onclick="viewCheckin('${rec.id}')">Chi tiết</button></td>
    `;

    body.prepend(row);

    if (scroll) row.scrollIntoView({ behavior: "smooth", block: "center" });
}

// Function mới để thêm check-in từ API
function addCheckinToHistoryFromAPI(checkin) {
    const body = document.getElementById("checkinTableBody");
    
    const row = document.createElement("tr");
    row.dataset.eventId = checkin.eventId; // Lưu eventId để lọc
    row.dataset.checkinId = checkin.checkinId;
    
    row.innerHTML = `
        <td>${checkin.userName}</td>
        <td>${checkin.userEmail}</td>
        <td>${checkin.eventName}</td>
        <td>${checkin.checkinTime}</td>
        <td><span class="status-badge success">Đã check-in</span></td>
        <td><button class="btn btn-sm btn-outline" onclick="viewCheckinFromAPI(${checkin.checkinId})">Chi tiết</button></td>
    `;
    
    body.appendChild(row);
}

function viewCheckin(id) {
    const list = JSON.parse(localStorage.getItem("checkinHistory") || "[]");
    const rec = list.find(r => r.id == id);
    if (!rec) return;

    alert(`
Tên: ${rec.participant.name}
Email: ${rec.participant.email}
SĐT: ${rec.participant.phone}
Sự kiện: ${rec.eventName}
Thời gian: ${new Date(rec.checkinTime).toLocaleString("vi-VN")}
`);
}

// Function mới để xem chi tiết check-in từ API
function viewCheckinFromAPI(checkinId) {
    const row = document.querySelector(`tr[data-checkin-id="${checkinId}"]`);
    if (!row) return;
    
    const cells = row.querySelectorAll("td");
    alert(`
Tên: ${cells[0].textContent}
Email: ${cells[1].textContent}
Sự kiện: ${cells[2].textContent}
Thời gian check-in: ${cells[3].textContent}
Trạng thái: Đã check-in
`);
}

// =====================================================
// THỐNG KÊ - TỪ API
// =====================================================
async function updateCheckinStats() {
    // Kiểm tra xem các element có tồn tại không
    const totalCheckinsEl = document.getElementById("totalCheckins");
    const todayCheckinsEl = document.getElementById("todayCheckins");
    const activeEventsEl = document.getElementById("activeEvents");
    
    if (!totalCheckinsEl || !todayCheckinsEl || !activeEventsEl) {
        console.log("ℹ️ Các element thống kê không tồn tại trên trang này, bỏ qua updateCheckinStats");
        return;
    }
    
    const currentUser = localStorage.getItem("currentUser");
    if (!currentUser) return;
    
    try {
        const userData = JSON.parse(currentUser);
        const organizerId = userData.user_id;
        
        const url = `http://localhost:8080/api/checkin-history?organizerId=${organizerId}`;
        const response = await fetch(url);
        
        if (!response.ok) throw new Error("Không thể tải thống kê");
        
        const checkins = await response.json();
        
        // Tổng check-in
        totalCheckinsEl.textContent = checkins.length;
        
        // Check-in hôm nay
        const today = new Date().toLocaleDateString("vi-VN");
        const todayCheckins = checkins.filter(c => {
            // Format từ API: "dd/MM/yyyy HH:mm:ss"
            const checkinDate = c.checkinTime.split(" ")[0]; // Lấy phần ngày
            return checkinDate === today.split("/").reverse().join("/");
        }).length;
        todayCheckinsEl.textContent = todayCheckins;
        
        // Số sự kiện unique
        const uniqueEvents = new Set(checkins.map(c => c.eventId));
        activeEventsEl.textContent = uniqueEvents.size;
        
    } catch (error) {
        console.error("Lỗi khi cập nhật thống kê:", error);
    }
}

// =====================================================
// LOAD SỰ KIỆN CỦA ORGANIZER
// =====================================================
async function loadOrganizerEvents() {
    const eventFilter = document.getElementById("historyEventFilter");
    if (!eventFilter) return;
    
    // Lấy organizerId từ localStorage
    const currentUser = localStorage.getItem("currentUser");
    if (!currentUser) {
        console.warn("Chưa đăng nhập");
        return;
    }
    
    let organizerId;
    try {
        const userData = JSON.parse(currentUser);
        organizerId = userData.user_id;
        
        if (!organizerId) {
            console.warn("Không tìm thấy organizerId");
            return;
        }
    } catch (error) {
        console.error("Lỗi parse user data:", error);
        return;
    }
    
    // Hiển thị loading state
    eventFilter.innerHTML = '<option value="">Đang tải sự kiện...</option>';
    eventFilter.disabled = true;
    
    try {
        // API đúng: /api/events/my-events
        const url = `http://localhost:8080/api/events/my-events?organizerId=${organizerId}`;
        
        const response = await fetch(url, {
            method: "GET",
            headers: {
                "Content-Type": "application/json"
            }
        });
        
        if (!response.ok) {
            throw new Error(`HTTP ${response.status}: Không thể tải danh sách sự kiện`);
        }
        
        const events = await response.json();
        console.log("✅ Đã tải được", events.length, "sự kiện");
        console.log("📦 Dữ liệu events:", events);
        
        // Kiểm tra cấu trúc event đầu tiên
        if (events.length > 0) {
            console.log("🔍 Event đầu tiên:", {
                eventId: events[0].eventId,
                title: events[0].title,
                "có eventId?": events[0].eventId !== undefined
            });
        }
        
        // Xóa loading và populate dropdown
        eventFilter.innerHTML = '<option value="">Tất cả sự kiện</option>';
        eventFilter.disabled = false;
        
        if (events.length === 0) {
            const option = document.createElement("option");
            option.value = "";
            option.textContent = "Chưa có sự kiện nào";
            option.disabled = true;
            eventFilter.appendChild(option);
        } else {
            // Thêm các sự kiện thực tế - SỬA: event.id → event.eventId
            events.forEach(event => {
                const option = document.createElement("option");
                option.value = event.eventId; // ✅ SỬA: Dùng eventId thay vì id
                option.textContent = event.title;
                eventFilter.appendChild(option);
                
                console.log("✅ Thêm option:", event.title, "với eventId:", event.eventId);
            });
            
            console.log("📋 Tổng số option trong dropdown:", eventFilter.options.length);
            
            // Cập nhật eventId cho các check-in cũ dựa trên tên sự kiện
            updateOldCheckinWithEventId(events);
        }
        
    } catch (error) {
        console.error("❌ Lỗi khi tải sự kiện:", error);
        eventFilter.innerHTML = '<option value="">Lỗi tải sự kiện</option>';
        eventFilter.disabled = false;
        showNotification("Không thể tải danh sách sự kiện: " + error.message, "error");
    }
}

// =====================================================
// CẬP NHẬT EVENT ID CHO CHECK-IN CŨ - KHÔNG CẦN NỮA
// =====================================================
function updateOldCheckinWithEventId(events) {
    // Không còn dùng localStorage nữa, không cần function này
    console.log("ℹ️ Không còn sử dụng localStorage cho check-in");
}

// =====================================================
// LỌC LỊCH SỬ - SỬ DỤNG DOM FILTER
// =====================================================
function filterCheckinHistory() {
    const eventFilterElement = document.getElementById("historyEventFilter");
    const dateFilterElement = document.getElementById("historyDateFilter");
    const body = document.getElementById("checkinTableBody");
    
    // Kiểm tra các element có tồn tại không
    if (!eventFilterElement || !dateFilterElement || !body) {
        console.log("ℹ️ Các element filter không tồn tại trên trang này, bỏ qua filterCheckinHistory");
        return;
    }
    
    const eventFilter = eventFilterElement.value;
    const dateFilter = dateFilterElement.value;
    
    const allRows = body.querySelectorAll("tr[data-event-id]");
    
    console.log("===========================================");
    console.log("🔍 BẮT ĐẦU LỌC");
    console.log("📌 Filter - Sự kiện ID:", eventFilter);
    console.log("📌 Filter - Ngày:", dateFilter);
    console.log("📊 Tổng số row:", allRows.length);
    console.log("-------------------------------------------");
    
    let visibleCount = 0;
    
    allRows.forEach(row => {
        const rowEventId = row.dataset.eventId;
        const rowCheckinTime = row.querySelectorAll("td")[3].textContent; // Cột thời gian
        
        let showRow = true;
        
        // Lọc theo sự kiện
        if (eventFilter) {
            if (String(rowEventId) !== String(eventFilter)) {
                showRow = false;
            }
        }
        
        // Lọc theo ngày
        if (dateFilter && showRow) {
            // dateFilter format: "YYYY-MM-DD"
            // rowCheckinTime format: "dd/MM/yyyy HH:mm:ss"
            const checkinDate = rowCheckinTime.split(" ")[0]; // "dd/MM/yyyy"
            const [day, month, year] = checkinDate.split("/");
            const rowDateFormatted = `${year}-${month}-${day}`; // Convert sang "YYYY-MM-DD"
            
            if (rowDateFormatted !== dateFilter) {
                showRow = false;
            }
        }
        
        // Hiển thị hoặc ẩn row
        row.style.display = showRow ? "" : "none";
        if (showRow) visibleCount++;
    });
    
    console.log("✅ Kết quả hiển thị:", visibleCount, "record");
    console.log("===========================================");
    
    // Hiển thị thông báo nếu không có kết quả
    if (visibleCount === 0) {
        body.innerHTML = `
            <tr>
                <td colspan="6" style="text-align: center; padding: 30px; color: #999;">
                    <i class="fas fa-search" style="font-size: 2rem; margin-bottom: 10px; display: block;"></i>
                    Không tìm thấy kết quả phù hợp
                </td>
            </tr>
        `;
    }
    
    showNotification(`Tìm thấy ${visibleCount} kết quả`, "success");
}

function resetFilters() {
    const eventFilterElement = document.getElementById("historyEventFilter");
    const dateFilterElement = document.getElementById("historyDateFilter");
    const body = document.getElementById("checkinTableBody");
    
    // Kiểm tra các element có tồn tại không
    if (!eventFilterElement || !dateFilterElement || !body) {
        console.log("ℹ️ Các element filter không tồn tại trên trang này, bỏ qua resetFilters");
        return;
    }
    
    eventFilterElement.value = "";
    dateFilterElement.value = "";
    
    // Hiển thị lại tất cả các row
    const allRows = body.querySelectorAll("tr[data-event-id]");
    
    allRows.forEach(row => {
        row.style.display = "";
    });
    
    showNotification("Đã đặt lại bộ lọc", "info");
}

// =====================================================
// THÔNG BÁO UI
// =====================================================
function showNotification(msg, type = "info") {
    const box = document.createElement("div");
    box.className = `notification notification-${type}`;

    box.innerHTML = `
        <i class="fas fa-info-circle"></i>
        <span>${msg}</span>
    `;

    box.style.cssText = `
        position: fixed;
        top: 20px; right: 20px;
        background: white;
        padding: 12px 18px;
        border-left: 4px solid ${
            type === "success"
                ? "#10b981"
                : type === "error"
                ? "#ef4444"
                : "#3b82f6"
        };
        border-radius: 6px;
        box-shadow: 0 0 15px rgba(0,0,0,0.1);
        z-index: 99999;
    `;

    document.body.appendChild(box);
    setTimeout(() => box.remove(), 3000);
}

// =====================================================
// DEBUG: Xem dữ liệu check-in từ API
// =====================================================
async function debugCheckinData() {
    const currentUser = localStorage.getItem("currentUser");
    if (!currentUser) {
        console.log("❌ Chưa đăng nhập");
        return;
    }
    
    try {
        const userData = JSON.parse(currentUser);
        const organizerId = userData.user_id;
        
        const url = `http://localhost:8080/api/checkin-history?organizerId=${organizerId}`;
        const response = await fetch(url);
        const checkins = await response.json();
        
        if (checkins.length === 0) {
            console.log("❌ Chưa có dữ liệu check-in nào");
            return;
        }
        
        console.log("📊 DANH SÁCH CHECK-IN:", checkins.length, "record");
        console.table(checkins.map(c => ({
            "ID": c.checkinId,
            "Tên": c.userName,
            "Email": c.userEmail,
            "Sự kiện": c.eventName,
            "Event ID": c.eventId,
            "Thời gian": c.checkinTime
        })));
        
        console.log("📦 Raw data:", checkins);
    } catch (error) {
        console.error("❌ Lỗi:", error);
    }
}
// Gọi trong console: debugCheckinData()
