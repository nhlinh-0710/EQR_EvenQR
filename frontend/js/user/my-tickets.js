/* =====================================================
   MY TICKETS — EventQR
   FINAL OPTIMIZED VERSION
   ===================================================== */

/* ----------------------------
   GLOBAL STATE
---------------------------- */
let currentFilter = "all";
let allTickets = [];

/* ----------------------------
   DOM READY
---------------------------- */
document.addEventListener("DOMContentLoaded", () => {
    // Update user name in header
    const currentUser = JSON.parse(localStorage.getItem('currentUser'));
    if (currentUser) {
        const userNameElement = document.getElementById('userName');
        if (userNameElement) {
            userNameElement.textContent = currentUser.name || 'Người dùng';
        }
    }
    
    loadMyTickets();
    initTicketFilters();
    initQRButtons();
});

/* =====================================================
   INIT QR BUTTONS (DOWNLOAD / PRINT)
===================================================== */
function initQRButtons() {
    const btnDownload = document.getElementById("btnDownloadQR");
    const btnPrint = document.getElementById("btnPrintQR");

    if (btnDownload) {
        btnDownload.addEventListener("click", () => {
            const canvas = document.querySelector("#qrcode canvas");
            if (!canvas) return alert("QR chưa được tạo!");
            
            const link = document.createElement("a");
            link.download = "qr-code.png";
            link.href = canvas.toDataURL("image/png");
            link.click();
        });
    }

    if (btnPrint) {
        btnPrint.addEventListener("click", () => {
            const canvas = document.querySelector("#qrcode canvas");
            if (!canvas) return alert("QR chưa được tạo!");

            const win = window.open("", "_blank");

            win.document.write(`
                <html>
                    <head><title>In QR</title></head>
                    <body style="text-align:center; margin-top:40px;">
                        <img style="width:260px;" src="${canvas.toDataURL("image/png")}">
                    </body>
                </html>
            `);

            win.document.close();
            win.print();
        });
    }
}

/* =====================================================
   LOAD TICKETS
===================================================== */
async function loadMyTickets() {
    console.log('🚀 loadMyTickets() được gọi');
    
    const user = JSON.parse(localStorage.getItem("currentUser"));
    if (!user) {
        console.warn('⚠️ Không có user trong localStorage');
        return;
    }
    
    console.log('👤 User ID:', user.user_id);

    const grid = document.getElementById("tickets-grid");
    if (!grid) {
        console.error('❌ Không tìm thấy #tickets-grid element!');
        return;
    }
    
    console.log('✅ Tìm thấy #tickets-grid');

    try {
        const url = `http://localhost:8080/api/user/${user.user_id}/tickets`;
        console.log('📡 Fetching tickets from:', url);
        
        const res = await fetch(url);
        console.log('📡 Response status:', res.status, res.statusText);
        
        if (!res.ok) {
            const errorText = await res.text();
            console.error('❌ API Error:', errorText);
            throw new Error("Không thể lấy danh sách vé!");
        }

        const tickets = await res.json();
        console.log('🎫 Tickets từ API:', tickets);
        console.log('🎫 Số lượng vé:', tickets.length);
        
        if (tickets.length > 0) {
            const sample = tickets[0];
            console.log('📋 Sample ticket keys:', Object.keys(sample));
            console.log('📋 eventStatus:', sample.eventStatus);
            console.log('📋 cancelled:', sample.cancelled);
            console.log('📋 Full ticket:', JSON.stringify(sample, null, 2));
        } else {
            console.log('⚠️ Không có vé nào trong response - Hiển thị empty state');
        }
        
        allTickets = tickets;
        console.log('💾 Đã lưu', allTickets.length, 'vé vào allTickets');

        renderTickets();

    } catch (err) {
        console.error("❌ Lỗi load vé:", err);
        grid.innerHTML = `
            <div class="tickets-empty">
                <i class="fas fa-exclamation-triangle"></i>
                <h3>Không thể tải dữ liệu</h3>
                <p>Vui lòng thử lại sau.</p>
            </div>`;
    }
}

