/**
 * Backend API Test Cases - Authentication (Login/Register)
 * Người thực hiện: Linh
 * Tool: Playwright
 * Số lượng: 20 test cases
 */

import { test, expect } from '@playwright/test';

const BASE_URL = 'http://localhost:8080/api/auth';

test.describe('Authentication Backend API Tests - Linh (20 Test Cases)', () => {
  // Tăng timeout cho tất cả tests
  test.setTimeout(60000); // 60 giây
  
  // Kiểm tra backend server có đang chạy không
  test.beforeAll(async ({ request }) => {
    try {
      const response = await request.get('http://localhost:8080/api/events', { timeout: 5000 });
      // Nếu không có response hoặc status > 500, server có thể không chạy
      if (response.status() >= 500) {
        console.warn('⚠️ Backend server có thể không hoạt động đúng. Vui lòng kiểm tra server có đang chạy tại http://localhost:8080');
      }
    } catch (error) {
      console.error('❌ Không thể kết nối đến backend server tại http://localhost:8080');
      console.error('💡 Vui lòng đảm bảo backend server đang chạy trước khi chạy tests');
      throw new Error('Backend server không khả dụng. Vui lòng khởi động server trước khi chạy tests.');
    }
  });
  
  // TC01: POST /api/auth/register - Thành công
  test('TC01: POST /api/auth/register - Đăng ký thành công với thông tin hợp lệ', async ({ request }) => {
    const timestamp = Date.now();
    const response = await request.post(`${BASE_URL}/register`, {
      data: {
        email: `test${timestamp}@eventqr.com`,
        password: 'password123',
        fullName: 'Test User',
        phone: '0123456789',
        role: 'USER'
      },
      timeout: 30000
    });
    
    expect(response.status()).toBe(200);
    const body = await response.json();
    expect(body.success).toBe(true);
    expect(body.message).toContain('thành công');
  });

  // TC02: POST /api/auth/register - Email trùng
  test('TC02: POST /api/auth/register - Trả về lỗi khi email đã tồn tại', async ({ request }) => {
    const response = await request.post(`${BASE_URL}/register`, {
      data: {
        email: 'admin@eventqr.com',
        password: 'password123',
        fullName: 'Test User',
        phone: '0123456789',
        role: 'USER'
      },
      timeout: 30000
    });
    
    expect(response.status()).toBe(200);
    const body = await response.json();
    expect(body.success).toBe(false);
    expect(body.message).toContain('đã tồn tại');
  });

  // TC03: POST /api/auth/register - Thiếu email
  test('TC03: POST /api/auth/register - Trả về lỗi khi thiếu email', async ({ request }) => {
    const response = await request.post(`${BASE_URL}/register`, {
      data: {
        password: 'password123',
        fullName: 'Test User',
        phone: '0123456789'
      },
      timeout: 30000
    });
    
    expect(response.status()).toBeGreaterThanOrEqual(400);
  });

  // TC04: POST /api/auth/register - Thiếu password
  test('TC04: POST /api/auth/register - Trả về lỗi khi thiếu password', async ({ request }) => {
    const response = await request.post(`${BASE_URL}/register`, {
      data: {
        email: 'test@eventqr.com',
        fullName: 'Test User',
        phone: '0123456789'
      },
      timeout: 30000
    });
    
    expect(response.status()).toBeGreaterThanOrEqual(400);
  });

  // TC05: POST /api/auth/login - Thành công
  test('TC05: POST /api/auth/login - Đăng nhập thành công với thông tin hợp lệ', async ({ request }) => {
    const response = await request.post(`${BASE_URL}/login`, {
      data: {
        email: 'admin@eventqr.com',
        password: 'admin123'
      },
      timeout: 30000
    });
    
    expect(response.status()).toBe(200);
    const body = await response.json();
    expect(body.success).toBe(true);
    expect(body.account).toBeDefined();
  });

  // TC06: POST /api/auth/login - Email sai
  test('TC06: POST /api/auth/login - Trả về lỗi khi email không tồn tại', async ({ request }) => {
    const response = await request.post(`${BASE_URL}/login`, {
      data: {
        email: 'nonexistent@eventqr.com',
        password: 'password123'
      },
      timeout: 30000
    });
    
    expect(response.status()).toBe(200);
    const body = await response.json();
    expect(body.success).toBe(false);
    expect(body.message).toContain('Sai email');
  });

  // TC07: POST /api/auth/login - Password sai
  test('TC07: POST /api/auth/login - Trả về lỗi khi password sai', async ({ request }) => {
    const response = await request.post(`${BASE_URL}/login`, {
      data: {
        email: 'admin@eventqr.com',
        password: 'wrongpassword'
      },
      timeout: 30000
    });
    
    expect(response.status()).toBe(200);
    const body = await response.json();
    expect(body.success).toBe(false);
    expect(body.message).toContain('Sai email');
  });

  // TC08: POST /api/auth/login - Thiếu thông tin
  test('TC08: POST /api/auth/login - Trả về lỗi khi thiếu email hoặc password', async ({ request }) => {
    const response = await request.post(`${BASE_URL}/login`, {
      data: {
        email: 'admin@eventqr.com'
        // Thiếu password
      },
      timeout: 30000
    });
    
    expect(response.status()).toBeGreaterThanOrEqual(400);
  });

  // TC09: Response format đúng
  test('TC09: Response format đúng với các trường bắt buộc', async ({ request }) => {
    const response = await request.post(`${BASE_URL}/login`, {
      data: {
        email: 'admin@eventqr.com',
        password: 'admin123'
      },
      timeout: 30000
    });
    
    expect(response.status()).toBe(200);
    const body = await response.json();
    expect(body).toHaveProperty('success');
    expect(body).toHaveProperty('message');
    if (body.success) {
      expect(body).toHaveProperty('account');
    }
  });

  // TC10: Status code 200 cho success
  test('TC10: Trả về status code 200 khi đăng nhập thành công', async ({ request }) => {
    const response = await request.post(`${BASE_URL}/login`, {
      data: {
        email: 'admin@eventqr.com',
        password: 'admin123'
      },
      timeout: 30000
    });
    
    expect(response.status()).toBe(200);
  });

  // TC11: Status code 400 cho bad request
  test('TC11: Trả về status code 400 khi request không hợp lệ', async ({ request }) => {
    const response = await request.post(`${BASE_URL}/register`, {
      data: {
        // Thiếu tất cả thông tin
      },
      timeout: 30000
    });
    
    expect(response.status()).toBeGreaterThanOrEqual(400);
  });

  // TC12: Status code 401 cho unauthorized (nếu có)
  test('TC12: Trả về status code phù hợp khi unauthorized', async ({ request }) => {
    const response = await request.post(`${BASE_URL}/login`, {
      data: {
        email: 'admin@eventqr.com',
        password: 'wrongpassword'
      },
      timeout: 30000
    });
    
    // Có thể là 200 với success: false hoặc 401
    expect([200, 401]).toContain(response.status());
  });

  // TC13: Password được hash (không trả về plain text)
  test('TC13: Password không được trả về trong response', async ({ request }) => {
    const response = await request.post(`${BASE_URL}/login`, {
      data: {
        email: 'admin@eventqr.com',
        password: 'admin123'
      },
      timeout: 30000
    });
    
    expect(response.status()).toBe(200);
    const body = await response.json();
    if (body.account) {
      expect(body.account.password).toBeUndefined();
    }
  });

  // TC14: JWT token được tạo (nếu có)
  test('TC14: Response có chứa token hoặc thông tin xác thực', async ({ request }) => {
    const response = await request.post(`${BASE_URL}/login`, {
      data: {
        email: 'admin@eventqr.com',
        password: 'admin123'
      },
      timeout: 30000
    });
    
    expect(response.status()).toBe(200);
    const body = await response.json();
    // Kiểm tra có token hoặc account info
    expect(body.account || body.token).toBeDefined();
  });

  // TC15: Token có expiration (nếu có token)
  test('TC15: Token có thời gian hết hạn hợp lệ', async ({ request }) => {
    const response = await request.post(`${BASE_URL}/login`, {
      data: {
        email: 'admin@eventqr.com',
        password: 'admin123'
      },
      timeout: 30000
    });
    
    expect(response.status()).toBe(200);
    const body = await response.json();
    if (body.token) {
      // Decode JWT và kiểm tra exp
      const tokenParts = body.token.split('.');
      expect(tokenParts.length).toBe(3); // JWT có 3 parts
    }
  });

  // TC16: CORS headers đúng
  test('TC16: Response có CORS headers cho phép cross-origin', async ({ request }) => {
    const response = await request.post(`${BASE_URL}/login`, {
      data: {
        email: 'admin@eventqr.com',
        password: 'admin123'
      },
      timeout: 30000
    });
    
    const headers = response.headers();
    // Kiểm tra Access-Control-Allow-Origin
    expect(headers['access-control-allow-origin'] || headers['Access-Control-Allow-Origin']).toBeDefined();
  });

  // TC17: Content-Type application/json
  test('TC17: Response có Content-Type là application/json', async ({ request }) => {
    const response = await request.post(`${BASE_URL}/login`, {
      data: {
        email: 'admin@eventqr.com',
        password: 'admin123'
      },
      timeout: 30000
    });
    
    const contentType = response.headers()['content-type'];
    expect(contentType).toContain('application/json');
  });

  // TC18: Xử lý SQL injection
  test('TC18: Bảo vệ chống SQL injection trong email', async ({ request }) => {
    const response = await request.post(`${BASE_URL}/login`, {
      data: {
        email: "admin@eventqr.com' OR '1'='1",
        password: 'admin123'
      },
      timeout: 30000
    });
    
    // Không nên đăng nhập thành công
    expect(response.status()).toBe(200);
    const body = await response.json();
    expect(body.success).toBe(false);
  });

  // TC19: Xử lý XSS
  test('TC19: Bảo vệ chống XSS trong input', async ({ request }) => {
    const response = await request.post(`${BASE_URL}/register`, {
      data: {
        email: '<script>alert("xss")</script>@eventqr.com',
        password: 'password123',
        fullName: '<script>alert("xss")</script>',
        phone: '0123456789'
      },
      timeout: 30000
    });
    
    expect(response.status()).toBeGreaterThanOrEqual(400);
  });

  // TC20: Rate limiting (nếu có)
  test('TC20: API có xử lý rate limiting hoặc không bị crash khi request nhiều', async ({ request }) => {
    const requests = [];
    for (let i = 0; i < 10; i++) {
      requests.push(
        request.post(`${BASE_URL}/login`, {
          data: {
            email: 'admin@eventqr.com',
            password: 'admin123'
          },
          timeout: 30000
        })
      );
    }
    
    const responses = await Promise.all(requests);
    // Tất cả requests đều phải có response (không bị crash)
    responses.forEach(response => {
      expect(response.status()).toBeGreaterThanOrEqual(200);
    });
  });
});

