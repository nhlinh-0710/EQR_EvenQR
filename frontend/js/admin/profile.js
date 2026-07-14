// ============================
// 1) LOCAL STORAGE PROFILE
// ============================
const PROFILE_KEY = "eventqr_profile";

// Update user name in header
document.addEventListener('DOMContentLoaded', function() {
    const currentUser = JSON.parse(localStorage.getItem('currentUser'));
    if (currentUser) {
        const userNameElement = document.getElementById('userName');
        if (userNameElement) {
            userNameElement.textContent = currentUser.name || 'Người dùng';
        }
    }
});

function loadProfile() {
    const data = localStorage.getItem(PROFILE_KEY);
    return data ? JSON.parse(data) : null;
}

function saveProfileData(profile) {
    localStorage.setItem(PROFILE_KEY, JSON.stringify(profile));
}


// ============================
// 2) RENDER PROFILE TO UI
// ============================
function renderProfile() {
    const p = loadProfile();
    if (!p) return;

    // Avatar
    if (p.avatar_url) {
        document.querySelector(".ap-avatar").src = p.avatar_url;
    }

    // Name / Title
    document.querySelector(".ap-name").innerText = p.title || "NHÓM CỦA CHÚNG TÔI";
    document.querySelector(".ap-title").innerText = p.subtitle || "";

    // Description
    document.querySelector(".ap-about").innerText = p.description || "";

    // Social links
    if (p.facebook)
        document.querySelector(".ap-socials a:nth-child(1)").href = p.facebook;

    if (p.github)
        document.querySelector(".ap-socials a:nth-child(2)").href = p.github;

    if (p.contact_email)
        document.querySelector(".ap-socials a:nth-child(3)").href = "mailto:" + p.contact_email;
}


// ============================
// 3) OPEN / CLOSE MODAL
// ============================
const modal = document.getElementById("profileEditModal");

document.querySelector(".edit-profile-btn").addEventListener("click", () => {
    fillForm();
    modal.classList.add("show");
});

function closeEditProfile() {
    modal.classList.remove("show");
}


// ============================
// 4) FILL FORM WITH CURRENT DATA
// ============================
function fillForm() {
    const p = loadProfile();
    if (!p) return;

    document.getElementById("editTitle").value = p.title || "";
    document.getElementById("editSubtitle").value = p.subtitle || "";
    document.getElementById("editDescription").value = p.description || "";
    document.getElementById("editFacebook").value = p.facebook || "";
    document.getElementById("editGithub").value = p.github || "";
    document.getElementById("editEmail").value = p.contact_email || "";
}


// ============================
// 5) SAVE PROFILE TO LOCAL STORAGE
// ============================
function saveProfile() {
    const avatarFile = document.getElementById("editAvatar").files[0];

    const newProfile = {
        title: document.getElementById("editTitle").value,
        subtitle: document.getElementById("editSubtitle").value,
        description: document.getElementById("editDescription").value,
        facebook: document.getElementById("editFacebook").value,
        github: document.getElementById("editGithub").value,
        contact_email: document.getElementById("editEmail").value,
        avatar_url: null
    };

    const old = loadProfile();
    newProfile.avatar_url = old?.avatar_url || null;

    if (avatarFile) {
        const reader = new FileReader();
        reader.onload = function (e) {
            newProfile.avatar_url = e.target.result;
            saveProfileData(newProfile);
            renderProfile();
            renderRecentProjects();
        };
        reader.readAsDataURL(avatarFile);
    } else {
        saveProfileData(newProfile);
        renderProfile();
        renderRecentProjects();
    }

    closeEditProfile();
}


// ===============================
// 6) RENDER PROJECTS FROM EVENTS
// ===============================
async function renderRecentProjects() {
    const grid = document.querySelector(".ap-grid");
    if (!grid) return;

    const user = JSON.parse(localStorage.getItem("currentUser"));
    if (!user) {
        grid.innerHTML = "<p>Vui lòng đăng nhập để xem sự kiện.</p>";
        return;
    }

    const organizerId = user.user_id;

    try {
        const res = await fetch(`http://localhost:8080/api/events/my-events?organizerId=${organizerId}`);
        const events = await res.json();

        if (!Array.isArray(events) || events.length === 0) {
            grid.innerHTML = "<p>Chưa có sự kiện nào.</p>";
            return;
        }

        const latest = events.slice(-3).reverse();
        grid.innerHTML = "";

        latest.forEach(ev => {
            const img = ev.imageUrl
                ? (ev.imageUrl.startsWith("http") ? ev.imageUrl : "http://localhost:8080" + (ev.imageUrl.startsWith("/") ? ev.imageUrl : "/" + ev.imageUrl))
                : "https://via.placeholder.com/400";

            let startText = "N/A";
            if (ev.startTime) {
                const d = new Date(ev.startTime);
                startText = d.toLocaleString("vi-VN", {
                    day: "2-digit",
                    month: "2-digit",
                    year: "numeric",
                    hour: "2-digit",
                    minute: "2-digit"
                });
            }

            // STATUS MAPPING
            let status = ev.status ?? "DRAFT";
            let statusLabel = "";
            let statusColor = "";

            switch (status.toUpperCase()) {
                case "UPCOMING":
                    statusLabel = "Sắp diễn ra";
                    statusColor = "#3B82F6";
                    break;
                case "ONGOING":
                    statusLabel = "Đang diễn ra";
                    statusColor = "#10B981";
                    break;
                case "COMPLETED":
                    statusLabel = "Đã kết thúc";
                    statusColor = "#6B7280";
                    break;
                case "CANCELLED":
                    statusLabel = "Đã hủy";
                    statusColor = "#EF4444";
                    break;
                default:
                    statusLabel = "Bản nháp";
                    statusColor = "#9CA3AF";
            }

            const card = document.createElement("article");
            card.classList.add("ap-card");

            card.innerHTML = `
                <div class="ap-card-cover">
                    <img loading="lazy" src="${img}">
                </div>

                <div class="ap-meta">
                    <h3>${ev.title || "Chưa đặt tên"}</h3>

                    <p class="meta-line">
                        <i class="fas fa-calendar-alt"></i> 
                        ${startText}
                    </p>

                    <p class="meta-line">
                        <i class="fas fa-map-marker-alt"></i>
                        ${ev.location || "Không rõ"}
                    </p>

                    <p>${ev.description || "Không có mô tả."}</p>

                    <div class="ap-tags">
                        <span style="
                            background:${statusColor}20;
                            color:${statusColor};
                            padding:6px 12px;
                            border-radius:12px;
                            font-size:13px;
                            font-weight:600;
                        ">
                            ${statusLabel}
                        </span>
                    </div>
                </div>
            `;

            grid.appendChild(card);
        });

    } catch (e) {
        console.error("Error:", e);
        grid.innerHTML = "<p style='color:red;'>Không tải được dự án!</p>";
    }
}


// ============================
// 7) INITIAL LOAD
// ============================
document.addEventListener("DOMContentLoaded", () => {
    renderProfile();
    renderRecentProjects();
});