/* =====================================================
   RENDER TICKETS
===================================================== */
function renderTickets() {
    console.log('🎨 renderTickets() được gọi');
    console.log('📊 allTickets.length:', allTickets.length);
    console.log('🔍 currentFilter:', currentFilter);
    
    const grid = document.getElementById("tickets-grid");
    if (!grid) {
        console.error('❌ Không tìm thấy #tickets-grid trong renderTickets()!');
        return;
    }
    
    // Đảm bảo grid hiển thị
    grid.style.display = 'grid';
    grid.style.visibility = 'visible';
    grid.style.opacity = '1';
    console.log('🔧 Đã set grid styles: display=grid, visibility=visible, opacity=1');
    
    grid.innerHTML = "";

    let filtered = [...allTickets];
    console.log('📋 Filtered tickets (before filter):', filtered.length);

    if (currentFilter !== "all") {
        filtered = allTickets.filter(t => {
            const status = getTicketStatus(t);
            // Map filter value sang status
            // Tab "upcoming" → status === "upcoming"
            // Tab "ongoing" → status === "ongoing"
            // Tab "ended" → status === "ended"
            // Tab "cancelled" → status === "cancelled" (bao gồm cả user hủy và organizer hủy event)
            const matches = status === currentFilter;
            console.log(`  - Ticket ${t.ticketId}: status=${status}, filter=${currentFilter}, matches=${matches}`);
            return matches;
        });
        console.log('📋 Filtered tickets (after filter):', filtered.length);
    }

    if (filtered.length === 0) {
        console.log('⚠️ Không có vé nào sau khi filter - Hiển thị empty state');
        grid.innerHTML = `
            <div class="tickets-empty">
                <i class="fas fa-ticket-alt"></i>
                <h3>Chưa có vé nào</h3>
                <p>Bạn hãy đăng ký một sự kiện để nhận vé.</p>
            </div>`;
        updateStats();
        return;
    }

    console.log(`✅ Rendering ${filtered.length} tickets (filter: ${currentFilter})`);
    filtered.forEach(t => {
        console.log('🎫 Rendering ticket:', t.ticketId, 'Status:', getTicketStatus(t));
        try {
            createTicketCard(t);
        } catch (error) {
            console.error('❌ Lỗi khi tạo card cho ticket', t.ticketId, ':', error);
        }
    });
    
    updateDateRanges();
    updateStats();
    console.log('✅ Hoàn thành renderTickets()');
}

/* =====================================================
   GET TICKET STATUS - ƯU TIÊN STATUS TỪ BACKEND
   Logic:
   1. Nếu vé bị user hủy (ticket.cancelled === true) → "cancelled"
   2. Nếu event bị organizer hủy (eventStatus === 'CANCELLED') → "cancelled"
   3. Nếu có eventStatus từ backend → map trực tiếp (DRAFT→draft, UPCOMING→upcoming, ONGOING→ongoing, COMPLETED→ended)
   4. Nếu không có eventStatus → tự động xác định dựa trên thời gian
===================================================== */
function getTicketStatus(ticket) {
    // BƯỚC 1: Kiểm tra vé có bị user hủy không
    const isTicketCancelled = ticket.cancelled === true || 
                              ticket.cancelled === "true" ||
                              ticket.cancelled === 1 ||
                              ticket.isCancelled === true ||  // Fallback
                              ticket.isCancelled === "true";
    
    // BƯỚC 2: Kiểm tra eventStatus từ backend (tôn trọng lựa chọn của organizer)
    const eventStatus = (ticket.eventStatus || ticket.event_status || ticket.status || '').toUpperCase();
    
    // BƯỚC 3: Nếu vé bị user hủy HOẶC event bị organizer hủy → trả về "cancelled"
    if (isTicketCancelled || eventStatus === 'CANCELLED') {
        return "cancelled";
    }
    
    // BƯỚC 4: Nếu có event status từ backend, dùng trực tiếp (tôn trọng lựa chọn của organizer)
    if (eventStatus && eventStatus !== 'PUBLISHED') {
        switch(eventStatus) {
            case 'DRAFT': return "draft";
            case 'UPCOMING': return "upcoming";
            case 'ONGOING': return "ongoing";
            case 'COMPLETED': return "ended";
            default:
                // Nếu status không hợp lệ, tiếp tục tự động xác định
                break;
        }
    }
    
    // BƯỚC 5: Tự động xác định dựa trên thời gian (nếu không có status từ backend hoặc là PUBLISHED cũ)
    if (ticket.startTime && ticket.endTime) {
        const now = new Date();
        const startTime = new Date(ticket.startTime);
        const endTime = new Date(ticket.endTime);
        
        if (isNaN(startTime.getTime()) || isNaN(endTime.getTime())) {
            return "unknown";
        }
        
        if (endTime < now) return "ended";
        if (startTime > now) return "upcoming";
        return "ongoing";
    }
    
    return "unknown";
}

