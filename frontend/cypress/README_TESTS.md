# Hướng Dẫn Chạy Cypress Tests

## Cách 1: Chạy với Live Server (Khuyến nghị)

1. **Mở VS Code** và cài extension **Live Server** (nếu chưa có)

2. **Start Live Server:**
   - Right-click vào file `frontend/index.html`
   - Chọn "Open with Live Server"
   - Server sẽ chạy tại `http://localhost:5500` (hoặc port tương tự)

3. **Cập nhật cypress.config.js:**
   ```javascript
   baseUrl: "http://localhost:5500"
   ```

4. **Chạy tests:**
   ```bash
   cd frontend
   npx cypress open
   # hoặc
   npx cypress run
   ```

## Cách 2: Chạy với file:// (Không cần server)

Tests đã được cấu hình để chạy trực tiếp với file:// protocol.

**Chạy tests:**
```bash
cd frontend
npx cypress open
# hoặc
npx cypress run
```

**Lưu ý:** Một số tính năng có thể không hoạt động với file:// do CORS restrictions.

## Cách 3: Chạy với lite-server

1. **Start lite-server:**
   ```bash
   cd frontend
   npm start
   ```
   Server sẽ chạy tại `http://localhost:3000`

2. **Cập nhật cypress.config.js:**
   ```javascript
   baseUrl: "http://localhost:3000"
   ```

3. **Chạy tests:**
   ```bash
   npx cypress open
   ```

## Test Files

- `testBasicFeatures.cy.js` - 20 test cases cơ bản (tất cả pass)
- `testHomePage.cy.js` - Test trang chủ
- `testAuth.cy.js` - Test authentication
- `testUserDashboard.cy.js` - Test user dashboard
- `testAdminDashboard.cy.js` - Test admin dashboard
- `testIntegration.cy.js` - Test integration
- `testResponsive.cy.js` - Test responsive design

## Troubleshooting

### Lỗi: "cy.visit() failed trying to load"

**Giải pháp:**
1. Đảm bảo server đang chạy (nếu dùng baseUrl)
2. Hoặc sử dụng `cy.visitFile()` command (đã được cấu hình sẵn)
3. Kiểm tra đường dẫn file có đúng không

### Lỗi: CORS policy

**Giải pháp:**
- Sử dụng Live Server hoặc lite-server thay vì file://
- Hoặc cấu hình browser để bỏ qua CORS (không khuyến nghị)

### Tests không tìm thấy elements

**Giải pháp:**
- Đảm bảo trang đã load hoàn toàn trước khi test
- Thêm `cy.wait()` nếu cần
- Kiểm tra selector có đúng không

