const API_BASE = "http://localhost:8080/api/auth";

function getModal(modalId) {
    return document.getElementById(modalId);
}

function hasOpenModal() {
    return Array.from(document.querySelectorAll(".modal")).some((modal) => {
        return modal.style.display === "block";
    });
}

function openModal(modalId) {
    const modal = getModal(modalId);
    if (!modal) return;

    modal.style.display = "block";
    document.body.style.overflow = "hidden";
}

function closeModal(modalId) {
    const modal = getModal(modalId);
    if (!modal) return;

    modal.style.display = "none";
    if (!hasOpenModal()) {
        document.body.style.overflow = "auto";
    }
}

function closeAllModals() {
    document.querySelectorAll(".modal").forEach((modal) => {
        modal.style.display = "none";
    });
    document.body.style.overflow = "auto";
}

function openLoginModal() {
    closeAllModals();
    openModal("loginModal");
}

function openRegisterModal() {
    closeAllModals();
    openModal("registerModal");
}

function openForgotPasswordModal() {
    closeAllModals();
    openModal("forgotPasswordModal");
}

function switchToRegister() {
    closeModal("loginModal");
    openModal("registerModal");
}

function switchToLogin() {
    closeModal("registerModal");
    closeModal("forgotPasswordModal");
    openModal("loginModal");
}

function scrollToFeatures() {
    const features = document.getElementById("features");
    if (!features) return;

    features.scrollIntoView({ behavior: "smooth", block: "start" });
}

function isValidEmail(email) {
    return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);
}

function getNotificationIcon(type) {
    switch (type) {
        case "success":
            return "fa-check-circle";
        case "error":
            return "fa-exclamation-circle";
        case "warning":
            return "fa-exclamation-triangle";
        default:
            return "fa-info-circle";
    }
}

function getNotificationColor(type) {
    switch (type) {
        case "success":
            return "#10b981";
        case "error":
            return "#ef4444";
        case "warning":
            return "#f59e0b";
        default:
            return "#3b82f6";
    }
}

function ensureNotificationStyles() {
    if (document.getElementById("eventqr-notify-style")) return;

    const style = document.createElement("style");
    style.id = "eventqr-notify-style";
    style.textContent = `
        @keyframes slideInRight {
            from { opacity: 0; transform: translateX(100%); }
            to { opacity: 1; transform: translateX(0); }
        }

        @keyframes slideOutRight {
            from { opacity: 1; transform: translateX(0); }
            to { opacity: 0; transform: translateX(100%); }
        }

        .nav-menu.active {
            display: flex !important;
            position: absolute;
            top: 100%;
            left: 0;
            right: 0;
            background: #ffffff;
            flex-direction: column;
            padding: 20px;
            box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
            gap: 15px;
        }

        .hamburger.active span:nth-child(1) {
            transform: rotate(-45deg) translate(-5px, 6px);
        }

        .hamburger.active span:nth-child(2) {
            opacity: 0;
        }

        .hamburger.active span:nth-child(3) {
            transform: rotate(45deg) translate(-5px, -6px);
        }

        .fade-in {
            opacity: 0;
            transform: translateY(30px);
            transition: all 0.6s ease;
        }

        .fade-in.visible {
            opacity: 1;
            transform: translateY(0);
        }
    `;
    document.head.appendChild(style);
}

function showNotification(message, type) {
    ensureNotificationStyles();

    if (!document.body) {
        alert(`${type.toUpperCase()}: ${message}`);
        return;
    }

    document.querySelectorAll(".notification").forEach((notification) => notification.remove());

    const notification = document.createElement("div");
    notification.className = `notification notification-${type}`;
    notification.style.cssText = `
        position: fixed;
        top: 20px;
        right: 20px;
        z-index: 10000;
        min-width: 280px;
        max-width: 500px;
        background: #ffffff;
        border-radius: 8px;
        box-shadow: 0 10px 25px rgba(0, 0, 0, 0.15);
        border-left: 4px solid ${getNotificationColor(type)};
        animation: slideInRight 0.3s ease-out;
    `;

    notification.innerHTML = `
        <div style="display:flex;align-items:center;gap:10px;padding:14px 16px;color:#333;">
            <i class="fas ${getNotificationIcon(type)}" style="color:${getNotificationColor(type)};"></i>
            <span style="flex:1;">${message}</span>
            <button type="button" aria-label="Close notification"
                style="background:none;border:none;cursor:pointer;color:#666;"
                onclick="this.closest('.notification').remove()">
                <i class="fas fa-times"></i>
            </button>
        </div>
    `;

    document.body.appendChild(notification);

    setTimeout(() => {
        if (!notification.parentElement) return;
        notification.style.animation = "slideOutRight 0.3s ease-in forwards";
        setTimeout(() => notification.remove(), 300);
    }, 4000);
}

function redirectBasedOnRole(role) {
    if (role === "organizer") {
        window.location.href = "pages/admin/index.html";
        return;
    }

    window.location.href = "pages/user/index.html";
}

function checkAuthStatus() {
    const currentUser = localStorage.getItem("currentUser");
    if (!currentUser) return;

    try {
        const userData = JSON.parse(currentUser);
        console.log("User is logged in:", userData);
    } catch (error) {
        console.error("Invalid currentUser data:", error);
        localStorage.removeItem("currentUser");
    }
}