/* =====================================================
   CREATE CARD
===================================================== */
function createTicketCard(t) {
    const grid = document.getElementById("tickets-grid");
    if (!grid) {
        console.error('❌ Không tìm thấy #tickets-grid');
        return;
    }
    
    const status = getTicketStatus(t);
    console.log(`🎫 Creating card for ticket ${t.ticketId}: status=${status}, eventStatus=${t.eventStatus}, cancelled=${t.cancelled}`);

    const { text, class: badgeClass, dataStatus } = getStatusInfo(status);

    const card = document.createElement("article");
    card.className = "ticket-card";

    /* Set dataset chuẩn */
    card.dataset.order = `TCK-${t.ticketId}`;
    card.dataset.status = dataStatus;
    card.dataset.start = t.startTime;
    card.dataset.end = t.endTime;
    card.dataset.name = t.eventTitle;
    card.dataset.venue = t.location;
    card.dataset.eventId = t.eventId;
    card.dataset.seatType = t.ticketType ?? "General";
    card.dataset.registered = t.registeredAt;
    card.dataset.phone = t.phone;


    card.innerHTML = `
        <div class="ticket-media">
            <img src="${fixImagePath(t.imageUrl)}" alt="${t.eventTitle}">
            <span class="badge ${badgeClass}">${text}</span>
        </div>

        <div class="ticket-body">
            <h3 class="title">${t.eventTitle}</h3>
            <p class="sub">
                <i class="fas fa-calendar"></i>
                <span data-date-range></span>
            </p>
            <p class="sub"><i class="fas fa-location-dot"></i> ${t.location}</p>
            <div class="meta">
                <span class="code">TCK-${t.ticketId}</span>
                <span class="seat">${t.ticketType}</span>
                <span class="price">Miễn phí</span>
            </div>
        </div>

        <div class="ticket-actions">
            <button class="btn-view"
                onclick="viewTicketDetails('TCK-${t.ticketId}')">
                <i class="fas fa-eye"></i> Xem chi tiết
            </button>

            <button class="btn-qr"
                onclick="openQRModal(${t.ticketId}, '${t.eventTitle.replace(/'/g, "\\'")}', '${t.startTime}', '${t.registeredAt}', ${t.eventId})">
                <i class="fas fa-qrcode"></i> QR Code
            </button>

            ${
                // Chỉ cho phép hủy vé khi status là "upcoming" hoặc "ongoing"
                // Không cho hủy khi đã "ended" hoặc "cancelled"
                (status === "upcoming" || status === "ongoing")
                    ? `<button class="btn-cancel" onclick="cancelTicket(${t.ticketId})">
                        <i class="fas fa-times-circle"></i> Hủy vé
                       </button>`
                    : `<button class="btn-refund" disabled>
                        <i class="fas fa-ban"></i> Không khả dụng
                       </button>`
            }
        </div>
    `;

    try {
        // Đảm bảo card hiển thị
        card.style.display = 'block';
        card.style.visibility = 'visible';
        card.style.opacity = '1';
        card.style.minHeight = '200px';
        card.style.position = 'relative';
        card.style.zIndex = '1';
        
        grid.appendChild(card);
        console.log(`✅ Card đã được append vào grid cho ticket ${t.ticketId}`);
    } catch (error) {
        console.error(`❌ Lỗi khi append card cho ticket ${t.ticketId}:`, error);
    }
}

/* =====================================================
   STATUS INFO
===================================================== */
function getStatusInfo(status) {
    switch (status) {
        case "upcoming": return { text: "Sắp diễn ra", class: "is-upcoming", dataStatus: "upcoming" };
        case "ongoing":  return { text: "Đang diễn ra", class: "is-ongoing", dataStatus: "ongoing" };
        case "ended":    return { text: "Đã kết thúc", class: "is-completed", dataStatus: "ended" };
        case "cancelled":return { text: "Đã hủy", class: "is-cancelled", dataStatus: "cancelled" };
        default: return { text: "Không xác định", class: "is-upcoming", dataStatus: "unknown" };
    }
}

