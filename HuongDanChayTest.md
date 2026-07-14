# 🚀 Hướng Dẫn Chạy Test - EventQR Project

## 📋 Yêu Cầu

1. **Node.js** 16+ đã được cài đặt
2. **Backend** đang chạy tại `http://localhost:8080`
3. **Frontend**: 
   - **Cách 1 (Khuyến nghị):** Chạy Live Server hoặc web server tại `http://localhost:5500`
   - **Cách 2:** Cypress có thể mở file HTML trực tiếp (không cần server) - đã được cấu hình sẵn

## 🛠️ Cài Đặt Dependencies

### Frontend (Cypress):
```bash
cd frontend
npm install
```

### Root (Playwright):
```bash
npm install
```

## 🎯 Chạy Test

### 1. Frontend Tests (Cypress)

#### Chạy tất cả Frontend tests:
```bash
cd frontend
npx cypress run
```

#### Chạy test cụ thể:
```bash
cd frontend
npx cypress run --spec "cypress/e2e/auth-linh.cy.js"
npx cypress run --spec "cypress/e2e/events-huy.cy.js"
npx cypress run --spec "cypress/e2e/checkin-duy.cy.js"
npx cypress run --spec "cypress/e2e/feedback-hao.cy.js"
npx cypress run --spec "cypress/e2e/registration-tung.cy.js"
npx cypress run --spec "cypress/e2e/dashboard-kiet.cy.js"
```

#### Mở Cypress UI:
```bash
cd frontend
npx cypress open
```

### 2. Backend Tests (Playwright)

#### Chạy tất cả Backend API tests:
```bash
npx playwright test
```

#### Chạy test cụ thể:
```bash
npx playwright test tests/api/auth-linh.api.spec.js
npx playwright test tests/api/events-huy.api.spec.js
npx playwright test tests/api/checkin-duy.api.spec.js
npx playwright test tests/api/feedback-hao.api.spec.js
npx playwright test tests/api/registration-tung.api.spec.js
npx playwright test tests/api/dashboard-kiet.api.spec.js
```

#### Chạy với UI mode:
```bash
npx playwright test --ui
```

#### Chạy với debug mode:
```bash
npx playwright test --debug
```

## 📊 Xem Kết Quả

### Cypress:
- Kết quả hiển thị trong terminal
- Screenshots lưu tại: `frontend/cypress/screenshots/`
- Videos lưu tại: `frontend/cypress/videos/` (nếu bật)

### Playwright:
- Kết quả hiển thị trong terminal
- HTML report: `npx playwright show-report`
- Screenshots lưu tại: `test-results/`

## 🔧 Cấu Hình

### Cypress Config:
File: `frontend/cypress.config.js`
- Viewport: 1280x720
- Base URL: có thể config trong file

### Playwright Config:
File: `playwright.config.js`
- Base URL: `http://localhost:8080/api`
- Timeout: 30000ms

## ⚠️ Lưu Ý

1. **Đảm bảo backend đang chạy** trước khi chạy API tests
2. **Frontend tests:**
   - Test files đã được cấu hình để mở file HTML trực tiếp (không cần server)
   - Nếu muốn test với server thực, chạy Live Server tại `http://localhost:5500` và sửa lại `baseUrl` trong test files
3. Một số test có thể cần **test data** trong database
4. Một số test sử dụng **fixtures** - cần tạo file fixtures nếu chưa có

## 🔧 Sửa Lỗi "cy.visit() failed"

Nếu gặp lỗi `cy.visit() failed trying to load: http://localhost:5500/index.html`:

**Giải pháp 1 (Đã áp dụng):** Test files đã được sửa để sử dụng đường dẫn file trực tiếp:
- Thay vì `cy.visit('http://localhost:5500/index.html')`
- Sử dụng `cy.visit('index.html')` (đường dẫn tương đối)

**Giải pháp 2:** Nếu muốn test với server thực:
1. Chạy Live Server hoặc web server tại `http://localhost:5500`
2. Sửa lại trong test files: thêm `const baseUrl = 'http://localhost:5500'` và dùng `${baseUrl}/...`

## 🔧 Sửa Lỗi Redirect về index.html

Nếu test bị redirect về `index.html` khi visit các trang admin/user:

**Nguyên nhân:** Trang yêu cầu đăng nhập và kiểm tra `localStorage.currentUser`

**Giải pháp:** Test files đã được sửa để:
1. Set `localStorage.currentUser` TRƯỚC khi visit (dùng `onBeforeLoad`)
2. Kiểm tra URL sau khi visit để đảm bảo không bị redirect
3. Visit lại nếu bị redirect

**Nếu vẫn bị redirect:**
- Đảm bảo `currentUser` có đầy đủ các trường: `id`, `role`, `name`, `email`, `user_id`
- Role phải đúng: `'ORGANIZER'` cho trang admin, `'USER'` cho trang user

## 📝 Test Data

Một số test cases cần test data trong database:
- User accounts (admin@eventqr.com, user@eventqr.com)
- Events
- Tickets
- Feedback

Đảm bảo database đã được import từ `database/even_qr.sql`

## 🐛 Troubleshooting

### Lỗi kết nối:
- Kiểm tra backend có đang chạy không: `http://localhost:8080/api/auth/test`
- Kiểm tra frontend có đang chạy không

### Lỗi timeout:
- Tăng timeout trong config file
- Kiểm tra network connection

### Lỗi không tìm thấy element:
- Kiểm tra selector trong test có đúng không
- Kiểm tra page đã load xong chưa

