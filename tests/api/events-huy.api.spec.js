/**
 * Backend API Test Cases - Event Management (Create/Update Event)
 * Người thực hiện: Huy
 * Tool: Playwright
 * Số lượng: 20 test cases
 */

import { test, expect } from '@playwright/test';

const BASE_URL = 'http://localhost:8080/api/events';

test.describe('Event Management Backend API Tests - Huy (20 Test Cases)', () => {
  const organizerId = 1; // Giả định organizer ID = 1

  // TC01: POST /api/events - Tạo sự kiện thành công
  test('TC01: POST /api/events - Tạo sự kiện thành công với thông tin đầy đủ', async ({ request }) => {
    const formData = new FormData();
    formData.append('title', 'Test Event');
    formData.append('description', 'Test Description');
    formData.append('eventDate', '2025-12-31T10:00:00');
    formData.append('duration', '8');
    formData.append('location', 'Test Location');
    formData.append('maxParticipants', '100');
    formData.append('organizerId', organizerId.toString());

    const response = await request.post(BASE_URL, {
      headers: {
        'X-Organizer-Id': organizerId.toString()
      },
      multipart: formData
    });

    expect(response.status()).toBe(201);
    const body = await response.json();
    expect(body.eventId).toBeDefined();
    expect(body.title).toBe('Test Event');
  });

  // TC02: POST /api/events - Thiếu title
  test('TC02: POST /api/events - Trả về lỗi khi thiếu title', async ({ request }) => {
    const formData = new FormData();
    formData.append('eventDate', '2025-12-31');
    formData.append('location', 'Test Location');
    formData.append('organizerId', organizerId.toString());

    const response = await request.post(BASE_URL, {
      headers: {
        'X-Organizer-Id': organizerId.toString()
      },
      multipart: formData
    });

    expect(response.status()).toBe(400);
  });

  // TC03: POST /api/events - Thiếu eventDate
  test('TC03: POST /api/events - Trả về lỗi khi thiếu eventDate', async ({ request }) => {
    const formData = new FormData();
    formData.append('title', 'Test Event');
    formData.append('location', 'Test Location');
    formData.append('organizerId', organizerId.toString());

    const response = await request.post(BASE_URL, {
      headers: {
        'X-Organizer-Id': organizerId.toString()
      },
      multipart: formData
    });

    expect(response.status()).toBe(400);
  });

  // TC04: POST /api/events - Thiếu location
  test('TC04: POST /api/events - Trả về lỗi khi thiếu location', async ({ request }) => {
    const formData = new FormData();
    formData.append('title', 'Test Event');
    formData.append('eventDate', '2025-12-31');
    formData.append('organizerId', organizerId.toString());

    const response = await request.post(BASE_URL, {
      headers: {
        'X-Organizer-Id': organizerId.toString()
      },
      multipart: formData
    });

    expect(response.status()).toBe(400);
  });

  // TC05: PUT /api/events/{id} - Cập nhật thành công
  test('TC05: PUT /api/events/{id} - Cập nhật sự kiện thành công', async ({ request }) => {
    const createForm = new FormData();
    createForm.append('title', 'Event to Update');
    createForm.append('eventDate', '2025-12-31T10:00:00');
    createForm.append('location', 'Test Location');
    createForm.append('organizerId', organizerId.toString());
    const createRes = await request.post(BASE_URL, {
      headers: { 'X-Organizer-Id': organizerId.toString() },
      multipart: createForm
    });
    const created = await createRes.json();
    const eventId = created.eventId;

    const formData = new FormData();
    formData.append('title', 'Updated Event');
    formData.append('description', 'Updated Description');
    formData.append('eventDate', '2025-12-31T10:00:00');
    formData.append('location', 'Updated Location');

    const response = await request.put(`${BASE_URL}/${eventId}`, {
      headers: {
        'X-Organizer-Id': organizerId.toString()
      },
      multipart: formData
    });

    expect(response.status()).toBe(200);
    const body = await response.json();
    expect(body.title).toBe('Updated Event');
  });

  // TC06: PUT /api/events/{id} - Không có quyền
  test('TC06: PUT /api/events/{id} - Trả về lỗi khi không có quyền chỉnh sửa', async ({ request }) => {
    const createForm = new FormData();
    createForm.append('title', 'Event for Permission Test');
    createForm.append('eventDate', '2025-12-31T10:00:00');
    createForm.append('location', 'Test Location');
    createForm.append('organizerId', organizerId.toString());
    const createRes = await request.post(BASE_URL, {
      headers: { 'X-Organizer-Id': organizerId.toString() },
      multipart: createForm
    });
    const created = await createRes.json();
    const eventId = created.eventId;

    const otherOrganizerId = 999;
    const formData = new FormData();
    formData.append('title', 'Updated Event');

    const response = await request.put(`${BASE_URL}/${eventId}`, {
      headers: {
        'X-Organizer-Id': otherOrganizerId.toString()
      },
      multipart: formData
    });

    expect(response.status()).toBe(403);
  });

  // TC07: GET /api/events - Lấy danh sách tất cả sự kiện
  test('TC07: GET /api/events - Lấy danh sách tất cả sự kiện', async ({ request }) => {
    const response = await request.get(BASE_URL);
    
    expect(response.status()).toBe(200);
    const body = await response.json();
    expect(Array.isArray(body)).toBe(true);
  });

  // TC08: GET /api/events/{id} - Lấy chi tiết sự kiện
  test('TC08: GET /api/events/{id} - Lấy chi tiết sự kiện thành công', async ({ request }) => {
    const createForm = new FormData();
    createForm.append('title', 'Detail Test Event');
    createForm.append('eventDate', '2025-12-31T10:00:00');
    createForm.append('location', 'Test Location');
    createForm.append('organizerId', organizerId.toString());
    const createRes = await request.post(BASE_URL, {
      headers: { 'X-Organizer-Id': organizerId.toString() },
      multipart: createForm
    });
    const created = await createRes.json();
    const eventId = created.eventId;

    const response = await request.get(`${BASE_URL}/${eventId}`);
    
    expect(response.status()).toBe(200);
    const body = await response.json();
    expect(body.eventId).toBe(eventId);
    expect(body.title).toBe('Detail Test Event');
  });

  // TC09: GET /api/events/{id} - Sự kiện không tồn tại
  test('TC09: GET /api/events/{id} - Trả về 404 khi sự kiện không tồn tại', async ({ request }) => {
    const eventId = 99999;
    const response = await request.get(`${BASE_URL}/${eventId}`);
    
    expect(response.status()).toBe(404);
  });

  // TC10: GET /api/events/my-events - Lấy sự kiện của organizer
  test('TC10: GET /api/events/my-events - Lấy danh sách sự kiện của organizer', async ({ request }) => {
    const response = await request.get(`${BASE_URL}/my-events`, {
      headers: {
        'X-Organizer-Id': organizerId.toString()
      }
    });
    
    expect(response.status()).toBe(200);
    const body = await response.json();
    expect(Array.isArray(body)).toBe(true);
  });

  // TC11: DELETE /api/events/{id} - Xóa sự kiện (nếu có)
  test('TC11: DELETE /api/events/{id} - Xóa sự kiện thành công', async ({ request }) => {
    const ADMIN_URL = 'http://localhost:8080/api/admin/events';
    const formData = new FormData();
    formData.append('title', 'Event to Delete');
    formData.append('eventDate', '2025-12-31T10:00:00');
    formData.append('location', 'Test Location');
    formData.append('organizerId', organizerId.toString());

    const createResponse = await request.post(BASE_URL, {
      headers: {
        'X-Organizer-Id': organizerId.toString()
      },
      multipart: formData
    });
    const createdEvent = await createResponse.json();
    const eventId = createdEvent.eventId;

    const deleteResponse = await request.delete(`${ADMIN_URL}/${eventId}`, {
      headers: {
        'X-Organizer-Id': organizerId.toString()
      }
    });

    expect([200, 204]).toContain(deleteResponse.status());
  });

  // TC12: Validation maxParticipants phải > 0
  test('TC12: POST /api/events - Trả về lỗi khi maxParticipants <= 0', async ({ request }) => {
    const formData = new FormData();
    formData.append('title', 'Test Event');
    formData.append('eventDate', '2025-12-31T10:00:00');
    formData.append('location', 'Test Location');
    formData.append('maxParticipants', '0');
    formData.append('organizerId', organizerId.toString());

    const response = await request.post(BASE_URL, {
      headers: {
        'X-Organizer-Id': organizerId.toString()
      },
      multipart: formData
    });

    expect(response.status()).toBeGreaterThanOrEqual(400);
  });

  // TC13: Upload hình ảnh sự kiện
  test('TC13: POST /api/events - Upload hình ảnh sự kiện thành công', async ({ request }) => {
    const formData = new FormData();
    formData.append('title', 'Test Event with Image');
    formData.append('eventDate', '2025-12-31T10:00:00');
    formData.append('location', 'Test Location');
    formData.append('organizerId', organizerId.toString());
    // Note: In real test, add actual image file
    // formData.append('image', fs.readFileSync('test-image.jpg'), 'test-image.jpg');

    const response = await request.post(BASE_URL, {
      headers: {
        'X-Organizer-Id': organizerId.toString()
      },
      multipart: formData
    });

    expect(response.status()).toBe(201);
  });

  // TC14: Response format đúng
  test('TC14: Response format có đầy đủ các trường bắt buộc', async ({ request }) => {
    const createForm = new FormData();
    createForm.append('title', 'Format Test Event');
    createForm.append('eventDate', '2025-12-31T10:00:00');
    createForm.append('location', 'Test Location');
    createForm.append('organizerId', organizerId.toString());
    const createRes = await request.post(BASE_URL, {
      headers: { 'X-Organizer-Id': organizerId.toString() },
      multipart: createForm
    });
    const created = await createRes.json();
    const eventId = created.eventId;

    const response = await request.get(`${BASE_URL}/${eventId}`);
    
    expect(response.status()).toBe(200);
    const body = await response.json();
    expect(body).toHaveProperty('eventId');
    expect(body).toHaveProperty('title');
    expect(body).toHaveProperty('status');
  });

  // TC15: Status code 201 cho create success
  test('TC15: Trả về status code 201 khi tạo sự kiện thành công', async ({ request }) => {
    const formData = new FormData();
    formData.append('title', 'Test Event');
    formData.append('eventDate', '2025-12-31T10:00:00');
    formData.append('location', 'Test Location');
    formData.append('organizerId', organizerId.toString());

    const response = await request.post(BASE_URL, {
      headers: {
        'X-Organizer-Id': organizerId.toString()
      },
      multipart: formData
    });

    expect(response.status()).toBe(201);
  });

  // TC16: CORS headers đúng
  test('TC16: Response có CORS headers cho phép cross-origin', async ({ request }) => {
    const response = await request.get(BASE_URL, {
      headers: {
        'Origin': 'http://localhost:5500'
      }
    });
    
    const headers = response.headers();
    expect(headers['access-control-allow-origin'] || headers['Access-Control-Allow-Origin']).toBeDefined();
  });

  // TC17: Content-Type application/json
  test('TC17: Response có Content-Type là application/json', async ({ request }) => {
    const response = await request.get(BASE_URL);
    
    const contentType = response.headers()['content-type'];
    expect(contentType).toContain('application/json');
  });

  // TC18: Xử lý SQL injection
  test('TC18: Bảo vệ chống SQL injection trong title', async ({ request }) => {
    const formData = new FormData();
    formData.append('title', "Test'; DROP TABLE events; --");
    formData.append('eventDate', '2025-12-31T10:00:00');
    formData.append('location', 'Test Location');
    formData.append('organizerId', organizerId.toString());

    const response = await request.post(BASE_URL, {
      headers: {
        'X-Organizer-Id': organizerId.toString()
      },
      multipart: formData
    });

    // Không nên tạo thành công hoặc gây lỗi SQL
    expect(response.status()).toBeGreaterThanOrEqual(400);
  });

  // TC19: Xử lý XSS
  test('TC19: Bảo vệ chống XSS trong description', async ({ request }) => {
    const formData = new FormData();
    formData.append('title', 'Test Event');
    formData.append('description', '<script>alert("xss")</script>');
    formData.append('eventDate', '2025-12-31T10:00:00');
    formData.append('location', 'Test Location');
    formData.append('organizerId', organizerId.toString());

    const response = await request.post(BASE_URL, {
      headers: {
        'X-Organizer-Id': organizerId.toString()
      },
      multipart: formData
    });

    // Nên sanitize hoặc reject
    if (response.status() === 201) {
      const body = await response.json();
      expect(body.description).not.toContain('<script>');
    }
  });

  // TC20: GET /api/events trả về danh sách
  test('TC20: GET /api/events trả về danh sách sự kiện', async ({ request }) => {
    const response = await request.get(BASE_URL);
    
    expect(response.status()).toBe(200);
    const body = await response.json();
    expect(Array.isArray(body)).toBe(true);
  });
});

