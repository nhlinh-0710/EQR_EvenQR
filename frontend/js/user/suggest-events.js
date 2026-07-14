// ======================================
// 🎯 GỢI Ý SỰ KIỆN – HOÀN CHỈNH FULL API
// ======================================

document.addEventListener("DOMContentLoaded", () => {

  // Update user name in header
  const currentUser = JSON.parse(localStorage.getItem('currentUser'));
  if (currentUser) {
      const userNameElement = document.getElementById('userName');
      if (userNameElement) {
          userNameElement.textContent = currentUser.name || 'Người dùng';
      }
  }

  // =============================
  // 1. STATE
  // =============================
  let events = [];
  let currentPage = 1;
  let searchTerm = "";
  let tabFilter = "all";
  let onlyRecent = false;
  let selectedTicketType = "REGULAR";
  let selectedEventId = null;

  const PAGE_SIZE = 6;

  // =============================
  // 2. DOM
  // =============================
  const eventGrid = document.getElementById("eventGrid");
  const paginationEl = document.querySelector(".pagination");
  const searchInput = document.querySelector(".search-input");
  const filterRecentCheckbox = document.getElementById("filterRecent");
  const tabs = document.querySelectorAll(".tabs .tab");
  const totalAmountEl = document.getElementById("totalAmount");

  if (!eventGrid) {
    console.warn("Không tìm thấy #eventGrid – script suggest-events.js đang chạy ở trang khác.");
    return;
  }

  // =============================
  // 3. API LẤY SỰ KIỆN
  // =============================
  async function loadEvents() {
    try {
      const res = await fetch("http://localhost:8080/api/events");
      if (!res.ok) {
        throw new Error("HTTP " + res.status);
      }
      const data = await res.json();

      
      events = data.map(ev => ({
        id: ev.eventId,                                // dùng eventId từ backend
        title: ev.title,
        location: ev.location || "Không rõ",
        category: ev.category || "Khác",
        image: ev.imageUrl,
        startTime: ev.startTime,
        endTime: ev.endTime,
        status: ev.status || null,                    
        price: ev.price || 0,                          
      }));

      renderEvents();
    } catch (err) {
      console.error("Lỗi API /api/events:", err);
      eventGrid.innerHTML = "<p style='color:red;'>Không thể tải dữ liệu sự kiện.</p>";
    }
  }

  // =============================
  // FORMAT
  // =============================
  function formatDateRange(start, end) {
    if (!start || !end) return "Không rõ thời gian";

    const s = new Date(start);
    const e = new Date(end);

    if (isNaN(s.getTime()) || isNaN(e.getTime())) {
      return "Không rõ thời gian";
    }

    const f = d =>
      `${d.getHours().toString().padStart(2, "0")}:${d.getMinutes().toString().padStart(2, "0")} ${d.getDate()}/${d.getMonth() + 1}/${d.getFullYear()}`;

    return `${f(s)} → ${f(e)}`;
  }

  function getEventStatus(ev) {
    // QUAN TRỌNG: Ưu tiên status từ database (do organizer đã chỉnh)
    const backendStatus = (ev.status || '').toUpperCase();
    
    // Nếu có status từ backend, dùng trực tiếp (tôn trọng lựa chọn của organizer)
    if (backendStatus) {
      switch(backendStatus) {
        case 'DRAFT': return "draft";
        case 'UPCOMING': return "upcoming";
        case 'ONGOING': return "ongoing";
        case 'COMPLETED': return "ended";
        case 'CANCELLED': return "cancelled";
        // Fallback cho status cũ
        case 'PUBLISHED':
          // Nếu là PUBLISHED cũ, tự động xác định dựa trên thời gian
          break;
        default:
          // Nếu status không hợp lệ, tự động xác định
          break;
      }
    }
    
    // Chỉ tự động xác định nếu không có status từ backend hoặc là PUBLISHED cũ
    const now = new Date();
    if (ev.startTime && ev.endTime) {
      const s = new Date(ev.startTime);
      const e = new Date(ev.endTime);

      if (isNaN(s.getTime()) || isNaN(e.getTime())) {
        return "unknown";
      }

      if (e < now) return "ended";
      if (s > now) return "upcoming";
      return "ongoing";
    }
    
    return "unknown";
  }

  function statusLabel(s) {
    switch(s) {
      case "draft": return "Bản nháp";
      case "upcoming": return "Sắp diễn ra";
      case "ongoing": return "Đang diễn ra";
      case "ended": return "Đã kết thúc";
      case "cancelled": return "Đã hủy";
      default: return "Không xác định";
    }
  }

  // =============================
  // FILTER + PHÂN TRANG
  // =============================
  function getFilteredEvents() {
    let result = [...events];

    // QUAN TRỌNG: Lọc ra các sự kiện "Đã hủy" (CANCELLED) và "Bản nháp" (DRAFT) - không hiển thị cho user
    result = result.filter(ev => {
      const status = getEventStatus(ev);
      return status !== "cancelled" && status !== "draft";
    });

    if (searchTerm) {
      const t = searchTerm.toLowerCase();
      result = result.filter(ev =>
        (ev.title || "").toLowerCase().includes(t) ||
        (ev.location || "").toLowerCase().includes(t) ||
        (ev.category || "").toLowerCase().includes(t)
      );
    }

    // Filter theo tab: "Tất cả" hiển thị tất cả (trừ cancelled/draft), các tab khác filter theo status
    if (tabFilter !== "all") {
      result = result.filter(ev => getEventStatus(ev) === tabFilter);
    }

    if (onlyRecent) {
      const now = new Date();
      result = result.filter(ev => {
        const diff = (new Date(ev.startTime) - now) / (1000 * 60 * 60 * 24);
        return diff >= 0 && diff <= 7;
      });
    }

    return result;
  }

  function renderPagination(total) {
    if (!paginationEl) return;
    paginationEl.innerHTML = "";
    const totalPages = Math.ceil(total / PAGE_SIZE);
    if (totalPages <= 1) return;

    for (let i = 1; i <= totalPages; i++) {
      const btn = document.createElement("button");
      btn.className = "page-btn " + (i === currentPage ? "active" : "");
      btn.textContent = i;

      btn.addEventListener("click", () => {
        currentPage = i;
        renderEvents();
      });

      paginationEl.appendChild(btn);
    }
  }

  // =============================
  // RENDER
  // =============================
  function renderEvents() {
    const data = getFilteredEvents();
    eventGrid.innerHTML = "";

    if (!data.length) {
      eventGrid.innerHTML = "<p class='muted'>Không có sự kiện phù hợp.</p>";
      if (paginationEl) paginationEl.innerHTML = "";
      return;
    }

    renderPagination(data.length);

    const start = (currentPage - 1) * PAGE_SIZE;
    const items = data.slice(start, start + PAGE_SIZE);

    items.forEach(ev => {
      const s = getEventStatus(ev);

      // Xử lý imageUrl
      let finalImageUrl = "/assets/img/default-event.jpg"; // Default fallback
      if (ev.image) {
        if (ev.image.startsWith('http://') || ev.image.startsWith('https://')) {
          // Đã là full URL
          finalImageUrl = ev.image;
        } else if (ev.image.startsWith('/')) {
          // Relative path bắt đầu bằng /, ghép với base URL của backend
          finalImageUrl = 'http://localhost:8080' + ev.image;
        } else {
          // Relative path không có /, ghép với base
          finalImageUrl = 'http://localhost:8080/images/' + ev.image;
        }
      }

      const card = document.createElement("article");
      card.className = "event-card";

      card.innerHTML = `
        <div class="event-cover">
          <img src="${finalImageUrl}" alt="${ev.title}">
          <span class="event-badge">${statusLabel(s)}</span>
        </div>

        <div class="event-body">
          <h3>${ev.title}</h3>
          <div class="event-meta">
            <span class="chip"><i class="fa-regular fa-clock"></i> ${formatDateRange(ev.startTime, ev.endTime)}</span>
            <span class="chip"><i class="fa-solid fa-location-dot"></i> ${ev.location}</span>
          </div>
        </div>

        <div class="event-footer">
          <div class="price">Từ 200.000 ₫</div>
          <button class="btn primary btn-register">
            <i class="fa-solid fa-ticket"></i> Đăng ký
          </button>
        </div>
      `;

      card.querySelector(".btn-register").addEventListener("click", () => {
        openRegisterModal(ev);
      });

      eventGrid.appendChild(card);
    });
  }

  // =============================
  // MODAL ĐĂNG KÝ
  // =============================
  function openRegisterModal(ev) {
    selectedEventId = ev.id;  // 👈 rất quan trọng

    document.getElementById("modalEventImage").src = ev.image || "/assets/img/default-event.jpg";
    document.getElementById("modalEventTitle").textContent = ev.title;
    document.getElementById("modalEventLocation").textContent = ev.location;
    document.getElementById("modalEventDate").textContent = formatDateRange(ev.startTime, ev.endTime);

    // Nếu muốn auto fill thông tin user, có thể làm như sau:
    try {
      const user = JSON.parse(localStorage.getItem("currentUser"));
      if (user) {
        if (document.getElementById("fullName")) {
          document.getElementById("fullName").value = user.name || "";
        }
        if (document.getElementById("email")) {
          document.getElementById("email").value = user.email || "";
        }
        if (document.getElementById("phone")) {
          document.getElementById("phone").value = user.phone || "";
        }
      }
    } catch (e) {
      console.warn("Không parse được currentUser:", e);
    }

    document.getElementById("registerModal").style.display = "flex";

    updateTotalAmount();
  }

  window.closeRegisterModal = () => {
    const modal = document.getElementById("registerModal");
    if (!modal) return;
    modal.style.display = "none";

    // reset view
    const body = document.getElementById("modalBody");
    const footer = document.getElementById("modalFooter");
    const success = document.getElementById("successMessage");
    if (body) body.style.display = "block";
    if (footer) footer.style.display = "flex";
    if (success) success.style.display = "none";
  };

  // =============================
  // TICKET SELECT
  // =============================
  window.selectTicket = function (el, type) {
    document.querySelectorAll(".ticket-option").forEach(x => x.classList.remove("selected"));
    el.classList.add("selected");
    selectedTicketType = type;
    updateTotalAmount();
  };

  window.changeQty = function (btn, delta) {
    const input = btn.parentElement.querySelector(".qty-input");
    let val = parseInt(input.value || "0") + delta;
    if (val < 0) val = 0;
    if (val > 10) val = 10;
    input.value = val;
    updateTotalAmount();
  };

  function updateTotalAmount() {
  const qtyInput = document.querySelector(".ticket-option.selected .qty-input");

  if (!qtyInput) {
    totalAmountEl.textContent = "0 ₫";
    return;
  }

  const qty = parseInt(qtyInput.value || "1");

  // Giá vé cố định
  let price = 0;

  // Chuyển sang chữ thường để so sánh
  const ticketType = (selectedTicketType || "").toLowerCase();
  
  if (ticketType === "vip") {
    price = 500000; // VIP: 500.000 đ
  } else {
    price = 200000; // Regular: 200.000 đ
  }

  const total = qty * price;

  totalAmountEl.textContent = total.toLocaleString() + " ₫";
}


 // =============================
// SUBMIT FORM ĐĂNG KÝ
// =============================
// LINH
window.submitRegisterForm = async function () {
    const user = JSON.parse(localStorage.getItem("currentUser"));
    if (!user) {
        alert("Bạn chưa đăng nhập!");
        return;
    }

    const payload = {
        eventId: selectedEventId,          // Long
        userId: user.user_id,             // Long (từ Account.user_id)
        name: document.getElementById("fullName").value,
        email: document.getElementById("email").value,
        phone: document.getElementById("phone").value,
        occupation: document.getElementById("occupation").value,
        ticketType: selectedTicketType,
        note: document.getElementById("notes").value
    };

    console.log("PAYLOAD = ", payload);

    try {
        const res = await fetch("http://localhost:8080/api/events/register", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(payload)
        });

        let data;
        try {
            const responseText = await res.text();
            data = JSON.parse(responseText);
        } catch (e) {
            console.error("BACKEND ERROR:", e);
            alert("Đăng ký thất bại! Không thể parse response từ server.");
            return;
        }

        if (res.ok && data.success) {
            // Ẩn form, hiện success
            document.getElementById("modalBody").style.display = "none";
            document.getElementById("modalFooter").style.display = "none";
            document.getElementById("successMessage").style.display = "block";

            setTimeout(() => {
                closeRegisterModal();
                window.location.href = "tickets.html";
            }, 1500);
        } else {
            // Hiển thị message từ backend (ví dụ: "Bạn đã đăng ký sự kiện này rồi!")
            const errorMessage = data.message || "Đăng ký thất bại!";
            alert(errorMessage);
            console.error("BACKEND ERROR:", data);
        }

    } catch (err) {
        console.error(err);
        alert("Lỗi kết nối đến server!");
    }
};
// LINH


  // =============================
  // EVENTS
  // =============================
  if (searchInput) {
    searchInput.addEventListener("input", e => {
      searchTerm = e.target.value.trim();
      currentPage = 1;
      renderEvents();
    });
  }

  if (filterRecentCheckbox) {
    filterRecentCheckbox.addEventListener("change", e => {
      onlyRecent = e.target.checked;
      currentPage = 1;
      renderEvents();
    });
  }

  if (tabs && tabs.length) {
    tabs.forEach(tab => {
      tab.addEventListener("click", () => {
        tabs.forEach(t => t.classList.remove("active"));
        tab.classList.add("active");

        const txt = tab.textContent.trim();
        // Map text sang filter value
        if (txt.includes("Tất cả") || txt === "Tất cả") {
          tabFilter = "all";
        } else if (txt.includes("Sắp") || txt === "Sắp diễn ra") {
          tabFilter = "upcoming";
        } else if (txt.includes("Đang") || txt === "Đang diễn ra") {
          tabFilter = "ongoing";
        } else if (txt.includes("Đã kết thúc") || txt === "Đã kết thúc") {
          tabFilter = "ended";
        } else {
          tabFilter = "all";
        }

        console.log('🔍 Tab filter changed to:', tabFilter); // Debug log
        currentPage = 1;
        renderEvents();
      });
    });
  }
// ==========================================
// 🔥 TOP 20 SỰ KIỆN – LẤY TỪ BẢNG EVENT
// ==========================================

const btnSeeTop = document.getElementById("seeTop");
const top20Modal = document.getElementById("top20Modal");
const top20List = document.getElementById("top20List");
const closeTop20 = document.getElementById("closeTop20");

btnSeeTop?.addEventListener("click", () => {
    const top20 = getTop20Events();
    renderTop20(top20);
    top20Modal.classList.add("active");
});

closeTop20?.addEventListener("click", () => {
    top20Modal.classList.remove("active");
});

// ⭐ SORT TOP 20 theo created_at (hoặc event_id nếu created_at null)
function getTop20Events() {
    return [...events]
        .sort((a, b) => {
            const da = new Date(a.createdAt || a.id);
            const db = new Date(b.createdAt || b.id);
            return db - da; // newest first
        })
        .slice(0, 20);
}

// ⭐ RENDER TOP 20
function renderTop20(list) {
    top20List.innerHTML = "";

    list.forEach((ev, index) => {
        const li = document.createElement("li");
        li.innerHTML = `
            <span class="top20-rank">${String(index + 1).padStart(2, "0")}</span>
            <span>${ev.title}</span>
            <span class="top20-score">${ev.category || ""}</span>
        `;
        top20List.appendChild(li);
    });
}
// ==========================================
// 🔥 DANH SÁCH HOT (Top 5) – LẤY TỪ EVENTS
// ==========================================

const hotList = document.getElementById("hotList");

function loadHotList() {

    if (!hotList) return;

    // Lấy top 5 sự kiện mới nhất
    const top5 = [...events]
        .sort((a, b) => {
            const da = new Date(a.createdAt || a.startTime || 0);
            const db = new Date(b.createdAt || b.startTime || 0);
            return db - da;
        })
        .slice(0, 5);

    hotList.innerHTML = "";

    top5.forEach(ev => {
        const li = document.createElement("li");
        li.innerHTML = `
            <a href="javascript:void(0)">${ev.title}</a>
        `;
        hotList.appendChild(li);
    });
}
  // START
  loadEvents().then(() => {
    loadHotList();
});
});