/* =====================================================
   SEARCH & FILTER
===================================================== */
function initTicketFilters() {
    const tabs = document.querySelectorAll('.tickets-toolbar .tab[data-filter]');

    tabs.forEach(tab => {
        tab.addEventListener("click", () => {
            tabs.forEach(t => {
                t.classList.remove("active");
                t.setAttribute('aria-selected', 'false');
            });
            tab.classList.add("active");
            tab.setAttribute('aria-selected', 'true');

            currentFilter = tab.dataset.filter;
            console.log('🔍 Filter changed to:', currentFilter);
            renderTickets();
        });
    });

    const searchInput = document.getElementById("tk-search");
    if (searchInput) {
        searchInput.addEventListener("input", () => {
            filterTicketsBySearch(searchInput.value.toLowerCase());
        });
    }
}

function filterTicketsBySearch(term) {
    const cards = document.querySelectorAll(".ticket-card");
    cards.forEach(card => {
        const name = card.dataset.name.toLowerCase();
        const code = card.dataset.order.toLowerCase();
        const venue = card.dataset.venue.toLowerCase();

        card.style.display =
            name.includes(term) || code.includes(term) || venue.includes(term)
                ? "block" : "none";
    });
}

/* =====================================================
   DATE RANGE
===================================================== */
function formatDateRange(start, end) {
    const s = new Date(start);
    const e = new Date(end);

    const f = d =>
        `${String(d.getDate()).padStart(2, '0')}/${String(d.getMonth() + 1).padStart(2, '0')}/${d.getFullYear()}
         — ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`;

    return `${f(s)} → ${f(e)}`;
}

function updateDateRanges() {
    document.querySelectorAll("[data-date-range]").forEach(el => {
        const card = el.closest(".ticket-card");
        el.textContent = formatDateRange(card.dataset.start, card.dataset.end);
    });
}

/* =====================================================
   STATS - Đếm số vé theo từng trạng thái
   Logic: Đếm từ allTickets (tất cả vé, không phụ thuộc filter hiện tại)
===================================================== */
function updateStats() {
    let upcoming = 0, ongoing = 0, ended = 0, canceled = 0;

    allTickets.forEach(t => {
        const st = getTicketStatus(t);
        // Đếm theo status
        if (st === "cancelled") {
            canceled++;
        } else if (st === "ended") {
            ended++;
        } else if (st === "ongoing") {
            ongoing++;
        } else if (st === "upcoming") {
            upcoming++;
        }
        // Bỏ qua "draft" và "unknown"
    });

    const statUpcoming = document.getElementById("stat-upcoming");
    const statOngoing = document.getElementById("stat-ongoing");
    const statEnded = document.getElementById("stat-ended");
    const statCanceled = document.getElementById("stat-canceled");
    
    if (statUpcoming) statUpcoming.textContent = upcoming;
    if (statOngoing) statOngoing.textContent = ongoing;
    if (statEnded) statEnded.textContent = ended;
    if (statCanceled) statCanceled.textContent = canceled;
    
    console.log('📊 Stats updated:', { upcoming, ongoing, ended, canceled });
}

/* =====================================================
   TICKET DETAIL MODAL
===================================================== */
function viewTicketDetails(ticketCode) {
    const card = [...document.querySelectorAll(".ticket-card")]
        .find(c => c.dataset.order === ticketCode);

    if (!card) return alert("Không tìm thấy vé!");

    const eventName = card.dataset.name;
    const venue = card.dataset.venue;
    const start = card.dataset.start;
    const end = card.dataset.end;
    const seatType = card.dataset.seatType;
    const status = card.dataset.status;
    const registeredAt = card.dataset.registered;
    const eventId = card.dataset.eventId;
    const phone = card.dataset.phone || "Không có";   
    const ticketId = ticketCode.replace("TCK-", "");

    const user = JSON.parse(localStorage.getItem("currentUser"));

    // QR
    const qrPayload = generateUniqueQR(ticketId, eventId, user.user_id, registeredAt);
    const temp = document.createElement("div");

    new QRCode(temp, { text: qrPayload, width: 240, height: 240 });

    const canvas = temp.querySelector("canvas");
    const qrImage = canvas.toDataURL("image/png");

    // Fill modal
    document.getElementById("modal-event-name").textContent = eventName;
    document.getElementById("modal-order-id").textContent = ticketCode;
    document.getElementById("modal-seat").textContent = seatType;
    document.getElementById("modal-price").textContent = "Miễn phí";

    document.getElementById("modal-phone").textContent = phone; 

    const statusText =
        status === "upcoming" ? "Sắp diễn ra" :
        status === "ongoing" ? "Đang diễn ra" :
        status === "ended" ? "Đã kết thúc" :
        status === "cancelled" ? "Đã hủy" : "Không xác định";

    document.getElementById("modal-ticket-status").textContent = statusText;
    document.getElementById("modal-datetime").textContent = formatDateRange(start, end);
    document.getElementById("modal-venue").textContent = venue;
    document.getElementById("modal-attendee").textContent = user.name;
    document.getElementById("modal-email").textContent = user.email;

    document.getElementById("modal-qrcode").innerHTML =
        `<img src="${qrImage}" style="width:220px; border-radius:12px;">`;

    document.getElementById("ticketDetailModal").style.display = "flex";
}

