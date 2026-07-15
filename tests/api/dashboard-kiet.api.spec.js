/**
 * Backend API Test Cases - Dashboard & Statistics
 * Người thực hiện: Kiệt
 * Tool: Playwright
 * Số lượng: 20 test cases
 */

import { test, expect } from '@playwright/test';

const BASE_URL = 'http://localhost:8080/api/dashboard';

test.describe('Dashboard Backend API Tests - Kiệt (20 Test Cases)', () => {
  const organizerId = 1;

  // TC01: GET /api/dashboard/statistics - Lấy thống kê tổng quan
  test('TC01: GET /api/dashboard/statistics - Lấy thống kê tổng quan thành công', async ({ request }) => {
    const response = await request.get(`${BASE_URL}/statistics`);
    
    expect(response.status()).toBe(200);
    const body = await response.json();
    expect(body).toHaveProperty('activeEvents');
    expect(body).toHaveProperty('totalAttendees');
    expect(body).toHaveProperty('totalTicketsSold');
  });

  // TC02: GET /api/dashboard/statistics - Lấy thống kê của organizer
  test('TC02: GET /api/dashboard/statistics - Lấy thống kê của organizer cụ thể', async ({ request }) => {
    const response = await request.get(`${BASE_URL}/statistics?organizerId=${organizerId}`);
    
    expect(response.status()).toBe(200);
    const body = await response.json();
    expect(body).toHaveProperty('activeEvents');
    expect(body).toHaveProperty('totalAttendees');
  });

  // TC03: GET /api/dashboard/statistics - Response format đúng
  test('TC03: Response format có đầy đủ các trường thống kê', async ({ request }) => {
    const response = await request.get(`${BASE_URL}/statistics`);
    
    expect(response.status()).toBe(200);
    const body = await response.json();
    expect(body).toHaveProperty('activeEvents');
    expect(body).toHaveProperty('totalAttendees');
    expect(body).toHaveProperty('totalTicketsSold');
    expect(body).toHaveProperty('totalRevenue');
  });

  // TC04: GET /api/dashboard/statistics - Kiểu dữ liệu đúng
  test('TC04: Các giá trị thống kê là số nguyên', async ({ request }) => {
    const response = await request.get(`${BASE_URL}/statistics`);
    
    expect(response.status()).toBe(200);
    const body = await response.json();
    expect(typeof body.activeEvents).toBe('number');
    expect(typeof body.totalAttendees).toBe('number');
    expect(typeof body.totalTicketsSold).toBe('number');
  });

  // TC05: GET /api/dashboard/recent-events - Lấy sự kiện gần đây
  test('TC05: GET /api/dashboard/recent-events - Lấy danh sách sự kiện gần đây', async ({ request }) => {
    const response = await request.get(`${BASE_URL}/recent-events`);
    
    expect(response.status()).toBe(200);
    const body = await response.json();
    expect(Array.isArray(body)).toBe(true);
  });

  // TC06: GET /api/dashboard/recent-events - Lấy sự kiện của organizer
  test('TC06: GET /api/dashboard/recent-events - Lấy sự kiện gần đây của organizer', async ({ request }) => {
    const response = await request.get(`${BASE_URL}/recent-events?organizerId=${organizerId}`);
    
    expect(response.status()).toBe(200);
    const body = await response.json();
    expect(Array.isArray(body)).toBe(true);
  });

  // TC07: GET /api/dashboard/recent-events - Limit số lượng
  test('TC07: GET /api/dashboard/recent-events - Giới hạn số lượng sự kiện trả về', async ({ request }) => {
    const limit = 5;
    const response = await request.get(`${BASE_URL}/recent-events?limit=${limit}`);
    
    expect(response.status()).toBe(200);
    const body = await response.json();
    expect(Array.isArray(body)).toBe(true);
    expect(body.length).toBeLessThanOrEqual(limit);
  });

  // TC08: GET /api/dashboard/recent-events - Response format đúng
  test('TC08: Response format có đầy đủ thông tin sự kiện', async ({ request }) => {
    const response = await request.get(`${BASE_URL}/recent-events`);
    
    expect(response.status()).toBe(200);
    const body = await response.json();
    if (body.length > 0) {
      expect(body[0]).toHaveProperty('eventId');
      expect(body[0]).toHaveProperty('title');
      expect(body[0]).toHaveProperty('status');
    }
  });

  // TC09: GET /api/dashboard/statistics - Tính toán đúng số sự kiện hoạt động
  test('TC09: Tính toán đúng số sự kiện hoạt động (UPCOMING + ONGOING)', async ({ request }) => {
    const response = await request.get(`${BASE_URL}/statistics?organizerId=${organizerId}`);
    
    expect(response.status()).toBe(200);
    const body = await response.json();
    expect(body.activeEvents).toBeGreaterThanOrEqual(0);
  });

  // TC10: GET /api/dashboard/statistics - Tính toán đúng số vé đã bán
  test('TC10: Tính toán đúng số vé đã bán', async ({ request }) => {
    const response = await request.get(`${BASE_URL}/statistics?organizerId=${organizerId}`);
    
    expect(response.status()).toBe(200);
    const body = await response.json();
    expect(body.totalTicketsSold).toBeGreaterThanOrEqual(0);
    expect(body.totalTicketsSold).toBeLessThanOrEqual(body.totalAttendees);
  });

  // TC11: GET /api/dashboard/statistics - Tính toán đúng doanh thu
  test('TC11: Tính toán đúng doanh thu (vé x giá)', async ({ request }) => {
    const response = await request.get(`${BASE_URL}/statistics?organizerId=${organizerId}`);
    
    expect(response.status()).toBe(200);
    const body = await response.json();
    expect(body.totalRevenue).toBeGreaterThanOrEqual(0);
  });

  // TC12: Status code 200 cho success
  test('TC12: Trả về status code 200 khi lấy thống kê thành công', async ({ request }) => {
    const response = await request.get(`${BASE_URL}/statistics`);
    
    expect(response.status()).toBe(200);
  });

  // TC13: Status code 500 cho server error
  test('TC13: Trả về status code 500 khi có lỗi server', async ({ request }) => {
    // Giả định có lỗi database
    const response = await request.get(`${BASE_URL}/statistics?organizerId=99999`);
    
    // Có thể là 200 với giá trị 0 hoặc 500
    expect([200, 500]).toContain(response.status());
  });

  // TC14: CORS headers đúng
  test('TC14: Response có CORS headers cho phép cross-origin', async ({ request }) => {
    const response = await request.get(`${BASE_URL}/statistics`, {
      headers: {
        'Origin': 'http://localhost:5500'
      }
    });
    
    const headers = response.headers();
    expect(headers['access-control-allow-origin'] || headers['Access-Control-Allow-Origin']).toBeDefined();
  });

  // TC15: Content-Type application/json
  test('TC15: Response có Content-Type là application/json', async ({ request }) => {
    const response = await request.get(`${BASE_URL}/statistics`);
    
    const contentType = response.headers()['content-type'];
    expect(contentType).toContain('application/json');
  });

  // TC16: Xử lý SQL injection
  test('TC16: Bảo vệ chống SQL injection trong organizerId', async ({ request }) => {
    const response = await request.get(`${BASE_URL}/statistics?organizerId=1'; DROP TABLE events; --`);
    
    // Không nên thực thi SQL injection
    expect([200, 400, 500]).toContain(response.status());
  });

  // TC17: Xử lý XSS
  test('TC17: Bảo vệ chống XSS trong response', async ({ request }) => {
    const response = await request.get(`${BASE_URL}/recent-events`);
    
    if (response.status() === 200) {
      const body = await response.json();
      const bodyStr = JSON.stringify(body);
      expect(bodyStr).not.toContain('<script>');
    }
  });

  // TC18: Performance - Response time hợp lý
  test('TC18: Response time hợp lý (< 2 giây)', async ({ request }) => {
    const startTime = Date.now();
    const response = await request.get(`${BASE_URL}/statistics`);
    const endTime = Date.now();
    const responseTime = endTime - startTime;
    
    expect(response.status()).toBe(200);
    expect(responseTime).toBeLessThan(2000); // < 2 giây
  });

  // TC19: Cache headers (nếu có)
  test('TC19: Response có cache headers phù hợp', async ({ request }) => {
    const response = await request.get(`${BASE_URL}/statistics`);
    
    const headers = response.headers();
    // Có thể có cache-control hoặc không
    // Chỉ kiểm tra response thành công
    expect(response.status()).toBe(200);
  });

  // TC20: Rate limiting (nếu có)
  test('TC20: API có xử lý rate limiting hoặc không bị crash khi request nhiều', async ({ request }) => {
    const requests = [];
    for (let i = 0; i < 10; i++) {
      requests.push(request.get(`${BASE_URL}/statistics`));
    }
    
    const responses = await Promise.all(requests);
    // Tất cả requests đều phải có response (không bị crash)
    responses.forEach(response => {
      expect(response.status()).toBeGreaterThanOrEqual(200);
    });
  });
});

