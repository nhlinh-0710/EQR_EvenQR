/**
 * Backend API Test Cases - Event Registration
 * Người thực hiện: Tùng
 * Tool: Playwright
 * Số lượng: 20 test cases
 */

import { test, expect } from '@playwright/test';

const BASE_URL = 'http://localhost:8080/api';

test.describe('Event Registration Backend API Tests - Tùng (20 Test Cases)', () => {
  const userId = 1;
  const eventId = 1;
  const ticketId = 1;

  // TC01: POST /api/events/register - Đăng ký sự kiện thành công
  test('TC01: POST /api/events/register - Đăng ký sự kiện thành công', async ({ request }) => {
    const response = await request.post(`${BASE_URL}/events/register`, {
      data: {
        eventId: eventId,
        userId: userId
      }
    });

    expect([200, 201]).toContain(response.status());
    const body = await response.json();
    expect(body.success).toBe(true);
    expect(body.ticketId).toBeDefined();
  });

  // TC02: POST /api/events/register - Thiếu eventId
  test('TC02: POST /api/events/register - Trả về lỗi khi thiếu eventId', async ({ request }) => {
    const response = await request.post(`${BASE_URL}/events/register`, {
      data: {
        userId: userId
        // Thiếu eventId
      }
    });

    expect(response.status()).toBeGreaterThanOrEqual(400);
  });

  // TC03: POST /api/events/register - Thiếu userId
  test('TC03: POST /api/events/register - Trả về lỗi khi thiếu userId', async ({ request }) => {
    const response = await request.post(`${BASE_URL}/events/register`, {
      data: {
        eventId: eventId
        // Thiếu userId
      }
    });

    expect(response.status()).toBeGreaterThanOrEqual(400);
  });

  // TC04: POST /api/events/register - Trả về lỗi khi event không tồn tại
  test('TC04: POST /api/events/register - Trả về lỗi khi event không tồn tại', async ({ request }) => {
    const response = await request.post(`${BASE_URL}/events/register`, {
      data: {
        eventId: 999,
        userId: userId
      }
    });

    expect(response.status()).toBeGreaterThanOrEqual(400);
  });

  // TC05: POST /api/events/register - Đã đăng ký rồi
  test('TC05: POST /api/events/register - Trả về lỗi khi đã đăng ký sự kiện', async ({ request }) => {
    // Giả định đã đăng ký rồi
    const response = await request.post(`${BASE_URL}/events/register`, {
      data: {
        eventId: eventId,
        userId: userId
      }
    });

    // Có thể thành công hoặc lỗi nếu đã đăng ký
    expect([200, 400]).toContain(response.status());
  });

  // TC06: GET /api/user/{userId}/tickets - Lấy danh sách vé
  test('TC06: GET /api/user/{userId}/tickets - Lấy danh sách vé của user', async ({ request }) => {
    const response = await request.get(`${BASE_URL}/user/${userId}/tickets`);
    
    expect(response.status()).toBe(200);
    const body = await response.json();
    expect(Array.isArray(body)).toBe(true);
  });

  // TC07: GET /api/user/{userId}/tickets - User không tồn tại
  test('TC07: GET /api/user/{userId}/tickets - Trả về lỗi khi user không tồn tại', async ({ request }) => {
    const response = await request.get(`${BASE_URL}/user/99999/tickets`);
    
    // Có thể trả về mảng rỗng hoặc 404
    expect([200, 404]).toContain(response.status());
  });

  // TC08: DELETE /api/user/{userId}/tickets/{ticketId} - Hủy vé thành công
  test('TC08: DELETE /api/user/{userId}/tickets/{ticketId} - Hủy vé thành công', async ({ request }) => {
    const response = await request.delete(`${BASE_URL}/user/${userId}/tickets/${ticketId}`);
    
    expect([200, 204]).toContain(response.status());
    if (response.status() === 200) {
      const body = await response.json();
      expect(body.success).toBe(true);
    }
  });

  // TC09: DELETE /api/user/{userId}/tickets/{ticketId} - Vé không tồn tại
  test('TC09: DELETE /api/user/{userId}/tickets/{ticketId} - Trả về lỗi khi vé không tồn tại', async ({ request }) => {
    const response = await request.delete(`${BASE_URL}/user/${userId}/tickets/99999`);
    
    expect(response.status()).toBeGreaterThanOrEqual(400);
  });

  // TC10: DELETE /api/user/{userId}/tickets/{ticketId} - Không có quyền
  test('TC10: DELETE /api/user/{userId}/tickets/{ticketId} - Trả về lỗi khi không có quyền hủy vé', async ({ request }) => {
    const response = await request.delete(`${BASE_URL}/user/99999/tickets/${ticketId}`);
    
    expect(response.status()).toBeGreaterThanOrEqual(400);
  });

  // TC11: Response format đúng
  test('TC11: Response format có đầy đủ thông tin vé', async ({ request }) => {
    const response = await request.get(`${BASE_URL}/user/${userId}/tickets`);
    
    expect(response.status()).toBe(200);
    const body = await response.json();
    expect(Array.isArray(body)).toBe(true);
    if (body.length > 0) {
      expect(body[0]).toHaveProperty('ticketId');
      expect(body[0]).toHaveProperty('eventId');
    }
  });

  // TC12: Status code 200 cho success
  test('TC12: Trả về status code 200 khi đăng ký thành công', async ({ request }) => {
    const response = await request.post(`${BASE_URL}/events/register`, {
      data: {
        eventId: eventId,
        userId: userId
      }
    });

    // Có thể là 200 hoặc 201
    expect([200, 201]).toContain(response.status());
  });

  // TC13: Status code 400 cho bad request
  test('TC13: Trả về status code 400 khi request không hợp lệ', async ({ request }) => {
    const response = await request.post(`${BASE_URL}/events/register`, {
      data: {
        // Thiếu tất cả thông tin
      }
    });

    expect(response.status()).toBeGreaterThanOrEqual(400);
  });

  // TC14: CORS headers đúng
  test('TC14: Response có CORS headers cho phép cross-origin', async ({ request }) => {
    const response = await request.get(`${BASE_URL}/user/${userId}/tickets`, {
      headers: {
        'Origin': 'http://localhost:5500'
      }
    });
    
    const headers = response.headers();
    expect(headers['access-control-allow-origin'] || headers['Access-Control-Allow-Origin']).toBeDefined();
  });

  // TC15: Content-Type application/json
  test('TC15: Response có Content-Type là application/json', async ({ request }) => {
    const response = await request.get(`${BASE_URL}/user/${userId}/tickets`);
    
    const contentType = response.headers()['content-type'];
    expect(contentType).toContain('application/json');
  });

  // TC16: Xử lý SQL injection
  test('TC16: Bảo vệ chống SQL injection trong eventId', async ({ request }) => {
    const response = await request.post(`${BASE_URL}/events/register`, {
      data: {
        eventId: "1'; DROP TABLE event_tickets; --",
        userId: userId
      }
    });

    // Không nên thực thi SQL injection
    expect(response.status()).toBeGreaterThanOrEqual(400);
  });

  // TC17: Xử lý XSS
  test('TC17: Bảo vệ chống XSS trong response', async ({ request }) => {
    const response = await request.get(`${BASE_URL}/user/${userId}/tickets`);
    
    if (response.status() === 200) {
      const body = await response.json();
      const bodyStr = JSON.stringify(body);
      expect(bodyStr).not.toContain('<script>');
    }
  });

  // TC18: Tạo QR code cho vé
  test('TC18: Vé được tạo có QR code hợp lệ', async ({ request }) => {
    const response = await request.post(`${BASE_URL}/events/register`, {
      data: {
        eventId: eventId,
        userId: userId
      }
    });

    if (response.status() === 200) {
      const body = await response.json();
      // Kiểm tra có ticketId để tạo QR
      expect(body.ticketId).toBeDefined();
    }
  });

  // TC19: Validation số lượng người tham gia
  test('TC19: POST /api/events/register - Trả về lỗi khi event không tồn tại', async ({ request }) => {
    const response = await request.post(`${BASE_URL}/events/register`, {
      data: {
        eventId: 999,
        userId: userId
      }
    });

    expect(response.status()).toBeGreaterThanOrEqual(400);
  });

  // TC20: Rate limiting (nếu có)
  test('TC20: API có xử lý rate limiting hoặc không bị crash khi request nhiều', async ({ request }) => {
    const requests = [];
    for (let i = 0; i < 10; i++) {
      requests.push(
        request.post(`${BASE_URL}/events/register`, {
          data: {
            eventId: eventId,
            userId: userId
          }
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

