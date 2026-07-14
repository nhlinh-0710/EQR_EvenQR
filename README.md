# 🎫 EventQR - Hệ Thống Quản Lý Sự Kiện Thông Minh

<div align="center">

**International School, Duy Tan University**

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://www.oracle.com/java/)
[![JavaScript](https://img.shields.io/badge/JavaScript-ES6+-yellow.svg)](https://www.javascript.com/)
[![MySQL](https://img.shields.io/badge/MySQL-8.0-blue.svg)](https://www.mysql.com/)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)

*Hệ thống quản lý sự kiện hiện đại với QR check-in tự động, thống kê real-time và AI chatbot hỗ trợ*

</div>

---

## 📋 Mục Lục

- [Giới Thiệu](#-giới-thiệu)
- [Tính Năng Nổi Bật](#-tính-năng-nổi-bật)
- [Công Nghệ Sử Dụng](#-công-nghệ-sử-dụng)
- [Cấu Trúc Dự Án](#-cấu-trúc-dự-án)
- [Hướng Dẫn Cài Đặt](#-hướng-dẫn-cài-đặt)
- [Hướng Dẫn Sử Dụng](#-hướng-dẫn-sử-dụng)
- [API Documentation](#-api-documentation)
- [Screenshots](#-screenshots)
- [Roadmap](#-roadmap)
- [Contributing](#-contributing)
- [License](#-license)

---

## 🎯 Giới Thiệu

**EventQR** là một hệ thống quản lý sự kiện full-stack được xây dựng với Spring Boot và JavaScript thuần, tích hợp các công nghệ hiện đại để mang lại trải nghiệm tốt nhất cho cả người tổ chức và người tham gia sự kiện.

### ✨ Điểm Nổi Bật

- 🎫 **QR Check-in tự động** - Quét mã QR để check-in nhanh chóng
- 📊 **Dashboard thống kê real-time** - Theo dõi sự kiện trực tiếp
- 🤖 **AI Chatbot** - Hỗ trợ người dùng 24/7
- 🔔 **WebSocket Notifications** - Thông báo real-time
- 📱 **Responsive Design** - Hoạt động mượt mà trên mọi thiết bị
- 🎨 **Modern UI/UX** - Giao diện đẹp mắt, thân thiện

---

## 🚀 Tính Năng Nổi Bật

### 👥 Quản Lý Người Dùng
- ✅ Đăng ký/Đăng nhập với JWT Authentication
- ✅ Phân quyền người dùng (Admin/User)
- ✅ Quản lý hồ sơ cá nhân
- ✅ Xác thực email và bảo mật cao

### 📅 Quản Lý Sự Kiện
- ✅ Tạo và chỉnh sửa sự kiện với thông tin chi tiết
- ✅ Upload hình ảnh cho sự kiện
- ✅ Phân loại sự kiện theo danh mục
- ✅ Quản lý số lượng người tham gia
- ✅ Theo dõi trạng thái sự kiện (Sắp diễn ra/Đang diễn ra/Đã kết thúc)

### 🎫 Hệ Thống Vé & Check-in
- ✅ Tự động sinh mã QR cho từng vé
- ✅ Quét QR code để check-in (hỗ trợ camera)
- ✅ Nhập mã QR thủ công
- ✅ Lịch sử check-in chi tiết
- ✅ In và tải xuống vé dạng PDF

### 📊 Dashboard & Thống Kê
- ✅ Thống kê tổng quan (Sự kiện, Người tham gia, Check-in)
- ✅ Biểu đồ trực quan với Chart.js
- ✅ Báo cáo chi tiết theo thời gian
- ✅ Export dữ liệu Excel/PDF

### 💬 Feedback & Đánh Giá
- ✅ Đánh giá sự kiện với rating 1-5 sao
- ✅ Gửi feedback chi tiết
- ✅ Quản lý và trả lời feedback
- ✅ Thống kê độ hài lòng

### 🔔 Hệ Thống Thông Báo
- ✅ Thông báo real-time qua WebSocket
- ✅ Thông báo về sự kiện sắp diễn ra
- ✅ Thông báo check-in thành công
- ✅ Quản lý trạng thái đã đọc/chưa đọc

### 🤖 AI Chatbot
- ✅ Hỗ trợ tự động 24/7
- ✅ Tích hợp Google Gemini AI
- ✅ Trả lời câu hỏi về sự kiện
- ✅ Gợi ý sự kiện phù hợp

---

## 🛠️ Công Nghệ Sử Dụng

### Backend Stack
```
├── Spring Boot 3.x          - Framework Java hiện đại
├── Spring Data JPA          - ORM và database abstraction
├── Spring Security          - Authentication & Authorization
├── MySQL 8.0                - Relational Database
├── WebSocket (STOMP)        - Real-time communication
├── JWT (JSON Web Token)     - Stateless authentication
├── Maven                    - Dependency management
└── Lombok                   - Reduce boilerplate code
```

### Frontend Stack
```
├── HTML5/CSS3               - Modern web standards
├── JavaScript (ES6+)        - Vanilla JavaScript
├── Fetch API                - HTTP client
├── WebSocket API            - Real-time updates
├── QRCode.js                - QR code generation
├── Html5-QRCode             - QR code scanner
├── Chart.js                 - Data visualization
└── Font Awesome & Boxicons  - Icons
```

### Additional Features
- 🎨 **CSS Grid & Flexbox** - Responsive layouts
- 🔄 **WebSocket** - Real-time notifications
- 📱 **Progressive Web App** - App-like experience
- 🌐 **CORS Enabled** - Cross-origin support
- 🔒 **Secure Headers** - XSS & CSRF protection
- 📊 **RESTful API** - Standard API design

---

## 📁 Cấu Trúc Dự Án

```
EventQR/
├── backend/                          # Backend Spring Boot
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/eventqr/
│   │   │   │   ├── config/          # Cấu hình CORS, WebSocket
│   │   │   │   ├── controller/      # REST API Controllers (10 files)
│   │   │   │   │   ├── AuthController.java
│   │   │   │   │   ├── EventController.java
│   │   │   │   │   ├── CheckInController.java
│   │   │   │   │   ├── FeedbackController.java
│   │   │   │   │   ├── NotificationController.java
│   │   │   │   │   └── ...
│   │   │   │   ├── dto/              # Data Transfer Objects (15 files)
│   │   │   │   ├── model/            # Entity Models (8 files)
│   │   │   │   │   ├── Account.java
│   │   │   │   │   ├── Event.java
│   │   │   │   │   ├── EventTicket.java
│   │   │   │   │   ├── CheckInHistory.java
│   │   │   │   │   └── ...
│   │   │   │   ├── repository/       # JPA Repositories (8 files)
│   │   │   │   ├── service/          # Business Logic Services (10 files)
│   │   │   │   ├── util/             # Utility Classes (2 files)
│   │   │   │   └── EventQrApplication.java
│   │   │   └── resources/
│   │   │       ├── application.properties
│   │   │       └── static/images/
│   │   └── test/
│   ├── pom.xml                       # Maven configuration
│   └── target/                       # Compiled classes & JAR
│
├── frontend/                         # Frontend Web Application
│   ├── index.html                    # Landing page
│   │
│   ├── css/
│   │   ├── admin/                    # Admin styles
│   │   │   ├── dashboard.css
│   │   │   ├── event.css
│   │   │   ├── feedback.css
│   │   │   ├── qr-checkin.css
│   │   │   └── stactics.css
│   │   ├── user/                     # User styles
│   │   │   ├── feedback.css
│   │   │   ├── my-events.css
│   │   │   ├── my-tickets.css
│   │   │   ├── qr-checkin.css
│   │   │   └── suggest-events.css
│   │   ├── chatbot.css               # AI chatbot styles
│   │   ├── placehoders.css           # Placeholder images
│   │   └── style.css                 # Global styles
│   │
│   ├── js/
│   │   ├── admin/                    # Admin scripts
│   │   │   ├── create-event.js
│   │   │   ├── dashboard.js
│   │   │   ├── events.js
│   │   │   ├── feedback.js
│   │   │   ├── notifications.js
│   │   │   └── statistics.js
│   │   ├── core/                     # Core modules
│   │   │   ├── auth.js               # Authentication
│   │   │   ├── chatbot.js            # AI Chatbot
│   │   │   ├── chatbot-simple.js     # Simple chatbot
│   │   │   ├── dashboard.js          # Dashboard logic
│   │   │   ├── main.js               # Main app
│   │   │   ├── notifications.js      # Notification system
│   │   │   └── websocket.js          # WebSocket client
│   │   └── user/                     # User scripts
│   │       ├── feedback.js
│   │       ├── my-tickets.js
│   │       ├── qr-checkin.js
│   │       ├── simple-qr.js
│   │       └── suggest-events.js
│   │
│   ├── pages/
│   │   ├── admin/                    # Admin pages
│   │   │   ├── index.html            # Admin dashboard
│   │   │   ├── create_event.html     # Create event
│   │   │   ├── events.html           # Event management
│   │   │   ├── feedback.html         # Feedback management
│   │   │   ├── profile.html          # Admin profile
│   │   │   ├── qr.html               # QR scanning
│   │   │   └── static.html           # Statistics
│   │   └── user/                     # User pages
│   │       ├── index.html            # User dashboard
│   │       ├── feedback.html         # Submit feedback
│   │       ├── profile.html          # User profile
│   │       └── tickets.html          # My tickets
│   │
│   ├── setup-ai.html                 # AI chatbot setup
│   ├── test-ai.html                  # Test AI features
│   ├── test-chatbot.html             # Test chatbot
│   ├── test-dashboard.html           # Test dashboard
│   ├── package.json                  # NPM dependencies
│   └── node_modules/                 # Node packages
│
├── database/
│   └── even_qr.sql                   # Database schema & sample data
│
├── START_BACKEND.bat                 # Start backend server
├── KILL_PORT_8080.bat                # Kill port 8080
├── CLEAN_ALL.bat                     # Clean project
└── README.md                         # Project documentation
```

---

## 🔧 Hướng Dẫn Cài Đặt

### 📋 Yêu Cầu Hệ Thống

- **Java JDK** 17 hoặc cao hơn
- **Maven** 3.8+
- **MySQL** 8.0+
- **Node.js** 16+ (cho frontend dependencies)
- **Git** (optional)

### 1️⃣ Clone Repository

```bash
git clone https://github.com/yourusername/EventQR.git
cd EventQR
```

### 2️⃣ Cấu Hình Database

**Tạo database MySQL:**
```sql
CREATE DATABASE even_qr;
USE even_qr;
```

**Import schema:**
```bash
mysql -u root -p even_qr < database/even_qr.sql
```

### 3️⃣ Cấu Hình Backend

**Chỉnh sửa `backend/src/main/resources/application.properties`:**
```properties
# Database Configuration
spring.datasource.url=jdbc:mysql://localhost:3306/even_qr
spring.datasource.username=your_username
spring.datasource.password=your_password

# Server Configuration
server.port=8080

# JWT Configuration
jwt.secret=your_secret_key_here
jwt.expiration=86400000

# File Upload
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=10MB
```

### 4️⃣ Build & Run Backend

**Sử dụng Maven:**
```bash
cd backend
mvn clean install
mvn spring-boot:run
```

**Hoặc sử dụng script (Windows):**
```bash
START_BACKEND.bat
```

Backend sẽ chạy tại: `http://localhost:8080`

### 5️⃣ Setup Frontend

**Cài đặt dependencies (nếu cần):**
```bash
cd frontend
npm install
```

**Chạy với Live Server:**
- Cài đặt extension "Live Server" trong VS Code
- Right-click vào `frontend/index.html`
- Chọn "Open with Live Server"

Frontend sẽ chạy tại: `http://localhost:5500` (hoặc port tương tự)

### 6️⃣ Cấu Hình AI Chatbot (Optional)

1. Truy cập: `frontend/setup-ai.html`
2. Nhập **Google Gemini API Key**
3. Test chatbot tại: `frontend/test-chatbot.html`

---

## 📖 Hướng Dẫn Sử Dụng

### 🏠 Trang Chủ

1. Truy cập `http://localhost:5500/frontend/index.html`
2. Xem thông tin giới thiệu về hệ thống
3. Click **"Đăng Nhập"** hoặc **"Đăng Ký"**

### 👤 Đăng Ký Tài Khoản

```
Email: your@email.com
Password: ********
Họ và Tên: Nguyễn Văn A
Số điện thoại: 0123456789
```

### 🔐 Đăng Nhập

**Admin Account (mặc định):**
```
Email: admin@eventqr.com
Password: admin123
```

**User Account:**
```
Email: user@eventqr.com
Password: user123
```

### 📅 Tạo Sự Kiện Mới (Admin)

1. Đăng nhập với tài khoản Admin
2. Vào **Dashboard** → **Tạo Sự Kiện**
3. Điền thông tin:
   - Tên sự kiện
   - Mô tả
   - Ngày & Giờ
   - Địa điểm
   - Số lượng tối đa
   - Danh mục
4. Upload hình ảnh (optional)
5. Click **"Tạo Sự Kiện"**

### 🎫 Đăng Ký Tham Gia (User)

1. Đăng nhập với tài khoản User
2. Xem danh sách sự kiện
3. Click **"Đăng Ký"** trên sự kiện mong muốn
4. Nhận mã QR vé qua email

### 📱 QR Check-in

**Cách 1: Quét QR Code**
1. Mở camera trên thiết bị
2. Quét mã QR trên vé
3. Hệ thống tự động check-in

**Cách 2: Nhập Mã Thủ Công**
1. Vào trang QR Check-in
2. Nhập mã vé
3. Click **"Check-in"**

### 📊 Xem Thống Kê (Admin)

1. Vào **Dashboard** → **Thống Kê**
2. Xem các biểu đồ:
   - Số lượng sự kiện theo tháng
   - Tỷ lệ check-in
   - Top sự kiện hot
3. Export báo cáo Excel/PDF

---



```javascript
// Connect to WebSocket
const socket = new SockJS('http://localhost:8080/ws');
const stompClient = Stomp.over(socket);

// Subscribe to notifications
stompClient.subscribe('/user/queue/notifications', (message) => {
  console.log('Received:', message.body);
});
```




---

## 🔒 Security Features

### Backend Security
- ✅ **JWT Authentication** - Token-based authentication
- ✅ **Password Encryption** - BCrypt hashing
- ✅ **CORS Protection** - Cross-origin configuration
- ✅ **SQL Injection Prevention** - Parameterized queries
- ✅ **XSS Protection** - Input sanitization
- ✅ **Rate Limiting** - API throttling

### Frontend Security
- ✅ **Input Validation** - Client-side validation
- ✅ **XSS Prevention** - Escape HTML
- ✅ **CSRF Protection** - Token validation
- ✅ **Secure Storage** - LocalStorage encryption
- ✅ **HTTPS Only** - Secure connections

---

## 🚦 Testing

### Backend Testing
```bash
cd backend
mvn test
```

### API Testing với Postman
1. Import collection: `postman/EventQR.postman_collection.json`
2. Set environment variables
3. Run tests

### Frontend Testing
1. Mở `frontend/test-dashboard.html`
2. Mở `frontend/test-chatbot.html`
3. Kiểm tra các tính năng

---

## 🎨 Responsive Design

### 💻 Desktop (≥ 1024px)
- Full sidebar navigation
- Grid layout cho cards
- Hover effects đầy đủ
- Charts & visualizations



---

## 🗺️ Development Roadmap

Dự án được phát triển theo phương pháp **Agile Scrum** với 4 Sprint chính, mỗi Sprint khoảng 3-4 tuần.

---

### 🟢 Sprint 1: Core System & User Management ✅
**Timeline:** 03/09 - 30/09  
**Branch:** `v1.0-core-system`  
**Status:** ✅ Completed

#### 🎯 Mục tiêu
Xây dựng nền tảng cơ bản của hệ thống với chức năng đăng nhập, đăng ký và quản lý sự kiện.

#### 📋 Tasks & Assignments

**Frontend (Hào)**
- [x] Thiết kế giao diện đăng nhập/đăng ký
- [x] Trang chủ cho người dùng (User)
- [x] Trang chủ cho người tổ chức (Organizer)
- [x] Responsive design cho tất cả màn hình

**Database & Backend (Linh)**
- [x] Thiết kế Database Schema (MySQL)
- [x] Tạo các Entity Models (Account, Event, Role)
- [x] API đăng ký tài khoản (User & Organizer)
- [x] API đăng nhập với JWT authentication
- [x] Phân quyền User/Organizer

**Backend - Event Management (Duy)**
- [x] API Create Event (POST /api/events)
- [x] API Update Event (PUT /api/events/{id})
- [x] API Delete Event (DELETE /api/events/{id})
- [x] API Get Events (GET /api/events)
- [x] Validation & Error handling

**Frontend - Event Suggestion (Huy)**
- [x] Chức năng gợi ý sự kiện mới
- [x] Hiển thị sự kiện hot/trending
- [x] Filter và search events
- [x] Tích hợp với backend API

#### 🎉 Deliverables
✅ Hệ thống cơ bản hoạt động với đăng nhập, đăng ký, CRUD sự kiện  
✅ Database schema hoàn chỉnh  
✅ JWT authentication working  
✅ User/Organizer role-based access

---

### 🟢 Sprint 2: Event Registration & QR Check-in ✅
**Timeline:** 01/10 - 21/10  
**Branch:** `v2.0-qr-checkin`  
**Status:** ✅ Completed

#### 🎯 Mục tiêu
Triển khai hệ thống đăng ký sự kiện, tạo vé điện tử và QR check-in.

#### 📋 Tasks & Assignments

**Frontend - Ticket UI (Hào)**
- [x] Giao diện "My Events" (danh sách vé của user)
- [x] Giao diện hiển thị vé điện tử với QR code
- [x] Giao diện QR check-in scanner
- [x] Modal check-in success/error

**Backend - Registration (Linh)**
- [x] API đăng ký sự kiện cho User
- [x] Tạo EventTicket khi đăng ký thành công
- [x] API lấy danh sách vé của User
- [x] Validation số lượng người tham gia

**Backend - Notifications (Linh)**
- [x] WebSocket configuration
- [x] Gửi thông báo đến Organizer khi có đăng ký mới
- [x] Notification entity & repository
- [x] Real-time notification system

**Backend - QR Generation (Huy)**
- [x] Tạo mã QR unique cho mỗi vé
- [x] Format: EVT-{eventId}-USR-{userId}-{timestamp}
- [x] API generate QR code
- [x] Lưu QR code vào database

**Backend - QR Scanner (Duy)**
- [x] API đọc và validate QR code
- [x] Xử lý check-in logic
- [x] Kiểm tra duplicate check-in
- [x] Response check-in status

**Backend - Check-in History (Linh)**
- [x] CheckInHistory entity
- [x] Lưu lịch sử check-in với timestamp
- [x] API lấy lịch sử check-in theo Event
- [x] Thống kê số lượng đã check-in

#### 🎉 Deliverables
✅ User có thể đăng ký sự kiện và nhận vé QR  
✅ QR check-in hoạt động (scanner + validate)  
✅ Lịch sử check-in được lưu trữ  
✅ Notification real-time cho Organizer

---

### 🟢 Sprint 3: Dashboard & Feedback System ✅
**Timeline:** 22/10 - 11/11  
**Branch:** `v3.0-reports-notifications`  
**Status:** ✅ Completed

#### 🎯 Mục tiêu
Xây dựng dashboard thống kê và hệ thống feedback/đánh giá sự kiện.

#### 📋 Tasks & Assignments

**Frontend - Feedback UI (Hào)**
- [x] Giao diện đánh giá sự kiện (User)
- [x] Rating system (1-5 sao)
- [x] Form nhập comment feedback
- [x] Giao diện xem feedback (Organizer)
- [x] Hiển thị danh sách feedback của sự kiện

**Backend - Feedback System (Linh)**
- [x] Feedback entity (rating, comment)
- [x] API submit feedback (POST /api/feedback/{eventId})
- [x] API get feedback by event
- [x] Validation: chỉ cho phép feedback sau khi check-in
- [x] Tính điểm rating trung bình

**Backend - Export Reports (Duy)**
- [x] Export feedback ra Excel
- [x] Export feedback ra PDF
- [x] API export check-in list
- [x] Format và styling cho reports

**Backend - Dashboard Statistics (Linh)**
- [x] API dashboard cho Organizer
- [x] Thống kê số lượng đăng ký theo sự kiện
- [x] Thống kê tỷ lệ check-in
- [x] Thống kê rating/feedback
- [x] Time-series data cho biểu đồ

#### 🎉 Deliverables
✅ Dashboard thống kê hoàn chỉnh cho Organizer  
✅ User có thể đánh giá và feedback  
✅ Export reports Excel/PDF  
✅ Real-time statistics updates

---

### 🟢 Sprint 4: AI Chatbot Integration ✅
**Timeline:** 12/11 - 02/12  
**Branch:** `v4.0-ai-chatbot`  
**Status:** ✅ Completed

#### 🎯 Mục tiêu
Tích hợp AI Chatbot để hỗ trợ người dùng tự động 24/7.

#### 📋 Tasks & Assignments

**Frontend - Chatbot UI (Hào)**
- [x] Thiết kế khung chat nổi (floating chat widget)
- [x] Chat interface với typing indicator
- [x] Message history display
- [x] Responsive chatbot UI
- [x] Tối ưu UI/UX đa trình duyệt (Chrome, Firefox, Safari, Edge)
- [x] Animation và transitions mượt mà

**Backend - AI Integration (Linh & Duy)**
- [x] Tích hợp Google Gemini AI API
- [x] API endpoint cho chatbot
- [x] Context management cho conversations
- [x] Training data về sự kiện
- [x] Natural language processing
- [x] Response formatting và optimization

**Testing & Optimization (Team)**
- [x] Test chatbot trên nhiều trình duyệt
- [x] Performance optimization
- [x] Error handling & fallback responses
- [x] User acceptance testing

#### 🎉 Deliverables
✅ **MVP hoàn chỉnh** - Tất cả tính năng core hoạt động  
✅ **AI Chatbot** - Hỗ trợ tự động cho User  
✅ **Cross-browser compatible** - Hoạt động mượt mà mọi trình duyệt  
✅ **Production ready** - Sẵn sàng deploy

---

### 📊 Sprint Summary

| Sprint | Duration | Features | Status |
|--------|----------|----------|--------|
| Sprint 1 | 03/09 - 30/09 | Auth, CRUD Events | ✅ |
| Sprint 2 | 01/10 - 21/10 | QR Check-in, Tickets | ✅ |
| Sprint 3 | 22/10 - 11/11 | Dashboard, Feedback | ✅ |
| Sprint 4 | 12/11 - 02/12 | AI Chatbot | ✅ |

**Total Development Time:** 3 months (Sep - Dec 2024)  
**Team Size:** 4 developers  
**Methodology:** Agile Scrum  
**Current Version:** v4.0

---

## 🤝 Contributing

Chúng tôi rất hoan nghênh mọi đóng góp! Để contribute:

### 1. Fork dự án
```bash
git clone https://github.com/yourusername/EventQR.git
cd EventQR
```

### 2. Tạo branch mới
```bash
git checkout -b feature/AmazingFeature
```

### 3. Commit changes
```bash
git add .
git commit -m "Add some AmazingFeature"
```

### 4. Push to branch
```bash
git push origin feature/AmazingFeature
```

### 5. Tạo Pull Request
- Mở pull request trên GitHub
- Mô tả rõ ràng về thay đổi
- Đợi review từ maintainers

### Coding Standards
- ✅ Follow Java conventions (backend)
- ✅ Use ESLint rules (frontend)
- ✅ Write meaningful commit messages
- ✅ Add comments cho code phức tạp
- ✅ Write unit tests
- ✅ Update documentation

---

## 📝 License

Dự án này được phân phối dưới **MIT License**. Xem file [LICENSE](LICENSE) để biết thêm chi tiết.

```
MIT License

Copyright (c) 2024 EventQR Team

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction...
```

---

## 👥 Team

### Project Members 
- **Ngô Châu Nhật Linh** - Backend Developer
- **Nguyễn Minh Duy** - Backend Developer
- **Phạm Văn Huy** - Backend Developer
- **Hồ Viết Hào** - UI/UX Designer
- **Nguyễn Công Minh** - Document
### Project Leader
- **Ngô Châu Nhật Linh**

---

## 🙏 Credits & Acknowledgments

### Technologies & Libraries
- **Spring Boot** - Backend framework
- **MySQL** - Database system
- **Font Awesome** - Icon library
- **Boxicons** - Additional icons
- **Google Fonts (Poppins)** - Typography
- **Chart.js** - Data visualization
- **QRCode.js** - QR code generation
- **Html5-QRCode** - QR code scanning
- **Google Gemini AI** - Chatbot intelligence

### Design Inspiration
- **Material Design** - Color palette
- **Dribbble** - UI/UX inspiration
- **Behance** - Design concepts
- **Awwwards** - Modern web design

### Resources
- **Stack Overflow** - Problem solving
- **GitHub** - Code hosting & version control
- **MDN Web Docs** - Web development reference
- **Spring.io** - Spring documentation

---

## 📞 Contact & Support

### Get in Touch
- 📧 **Email**: support@eventqr.com
- 🌐 **Website**: https://eventqr.com
- 💬 **Discord**: [Join our community](https://discord.gg/eventqr)
- 📱 **Facebook**: [@EventQR](https://facebook.com/eventqr)

### Report Issues
- 🐛 **Bug Reports**: [GitHub Issues](https://github.com/yourusername/EventQR/issues)
- 💡 **Feature Requests**: [GitHub Discussions](https://github.com/yourusername/EventQR/discussions)

### Documentation
- 📚 **Wiki**: [Project Wiki](https://github.com/yourusername/EventQR/wiki)
- 📖 **API Docs**: [API Documentation](https://api.eventqr.com/docs)

---

## 📊 Project Statistics

![GitHub Stars](https://img.shields.io/github/stars/yourusername/EventQR?style=social)
![GitHub Forks](https://img.shields.io/github/forks/yourusername/EventQR?style=social)
![GitHub Issues](https://img.shields.io/github/issues/yourusername/EventQR)
![GitHub Pull Requests](https://img.shields.io/github/issues-pr/yourusername/EventQR)
![Code Size](https://img.shields.io/github/languages/code-size/yourusername/EventQR)
![Contributors](https://img.shields.io/github/contributors/yourusername/EventQR)

---

<div align="center">

### 🎉 **EventQR - Tạo ra những sự kiện đáng nhớ!**

Made with ❤️ by EventQR Team

**International School, Duy Tan University**

[⬆ Back to Top](#-eventqr---hệ-thống-quản-lý-sự-kiện-thông-minh)

</div>