async function handleLogin() {
    const email = document.getElementById("loginEmail")?.value.trim() || "";
    const password = document.getElementById("loginPassword")?.value.trim() || "";

    if (!email || !password) {
        showNotification("Vui long dien day du thong tin", "error");
        return;
    }

    if (!isValidEmail(email)) {
        showNotification("Email khong hop le", "error");
        return;
    }

    showNotification("Dang xu ly...", "info");

    try {
        const response = await fetch(`${API_BASE}/login`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ email, password })
        });

        if (!response.ok) {
            throw new Error(`HTTP ${response.status}`);
        }

        const data = await response.json();
        if (!data.success) {
            showNotification(data.message || "Sai tai khoan hoac mat khau", "error");
            return;
        }

        const userData = { ...data.account, role: data.role };
        localStorage.setItem("currentUser", JSON.stringify(userData));

        showNotification("Dang nhap thanh cong!", "success");
        closeModal("loginModal");
        setTimeout(() => redirectBasedOnRole(data.role), 700);
    } catch (error) {
        console.error("Login error:", error);
        showNotification("Khong the ket noi den may chu", "error");
    }
}

async function handleRegister() {
    const name = document.getElementById("registerName")?.value.trim() || "";
    const email = document.getElementById("registerEmail")?.value.trim() || "";
    const password = document.getElementById("registerPassword")?.value.trim() || "";
    const confirmPassword = document.getElementById("confirmPassword")?.value.trim() || "";
    const role = document.querySelector('input[name="role"]:checked')?.value;

    if (!name || !email || !password || !confirmPassword) {
        showNotification("Vui long dien day du thong tin", "error");
        return;
    }

    if (!role) {
        showNotification("Vui long chon vai tro", "error");
        return;
    }

    if (!isValidEmail(email)) {
        showNotification("Email khong hop le", "error");
        return;
    }

    if (password.length < 6) {
        showNotification("Mat khau phai co it nhat 6 ky tu", "error");
        return;
    }

    if (password !== confirmPassword) {
        showNotification("Mat khau xac nhan khong khop", "error");
        return;
    }

    showNotification("Dang tao tai khoan...", "info");

    try {
        const response = await fetch(`${API_BASE}/register`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ name, email, password, role })
        });

        if (!response.ok) {
            throw new Error(`HTTP ${response.status}`);
        }

        const data = await response.json();
        if (!data.success) {
            showNotification(data.message || "Dang ky that bai", "error");
            return;
        }

        const userData = { ...data.account, role: data.role };
        localStorage.setItem("currentUser", JSON.stringify(userData));

        showNotification("Dang ky thanh cong!", "success");
        closeModal("registerModal");
        setTimeout(() => redirectBasedOnRole(data.role), 700);
    } catch (error) {
        console.error("Register error:", error);
        showNotification("Khong the ket noi den may chu", "error");
    }
}

async function handleForgotPassword() {
    const email = document.getElementById("forgotEmail")?.value.trim() || "";

    if (!email) {
        showNotification("Vui long nhap email", "error");
        return;
    }

    if (!isValidEmail(email)) {
        showNotification("Email khong hop le", "error");
        return;
    }

    showNotification("Dang gui yeu cau...", "info");

    try {
        const response = await fetch(`${API_BASE}/forgot`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ email })
        });

        if (!response.ok) {
            throw new Error(`HTTP ${response.status}`);
        }

        const data = await response.json();
        if (!data.success) {
            showNotification(data.message || "Khong the xu ly yeu cau", "error");
            return;
        }

        showNotification(data.message || "Da gui huong dan dat lai mat khau", "success");
        closeModal("forgotPasswordModal");
    } catch (error) {
        console.error("Forgot password error:", error);
        showNotification("Khong the ket noi den may chu", "error");
    }
}

function initForms() {
    document.getElementById("loginForm")?.addEventListener("submit", (event) => {
        event.preventDefault();
        handleLogin();
    });

    document.getElementById("registerForm")?.addEventListener("submit", (event) => {
        event.preventDefault();
        handleRegister();
    });

    document.getElementById("forgotPasswordForm")?.addEventListener("submit", (event) => {
        event.preventDefault();
        handleForgotPassword();
    });
}

function initNavigation() {
    const hamburger = document.querySelector(".hamburger");
    const navMenu = document.querySelector(".nav-menu");

    if (hamburger && navMenu) {
        hamburger.addEventListener("click", () => {
            navMenu.classList.toggle("active");
            hamburger.classList.toggle("active");
        });
    }

    document.querySelectorAll(".nav-link").forEach((link) => {
        link.addEventListener("click", (event) => {
            event.preventDefault();
            const targetId = link.getAttribute("href")?.replace("#", "");
            if (!targetId) return;

            const target = document.getElementById(targetId);
            if (!target) return;

            target.scrollIntoView({ behavior: "smooth", block: "start" });

            if (navMenu && navMenu.classList.contains("active")) {
                navMenu.classList.remove("active");
                hamburger?.classList.remove("active");
            }
        });
    });
}

function observeElements() {
    if (!("IntersectionObserver" in window)) return;

    const observer = new IntersectionObserver((entries) => {
        entries.forEach((entry) => {
            if (entry.isIntersecting) {
                entry.target.classList.add("visible");
            }
        });
    }, {
        threshold: 0.1,
        rootMargin: "0px 0px -50px 0px"
    });

    document.querySelectorAll(".feature-card, .contact-item, .about-card").forEach((element) => {
        element.classList.add("fade-in");
        observer.observe(element);
    });
}

window.addEventListener("click", (event) => {
    if (event.target.classList.contains("modal")) {
        closeAllModals();
    }
});

document.addEventListener("keydown", (event) => {
    if (event.key === "Escape") {
        closeAllModals();
    }
});

document.addEventListener("DOMContentLoaded", () => {
    initForms();
    initNavigation();
    observeElements();
    checkAuthStatus();
});