function closeTicketDetail() {
    document.getElementById("ticketDetailModal").style.display = "none";
}

/* =====================================================
   QR MODAL (CHECK-IN)
===================================================== */
function openQRModal(ticketId, eventName, startTime, registeredAt, eventId) {
    const qrBox = document.getElementById("qrcode");
    qrBox.innerHTML = "";

    const user = JSON.parse(localStorage.getItem("currentUser"));

    const qrPayload = generateUniqueQR(ticketId, eventId, user.user_id, registeredAt);
    const shortString = shortenQR(ticketId, eventId, user.user_id);

    new QRCode(qrBox, {
        text: qrPayload,
        width: 340,
        height: 340
    });

    document.getElementById("qrCreatedDate").textContent =
        new Date(registeredAt).toLocaleDateString("vi-VN");
    document.getElementById("qrCreatedTime").textContent =
        new Date(registeredAt).toLocaleTimeString("vi-VN");

    document.getElementById("qrEventName").textContent =
        `${eventName} - ${new Date(startTime).toLocaleDateString("vi-VN")}`;

    document.getElementById("qrCodeText").textContent = shortString;

    document.getElementById("qrModal").style.display = "flex";
}

function closeQR() {
    document.getElementById("qrModal").style.display = "none";
}

/* =====================================================
   QR UTILS
===================================================== */
function generateUniqueQR(ticketId, eventId, userId, registeredAt) {
    const payload = {
        ticket_id: Number(ticketId),
        event_id: Number(eventId),
        user_id: Number(userId),
        registered_at: registeredAt,
        nonce: Math.random().toString(36).substring(2, 12)
    };
    return btoa(JSON.stringify(payload)); // Encode to Base64
}

function shortenQR(ticketId, eventId, userId) {
    return `#${ticketId}-E${eventId}-U${userId}`;
}

/* =====================================================
   IMAGE FIX
===================================================== */
function fixImagePath(path) {
    // Default fallback image
    if (!path || path === "null") return "../../images/default-event.png";
    
    // Nếu đã là full URL
    if (path.startsWith('http://') || path.startsWith('https://')) {
        return path;
    }
    
    // Nếu là đường dẫn tương đối bắt đầu bằng /
    if (path.startsWith('/')) {
        // Ghép với base URL của backend
        return 'http://localhost:8080' + path;
    }
    
    // Nếu là đường dẫn tương đối khác
    return 'http://localhost:8080/images/' + path;
}

/* =====================================================
   CANCEL TICKET
===================================================== */
async function cancelTicket(ticketId) {
    if (!confirm("Bạn có chắc muốn hủy vé?")) return;

    const user = JSON.parse(localStorage.getItem("currentUser"));

    try {
        const res = await fetch(`http://localhost:8080/api/user/${user.user_id}/tickets/${ticketId}`, {
            method: "DELETE"
        });

        const data = await res.json();

        if (data.success) {
            alert("Hủy vé thành công!");
            // Reload lại danh sách vé để cập nhật status
            await loadMyTickets();
            
            // Tự động chuyển sang tab "Đã hủy" để user thấy vé vừa hủy
            const cancelledTab = document.querySelector('.tickets-toolbar .tab[data-filter="cancelled"]');
            if (cancelledTab) {
                cancelledTab.click();
            }
        } else {
            alert(data.message || "Hủy vé thất bại!");
        }

    } catch (e) {
        alert("Không thể kết nối server!");
    }
}
