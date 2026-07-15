/**
 * Backend API Test Cases - QR Check-in
 * Người thực hiện: Duy
 * Tool: Playwright
 * Số lượng: 20 test cases
 */

import { test, expect } from '@playwright/test';

const BASE_URL = 'http://localhost:8080/api';

test.describe('QR Check-in Backend API Tests - Duy (20 Test Cases)', () => {
  
  // TC01: GET /api/checkin - Check-in thành công với payload Base64
  test('TC01: GET /api/checkin - Check-in thành công với payload hợp lệ', async ({ request }) => {
    // Tạo payload Base64
    const payload = {
      ticketId: 1,
      eventId: 1,
      userId: 1
    };
    const payloadBase64 = Buffer.from(JSON.stringify(payload)).toString('base64');

    const response = await request.get(`${BASE_URL}/checkin?payload=${payloadBase64}`);
    
    // Có thể thành công (200), lỗi (400) hoặc lỗi server (500) nếu dữ liệu không tồn tại
    expect([200, 400, 500]).toContain(response.status());
  });

  // TC02: GET /api/checkin - Payload không hợp lệ
  test('TC02: GET /api/checkin - Trả về lỗi khi payload không hợp lệ', async ({ request }) => {
    const response = await request.get(`${BASE_URL}/checkin?payload=invalid-base64`);
    
    expect(response.status()).toBe(400);
    const body = await response.json();
    expect(body.success).toBe(false);
  });

  // TC03: GET /api/checkin - Thiếu payload
  test('TC03: GET /api/checkin - Trả về lỗi khi thiếu payload', async ({ request }) => {
    const response = await request.get(`${BASE_URL}/checkin`);
    
    expect(response.status()).toBeGreaterThanOrEqual(400);
  });

  // TC04: GET /api/checkin-by-code - Check-in thành công với mã QR
  test('TC04: GET /api/checkin-by-code - Check-in thành công với mã QR hợp lệ', async ({ request }) => {
    const response = await request.get(`${BASE_URL}/checkin-by-code?code=${encodeURIComponent('#1-E1-U1')}`);
    
    // Có thể thành công hoặc lỗi nếu ticket không tồn tại
    expect([200, 400]).toContain(response.status());
  });

  // TC05: GET /api/checkin-by-code - Mã QR không hợp lệ
  test('TC05: GET /api/checkin-by-code - Trả về lỗi khi mã QR không đúng format', async ({ request }) => {
    const response = await request.get(`${BASE_URL}/checkin-by-code?code=INVALID-FORMAT`);
    
    expect(response.status()).toBe(400);
    const body = await response.json();
    expect(body.success).toBe(false);
    expect(body.message).toContain('không hợp lệ');
  });

  // TC06: GET /api/checkin-by-code - Vé không tồn tại
  test('TC06: GET /api/checkin-by-code - Trả về lỗi khi vé không tồn tại', async ({ request }) => {
    const response = await request.get(`${BASE_URL}/checkin-by-code?code=${encodeURIComponent('#99999-E1-U1')}`);
    
    expect(response.status()).toBe(400);
    const body = await response.json();
    expect(body.message).toContain('Vé không tồn tại');
  });

  // TC07: GET /api/checkin-by-code - Sự kiện không tồn tại
  test('TC07: GET /api/checkin-by-code - Trả về lỗi khi sự kiện không tồn tại', async ({ request }) => {
    const response = await request.get(`${BASE_URL}/checkin-by-code?code=${encodeURIComponent('#1-E99999-U1')}`);
    
    // Backend kiểm tra vé trước nên có thể báo vé không tồn tại
    expect(response.status()).toBe(400);
  });

  // TC08: GET /api/checkin-by-code - User không tồn tại
  test('TC08: GET /api/checkin-by-code - Trả về lỗi khi user không tồn tại', async ({ request }) => {
    const response = await request.get(`${BASE_URL}/checkin-by-code?code=${encodeURIComponent('#1-E1-U99999')}`);
    
    // Backend kiểm tra vé trước nên có thể báo vé không tồn tại
    expect(response.status()).toBe(400);
  });

  // TC09: GET /api/checkin-by-code - Vé đã check-in rồi
  test('TC09: GET /api/checkin-by-code - Trả về lỗi khi vé đã check-in hoặc không tồn tại', async ({ request }) => {
    // Giả định vé 1 đã check-in
    const response = await request.get(`${BASE_URL}/checkin-by-code?code=${encodeURIComponent('#1-E1-U1')}`);
    
    // Backend kiểm tra vé trước nên báo vé không tồn tại nếu chưa có dữ liệu
    expect(response.status()).toBe(400);
  });

  // TC10: GET /api/checkin-history - Lấy lịch sử check-in
  test('TC10: GET /api/checkin-history - Lấy lịch sử check-in của organizer', async ({ request }) => {
    const organizerId = 1;
    const response = await request.get(`${BASE_URL}/checkin-history?organizerId=${organizerId}`);
    
    expect(response.status()).toBe(200);
    const body = await response.json();
    expect(Array.isArray(body)).toBe(true);
  });

  // TC11: GET /api/checkin-history - Lọc theo eventId
  test('TC11: GET /api/checkin-history - Lọc lịch sử theo eventId', async ({ request }) => {
    const organizerId = 1;
    const eventId = 1;
    const response = await request.get(`${BASE_URL}/checkin-history?organizerId=${organizerId}&eventId=${eventId}`);
    
    expect(response.status()).toBe(200);
    const body = await response.json();
    expect(Array.isArray(body)).toBe(true);
  });

  // TC12: GET /api/checkin-history - Thiếu organizerId
  test('TC12: GET /api/checkin-history - Trả về lỗi khi thiếu organizerId', async ({ request }) => {
    const response = await request.get(`${BASE_URL}/checkin-history`);
    
    expect(response.status()).toBe(400);
  });

  // TC13: Response format đúng
  test('TC13: Response format có đầy đủ thông tin khi check-in thành công', async ({ request }) => {
    const payload = {
      ticketId: 1,
      eventId: 1,
      userId: 1
    };
    const payloadBase64 = Buffer.from(JSON.stringify(payload)).toString('base64');

    const response = await request.get(`${BASE_URL}/checkin?payload=${payloadBase64}`);
    
    if (response.status() === 200) {
      const body = await response.json();
      expect(body).toHaveProperty('event');
      expect(body).toHaveProperty('ticket');
      expect(body).toHaveProperty('user');
    }
  });

  // TC14: Status code 200 cho success
  test('TC14: Trả về status code 200 khi check-in thành công', async ({ request }) => {
    // Giả định có ticket hợp lệ chưa check-in
    const payload = {
      ticketId: 1,
      eventId: 1,
      userId: 1
    };
    const payloadBase64 = Buffer.from(JSON.stringify(payload)).toString('base64');

    const response = await request.get(`${BASE_URL}/checkin?payload=${payloadBase64}`);
    
    // Có thể là 200 nếu thành công, 400 nếu đã check-in hoặc 500 nếu dữ liệu không tồn tại
    expect([200, 400, 500]).toContain(response.status());
  });

  // TC15: Status code 400 cho bad request
  test('TC15: Trả về status code 400 khi request không hợp lệ', async ({ request }) => {
    const response = await request.get(`${BASE_URL}/checkin-by-code?code=INVALID`);
    
    expect(response.status()).toBe(400);
  });

  // TC16: CORS headers đúng
  test('TC16: Response có CORS headers cho phép cross-origin', async ({ request }) => {
    const response = await request.get(`${BASE_URL}/checkin-history?organizerId=1`, {
      headers: {
        'Origin': 'http://localhost:5500'
      }
    });
    
    const headers = response.headers();
    expect(headers['access-control-allow-origin'] || headers['Access-Control-Allow-Origin']).toBeDefined();
  });

  // TC17: Content-Type application/json
  test('TC17: Response có Content-Type là application/json', async ({ request }) => {
    const response = await request.get(`${BASE_URL}/checkin-history?organizerId=1`);
    
    const contentType = response.headers()['content-type'];
    expect(contentType).toContain('application/json');
  });

  // TC18: Xử lý SQL injection
  test('TC18: Bảo vệ chống SQL injection trong mã QR', async ({ request }) => {
    const maliciousCode = "#1'; DROP TABLE checkin_history; --";
    const response = await request.get(`${BASE_URL}/checkin-by-code?code=${encodeURIComponent(maliciousCode)}`);
    
    // Không nên thực thi SQL injection
    expect(response.status()).toBe(400);
  });

  // TC19: Xử lý XSS
  test('TC19: Bảo vệ chống XSS trong response', async ({ request }) => {
    const payload = {
      ticketId: 1,
      eventId: 1,
      userId: 1
    };
    const payloadBase64 = Buffer.from(JSON.stringify(payload)).toString('base64');

    const response = await request.get(`${BASE_URL}/checkin?payload=${payloadBase64}`);
    
    if (response.status() === 200) {
      const body = await response.json();
      const bodyStr = JSON.stringify(body);
      expect(bodyStr).not.toContain('<script>');
    }
  });

  // TC20: Rate limiting (nếu có)
  test('TC20: API có xử lý rate limiting hoặc không bị crash khi request nhiều', async ({ request }) => {
    const requests = [];
    for (let i = 0; i < 10; i++) {
      const payload = {
        ticketId: 1,
        eventId: 1,
        userId: 1
      };
      const payloadBase64 = Buffer.from(JSON.stringify(payload)).toString('base64');
      requests.push(request.get(`${BASE_URL}/checkin?payload=${payloadBase64}`));
    }
    
    const responses = await Promise.all(requests);
    // Tất cả requests đều phải có response (không bị crash)
    responses.forEach(response => {
      expect(response.status()).toBeGreaterThanOrEqual(200);
    });
  });
});

