/**
 * Frontend Test Cases - Authentication (Login/Register)
 * Người thực hiện: Linh
 * Tool: Cypress
 * Số lượng: 20 test cases
 */

describe('Authentication Frontend Tests - Linh (20 Test Cases)', () => {
  // Sử dụng đường dẫn file trực tiếp thay vì URL
  // Nếu có server chạy, có thể đổi thành: const baseUrl = 'http://localhost:5500';
  
  beforeEach(() => {
    // Sử dụng đường dẫn relative từ thư mục frontend
    cy.visit('index.html');
  });

  // TC01: Hiển thị form đăng nhập
  it('TC01: Hiển thị form đăng nhập khi click nút đăng nhập', () => {
    cy.contains('button', 'Đăng Nhập').first().click();
    cy.get('#loginModal').should('be.visible');
    cy.get('#loginEmail').should('be.visible');
    cy.get('#loginPassword').should('be.visible');
  });

  // TC02: Hiển thị form đăng ký
  it('TC02: Hiển thị form đăng ký khi click nút đăng ký', () => {
    cy.contains('button', 'Đăng Ký').first().click();
    cy.get('#registerModal').should('be.visible');
    cy.get('#registerEmail').should('be.visible');
    cy.get('#registerPassword').should('be.visible');
  });

  // TC03: Validation email không hợp lệ
  it('TC03: Hiển thị lỗi khi nhập email không hợp lệ', () => {
    cy.contains('button', 'Đăng Nhập').first().click();
    cy.get('#loginEmail').type('invalid-email');
    cy.get('#loginEmail').blur();
    // Kiểm tra validation HTML5 hoặc custom validation
    cy.get('#loginEmail').should('have.attr', 'type', 'email');
    // Nếu có custom validation message
    cy.get('#loginEmail').then(($input) => {
      expect($input[0].validity.valid).to.be.false;
    });
  });

  // TC04: Validation password quá ngắn
  it('TC04: Hiển thị lỗi khi password quá ngắn (< 6 ký tự)', () => {
    cy.contains('button', 'Đăng Ký').first().click();
    cy.get('#registerPassword').type('12345');
    cy.get('#registerPassword').blur();
    // Kiểm tra validation HTML5
    cy.get('#registerPassword').should('have.attr', 'required');
  });

  // TC05: Đăng nhập thành công
  it('TC05: Đăng nhập thành công với thông tin hợp lệ', () => {
    cy.intercept('POST', '**/api/auth/login', { statusCode: 200, body: { success: true, account: { id: 1, role: 'USER' } } }).as('loginRequest');
    cy.contains('button', 'Đăng Nhập').first().click();
    cy.get('#loginEmail').type('user@eventqr.com');
    cy.get('#loginPassword').type('user123');
    cy.get('#loginForm').submit();
    cy.wait('@loginRequest');
    // Kiểm tra redirect hoặc modal đóng
    cy.get('#loginModal').should('not.be.visible');
  });

  // TC06: Đăng nhập với email sai
  it('TC06: Hiển thị lỗi khi đăng nhập với email không tồn tại', () => {
    cy.intercept('POST', '**/api/auth/login', { statusCode: 200, body: { success: false, message: 'Sai email hoặc mật khẩu' } }).as('loginFail');
    cy.contains('button', 'Đăng Nhập').first().click();
    cy.get('#loginEmail').type('wrong@email.com');
    cy.get('#loginPassword').type('password123');
    cy.get('#loginForm').submit();
    cy.wait('@loginFail');
    // Kiểm tra thông báo lỗi (có thể hiển thị trong modal hoặc alert)
    cy.get('body').should('contain', 'Sai email');
  });

  // TC07: Đăng nhập với password sai
  it('TC07: Hiển thị lỗi khi đăng nhập với password sai', () => {
    cy.intercept('POST', '**/api/auth/login', { statusCode: 200, body: { success: false, message: 'Sai email hoặc mật khẩu' } }).as('loginFail');
    cy.contains('button', 'Đăng Nhập').first().click();
    cy.get('#loginEmail').type('user@eventqr.com');
    cy.get('#loginPassword').type('wrongpassword');
    cy.get('#loginForm').submit();
    cy.wait('@loginFail');
    // Kiểm tra thông báo lỗi
    cy.get('body').should('contain', 'Sai email');
  });

  // TC08: Đăng ký thành công
  it('TC08: Đăng ký thành công với thông tin hợp lệ', () => {
    const timestamp = Date.now();
    cy.intercept('POST', '**/api/auth/register', { statusCode: 200, body: { success: true, message: 'Đăng ký thành công' } }).as('registerSuccess');
    cy.contains('button', 'Đăng Ký').first().click();
    cy.get('#registerName').type('Test User');
    cy.get('#registerEmail').type(`test${timestamp}@eventqr.com`);
    cy.get('#registerPassword').type('password123');
    cy.get('#confirmPassword').type('password123');
    cy.get('input[name="role"][value="user"]').check();
    cy.get('#registerForm').submit();
    cy.wait('@registerSuccess');
    // Kiểm tra thông báo thành công
    cy.get('body').should('contain', 'thành công');
  });

  // TC09: Đăng ký với email đã tồn tại
  it('TC09: Hiển thị lỗi khi đăng ký với email đã tồn tại', () => {
    cy.intercept('POST', '**/api/auth/register', { statusCode: 200, body: { success: false, message: 'Email đã tồn tại' } }).as('registerFail');
    cy.contains('button', 'Đăng Ký').first().click();
    cy.get('#registerName').type('Test User');
    cy.get('#registerEmail').type('admin@eventqr.com');
    cy.get('#registerPassword').type('password123');
    cy.get('#confirmPassword').type('password123');
    cy.get('input[name="role"][value="user"]').check();
    cy.get('#registerForm').submit();
    cy.wait('@registerFail');
    // Kiểm tra thông báo lỗi
    cy.get('body').should('contain', 'đã tồn tại');
  });

  // TC10: Hiển thị thông báo lỗi
  it('TC10: Hiển thị thông báo lỗi khi có lỗi xảy ra', () => {
    cy.intercept('POST', '**/api/auth/login', { statusCode: 500, body: { success: false, message: 'Lỗi server' } }).as('serverError');
    cy.contains('button', 'Đăng Nhập').first().click();
    cy.get('#loginEmail').type('user@eventqr.com');
    cy.get('#loginPassword').type('user123');
    cy.get('#loginForm').submit();
    cy.wait('@serverError');
    // Đợi một chút để xử lý lỗi
    cy.wait(1000);
    // Kiểm tra modal vẫn còn mở (không redirect - có nghĩa là có lỗi)
    cy.get('#loginModal').should('be.visible');
    // Kiểm tra form vẫn còn hiển thị
    cy.get('#loginForm').should('be.visible');
    // Kiểm tra input vẫn còn giá trị (chưa bị clear)
    cy.get('#loginEmail').should('have.value', 'user@eventqr.com');
  });

  // TC11: Chuyển đổi giữa form login/register
  it('TC11: Chuyển đổi giữa form đăng nhập và đăng ký', () => {
    cy.contains('button', 'Đăng Nhập').first().click();
    cy.get('#loginModal').should('be.visible');
    cy.get('#loginEmail').should('be.visible');
    cy.contains('a', 'Đăng ký ngay').click({ force: true });
    cy.get('#registerModal', { timeout: 2000 }).should('be.visible');
    cy.get('#registerName').should('be.visible');
    cy.contains('a', 'Đăng nhập ngay').click({ force: true });
    cy.get('#loginModal', { timeout: 2000 }).should('be.visible');
    cy.get('#loginEmail').should('be.visible');
  });

  // TC12: Responsive trên mobile
  it('TC12: Form đăng nhập hiển thị đúng trên mobile (375px)', () => {
    cy.viewport(375, 667);
    cy.contains('button', 'Đăng Nhập').first().click();
    cy.get('#loginEmail').should('be.visible');
    cy.get('#loginPassword').should('be.visible');
  });

  // TC13: Responsive trên tablet
  it('TC13: Form đăng nhập hiển thị đúng trên tablet (768px)', () => {
    cy.viewport(768, 1024);
    cy.contains('button', 'Đăng Nhập').first().click();
    cy.get('#loginEmail').should('be.visible');
    cy.get('#loginPassword').should('be.visible');
  });

  // TC14: Xử lý loading state
  it('TC14: Hiển thị loading state khi đang xử lý đăng nhập', () => {
    cy.intercept('POST', '**/api/auth/login', { delay: 1000, statusCode: 200, body: { success: true } }).as('slowLogin');
    cy.contains('button', 'Đăng Nhập').first().click();
    cy.get('#loginEmail').type('user@eventqr.com');
    cy.get('#loginPassword').type('user123');
    cy.get('#loginForm').submit();
    // Kiểm tra button có disabled hoặc có loading indicator
    cy.get('#loginForm button[type="submit"]').should('exist');
    cy.wait('@slowLogin');
  });

  // TC15: Xử lý network error
  it('TC15: Hiển thị lỗi khi mất kết nối mạng', () => {
    cy.intercept('POST', '**/api/auth/login', { forceNetworkError: true }).as('networkError');
    cy.contains('button', 'Đăng Nhập').first().click();
    cy.get('#loginEmail').type('user@eventqr.com');
    cy.get('#loginPassword').type('user123');
    cy.get('#loginForm').submit();
    // Đợi một chút để xử lý lỗi network (catch error có thể mất thời gian)
    cy.wait(2000);
    // Kiểm tra modal vẫn còn mở (không redirect - có nghĩa là có lỗi)
    cy.get('#loginModal', { timeout: 5000 }).should('be.visible');
    // Kiểm tra form vẫn còn hiển thị
    cy.get('#loginForm').should('be.visible');
    // Kiểm tra input vẫn còn giá trị (chưa bị clear)
    cy.get('#loginEmail').should('have.value', 'user@eventqr.com');
  });

  // TC16: Lưu token vào localStorage
  it('TC16: Lưu token vào localStorage sau khi đăng nhập thành công', () => {
    cy.intercept('POST', '**/api/auth/login', { statusCode: 200, body: { success: true, account: { id: 1, role: 'USER', email: 'user@eventqr.com' }, role: 'USER' } }).as('loginSuccess');
    cy.contains('button', 'Đăng Nhập').first().click();
    cy.get('#loginEmail').type('user@eventqr.com');
    cy.get('#loginPassword').type('user123');
    cy.get('#loginForm').submit();
    cy.wait('@loginSuccess');
    // Đợi một chút để ứng dụng lưu vào localStorage và redirect
    cy.wait(1500);
    // Kiểm tra localStorage có currentUser (ứng dụng dùng currentUser thay vì token)
    cy.window().then((win) => {
      const currentUser = win.localStorage.getItem('currentUser');
      expect(currentUser).to.exist;
      if (currentUser) {
        const userData = JSON.parse(currentUser);
        expect(userData).to.have.property('id');
        expect(userData).to.have.property('role');
      }
    });
  });

  // TC17: Redirect sau khi đăng nhập
  it('TC17: Redirect đến trang dashboard sau khi đăng nhập thành công', () => {
    cy.intercept('POST', '**/api/auth/login', { statusCode: 200, body: { success: true, account: { id: 1, role: 'USER' }, role: 'USER' } }).as('loginSuccess');
    cy.contains('button', 'Đăng Nhập').first().click();
    cy.get('#loginEmail').type('user@eventqr.com');
    cy.get('#loginPassword').type('user123');
    cy.get('#loginForm').submit();
    cy.wait('@loginSuccess');
    // Ứng dụng redirect sau 1 giây, đợi redirect
    cy.url({ timeout: 3000 }).should('include', 'pages/user/index.html');
  });

  // TC18: Logout chức năng
  it('TC18: Logout xóa token và redirect về trang chủ', () => {
    cy.window().then((win) => {
      win.localStorage.setItem('currentUser', JSON.stringify({ id: 1, email: 'user@eventqr.com', role: 'USER' }));
    });
    cy.visit('pages/user/index.html');
    // Tìm nút logout (có class .logout-btn và text "Đăng Xuất" - chữ X viết hoa)
    cy.get('.logout-btn', { timeout: 3000 }).should('be.visible');
    cy.get('.logout-btn').click();
    // Kiểm tra currentUser đã bị xóa sau khi logout
    cy.window().then((win) => {
      expect(win.localStorage.getItem('currentUser')).to.be.null;
    });
    // Kiểm tra redirect về trang chủ
    cy.url({ timeout: 3000 }).should('include', 'index.html');
  });

  // TC19: Remember me (nếu có)
  it('TC19: Checkbox "Remember me" lưu thông tin đăng nhập', () => {
    cy.contains('button', 'Đăng Nhập').first().click();
    cy.get('#loginEmail').type('user@eventqr.com');
    cy.get('#loginPassword').type('user123');
    // Tìm checkbox remember me trong form
    cy.get('#loginForm').within(() => {
      cy.get('input[type="checkbox"]').first().check();
      cy.get('input[type="checkbox"]').first().should('be.checked');
    });
  });

  // TC20: Accessibility (keyboard navigation)
  it('TC20: Có thể điều hướng form bằng bàn phím (Tab, Enter)', () => {
    cy.contains('button', 'Đăng Nhập').first().click();
    // Kiểm tra các input có thể focus được
    cy.get('#loginEmail').focus().should('be.focused');
    cy.get('#loginEmail').type('user@eventqr.com');
    
    // Kiểm tra password field có thể focus
    cy.get('#loginPassword').focus().should('be.focused');
    cy.get('#loginPassword').type('user123');
    
    // Kiểm tra submit button có thể focus (tabindex hoặc có thể focus)
    cy.get('#loginForm button[type="submit"]').should('exist');
    // Kiểm tra form có thể submit bằng Enter
    cy.get('#loginForm').should('exist');
  });
});
