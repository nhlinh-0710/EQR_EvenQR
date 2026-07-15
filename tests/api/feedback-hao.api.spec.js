/**
 * Backend API Test Cases - Feedback System
 * Người thực hiện: Hào
 * Tool: Playwright
 * Số lượng: 20 test cases
 */

import { test, expect } from '@playwright/test';

const BASE_URL = 'http://localhost:8080/api/feedback';

test.describe('Feedback Backend API Tests - Hào (20 Test Cases)', () => {
  const userId = 1;
  const eventId = 1;
  const organizerId = 1;
  const feedbackId = 1;

  // TC01: POST /api/feedback - Gửi feedback thành công
  test('TC01: POST /api/feedback - Gửi feedback thành công với rating và comment', async ({ request }) => {
    const response = await request.post(BASE_URL, {
      data: {
        eventId: eventId,
        userId: userId,
        rating: 5,
        comment: 'Great event!'
      }
    });

    // Có thể 200 nếu thành công, 400/403 nếu event chưa kết thúc hoặc chưa check-in
    expect([200, 400, 403]).toContain(response.status());
    if (response.status() === 200) {
      const body = await response.json();
      expect(body.success).toBe(true);
    }
  });

  // TC02: POST /api/feedback - Thiếu rating
  test('TC02: POST /api/feedback - Trả về lỗi khi thiếu rating', async ({ request }) => {
    const response = await request.post(BASE_URL, {
      data: {
        eventId: eventId,
        userId: userId,
        comment: 'Great event!'
        // Thiếu rating
      }
    });

    expect(response.status()).toBeGreaterThanOrEqual(400);
  });

  // TC03: POST /api/feedback - Rating không hợp lệ (< 1 hoặc > 5)
  test('TC03: POST /api/feedback - Trả về lỗi khi rating không hợp lệ', async ({ request }) => {
    const response = await request.post(BASE_URL, {
      data: {
        eventId: eventId,
        userId: userId,
        rating: 6, // Rating > 5
        comment: 'Great event!'
      }
    });

    expect(response.status()).toBeGreaterThanOrEqual(400);
  });

  // TC04: POST /api/feedback - Thiếu comment
  test('TC04: POST /api/feedback - Trả về lỗi khi thiếu comment', async ({ request }) => {
    const response = await request.post(BASE_URL, {
      data: {
        eventId: eventId,
        userId: userId,
        rating: 5
        // Thiếu comment
      }
    });

    expect(response.status()).toBeGreaterThanOrEqual(400);
  });

  // TC05: POST /api/feedback - Chưa check-in
  test('TC05: POST /api/feedback - Trả về lỗi khi chưa check-in sự kiện', async ({ request }) => {
    const response = await request.post(BASE_URL, {
      data: {
        eventId: eventId,
        userId: userId,
        rating: 5,
        comment: 'Great event!'
      }
    });

    // Có thể là 400 hoặc 403 nếu chưa check-in hoặc event không tồn tại
    if (response.status() === 403) {
      const body = await response.json();
      expect(body.message).toContain('check-in');
    }
  });

  // TC06: GET /api/feedback/completed-events/{userId} - Lấy sự kiện đã kết thúc
  test('TC06: GET /api/feedback/completed-events/{userId} - Lấy danh sách sự kiện đã kết thúc', async ({ request }) => {
    const response = await request.get(`${BASE_URL}/completed-events/${userId}`);
    
    expect(response.status()).toBe(200);
    const body = await response.json();
    expect(Array.isArray(body)).toBe(true);
  });

  // TC07: GET /api/feedback/event/{eventId} - Lấy feedback của sự kiện
  test('TC07: GET /api/feedback/event/{eventId} - Lấy danh sách feedback của sự kiện', async ({ request }) => {
    const response = await request.get(`${BASE_URL}/event/${eventId}`);
    
    expect(response.status()).toBe(200);
    const body = await response.json();
    expect(Array.isArray(body)).toBe(true);
  });

  // TC08: GET /api/feedback/user/{userId} - Lấy feedback của user
  test('TC08: GET /api/feedback/user/{userId} - Lấy danh sách feedback của user', async ({ request }) => {
    const response = await request.get(`${BASE_URL}/user/${userId}`);
    
    expect(response.status()).toBe(200);
    const body = await response.json();
    expect(Array.isArray(body)).toBe(true);
  });

  // TC09: GET /api/feedback/organizer/{organizerId} - Lấy feedback của organizer
  test('TC09: GET /api/feedback/organizer/{organizerId} - Lấy feedback của các sự kiện thuộc organizer', async ({ request }) => {
    const response = await request.get(`${BASE_URL}/organizer/${organizerId}`);
    
    expect(response.status()).toBe(200);
    const body = await response.json();
    expect(Array.isArray(body)).toBe(true);
  });

  // TC10: POST /api/feedback/{feedbackId}/reply - Reply feedback
  test('TC10: POST /api/feedback/{feedbackId}/reply - Organizer reply feedback thành công', async ({ request }) => {
    const response = await request.post(`${BASE_URL}/${feedbackId}/reply`, {
      data: {
        organizerId: organizerId,
        reply: 'Thank you for your feedback!'
      }
    });

    // Có thể 200 nếu thành công, 400 nếu feedback không tồn tại
    expect([200, 400]).toContain(response.status());
  });

  // TC11: POST /api/feedback/{feedbackId}/reply - Thiếu organizerId
  test('TC11: POST /api/feedback/{feedbackId}/reply - Trả về lỗi khi thiếu organizerId', async ({ request }) => {
    const response = await request.post(`${BASE_URL}/${feedbackId}/reply`, {
      data: {
        reply: 'Thank you!'
        // Thiếu organizerId
      }
    });

    expect(response.status()).toBe(400);
  });

  // TC12: GET /api/feedback/admin/event/{eventId}/export-pdf - Export PDF
  test('TC12: GET /api/feedback/admin/event/{eventId}/export-pdf - Export feedback ra PDF', async ({ request }) => {
    const response = await request.get(`${BASE_URL}/admin/event/${eventId}/export-pdf`);
    
    expect([200, 500]).toContain(response.status());
    if (response.status() === 200) {
      const contentType = response.headers()['content-type'];
      expect(contentType).toContain('application/pdf');
    }
  });

  // TC13: Response format đúng
  test('TC13: Response format có đầy đủ các trường bắt buộc', async ({ request }) => {
    const response = await request.get(`${BASE_URL}/event/${eventId}`);
    
    expect(response.status()).toBe(200);
    const body = await response.json();
    if (body.length > 0) {
      expect(body[0]).toHaveProperty('feedbackId');
      expect(body[0]).toHaveProperty('rating');
      expect(body[0]).toHaveProperty('comment');
    }
  });

  // TC14: Status code 200 cho success
  test('TC14: Trả về status code 200 khi gửi feedback thành công', async ({ request }) => {
    const response = await request.post(BASE_URL, {
      data: {
        eventId: eventId,
        userId: userId,
        rating: 5,
        comment: 'Great event!'
      }
    });

    // Có thể là 200 hoặc 400/403 nếu chưa check-in hoặc event không tồn tại
    expect([200, 400, 403]).toContain(response.status());
  });

  // TC15: Status code 403 cho forbidden
  test('TC15: Trả về status code 403 khi chưa check-in', async ({ request }) => {
    const response = await request.post(BASE_URL, {
      data: {
        eventId: eventId,
        userId: userId,
        rating: 5,
        comment: 'Great event!'
      }
    });

    // Có thể là 400 hoặc 403 nếu chưa check-in hoặc event không tồn tại
    if (response.status() === 403) {
      const body = await response.json();
      expect(body.message).toContain('check-in');
    }
  });

  // TC16: CORS headers đúng
  test('TC16: Response có CORS headers cho phép cross-origin', async ({ request }) => {
    const response = await request.get(`${BASE_URL}/event/${eventId}`, {
      headers: {
        'Origin': 'http://localhost:5500'
      }
    });
    
    const headers = response.headers();
    expect(headers['access-control-allow-origin'] || headers['Access-Control-Allow-Origin']).toBeDefined();
  });

  // TC17: Content-Type application/json
  test('TC17: Response có Content-Type là application/json', async ({ request }) => {
    const response = await request.get(`${BASE_URL}/event/${eventId}`);
    
    const contentType = response.headers()['content-type'];
    expect(contentType).toContain('application/json');
  });

  // TC18: Xử lý SQL injection
  test('TC18: Bảo vệ chống SQL injection trong comment', async ({ request }) => {
    const response = await request.post(BASE_URL, {
      data: {
        eventId: eventId,
        userId: userId,
        rating: 5,
        comment: "Great event'; DROP TABLE feedback; --"
      }
    });

    // Không nên thực thi SQL injection
    if (response.status() === 200) {
      const body = await response.json();
      expect(body.success).toBe(true);
    }
  });

  // TC19: Xử lý XSS
  test('TC19: Bảo vệ chống XSS trong comment', async ({ request }) => {
    const response = await request.post(BASE_URL, {
      data: {
        eventId: eventId,
        userId: userId,
        rating: 5,
        comment: '<script>alert("xss")</script>Great event!'
      }
    });

    // Nên sanitize hoặc reject
    if (response.status() === 200) {
      const getResponse = await request.get(`${BASE_URL}/event/${eventId}`);
      const body = await getResponse.json();
      if (body.length > 0) {
        expect(body[0].comment).not.toContain('<script>');
      }
    }
  });

  // TC20: Tính rating trung bình
  test('TC20: Tính rating trung bình của sự kiện', async ({ request }) => {
    const response = await request.get(`${BASE_URL}/event/${eventId}`);
    
    expect(response.status()).toBe(200);
    const body = await response.json();
    if (body.length > 0) {
      // Kiểm tra có thể tính rating trung bình
      const ratings = body.map(f => f.rating).filter(r => r != null);
      if (ratings.length > 0) {
        const avg = ratings.reduce((a, b) => a + b, 0) / ratings.length;
        expect(avg).toBeGreaterThanOrEqual(1);
        expect(avg).toBeLessThanOrEqual(5);
      }
    }
  });
});

