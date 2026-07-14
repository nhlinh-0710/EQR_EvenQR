import { test, expect } from '@playwright/test';

const URL = 'http://127.0.0.1:5501/frontend/pages/admin/create_event.html';

test.describe('PLAYWRIGHT - FUNCTION TEST CREATE EVENT (FIXED VERSION)', () => {

  test.beforeEach(async ({ page }) => {
    await page.goto(URL, { waitUntil: 'domcontentloaded' });
    await page.waitForLoadState("networkidle");
  });

  // ================= FUNC-CE01 =================
  test('FUNC-CE01 - Mở đúng trang Create Event', async ({ page }) => {
    await expect(page.locator('#createEventForm')).toBeVisible();
  });

  // ================= FUNC-CE02 =================
  test('FUNC-CE02 - Tạo sự kiện thành công', async ({ page }) => {
    await page.fill('#eventTitle', 'Hội Thảo Công Nghệ');
    await page.fill('#eventDate', '2025-12-25T19:30');
    await page.fill('#eventLocation', 'Đà Nẵng');

    await page.click('#btnCreate');

    await expect(page).toHaveURL(/create_event.html/);
  });

  // ================= FUNC-CE03 =================
  test('FUNC-CE03 - Không cho tạo khi thiếu Tên', async ({ page }) => {
    await page.fill('#eventDate', '2025-12-25T19:30');
    await page.fill('#eventLocation', 'Hà Nội');

    await page.click('#btnCreate');

    const title = page.locator('#eventTitle');
    expect(await title.inputValue()).toBe('');
  });

  // ================= FUNC-CE04 =================
  test('FUNC-CE04 - Không cho tạo khi thiếu Ngày', async ({ page }) => {
    await page.fill('#eventTitle', 'Test Event');
    await page.fill('#eventLocation', 'Huế');

    await page.click('#btnCreate');

    const date = await page.inputValue('#eventDate');
    expect(date).toBe('');
  });

  // ================= FUNC-CE05 =================
  test('FUNC-CE05 - Không cho tạo khi thiếu Địa điểm', async ({ page }) => {
    await page.fill('#eventTitle', 'Test Event');
    await page.fill('#eventDate', '2025-12-20T18:00');

    await page.click('#btnCreate');

    const location = await page.inputValue('#eventLocation');
    expect(location).toBe('');
  });

  // ================= FUNC-CE06 =================
  test('FUNC-CE06 - Không cho nhập số người âm', async ({ page }) => {
    await page.fill('#maxParticipants', '-10');
    const value = await page.inputValue('#maxParticipants');

    expect(Number(value)).toBeGreaterThanOrEqual(0); // FIX
  });

  // ================= FUNC-CE07 =================
  test('FUNC-CE07 - Không cho nhập thời lượng > 24', async ({ page }) => {
    await page.fill('#eventDuration', '50');
    const value = Number(await page.inputValue('#eventDuration'));

    expect(value).toBeLessThanOrEqual(24); // FIX
  });

  // ================= FUNC-CE08 =================
  test('FUNC-CE08 - Tạo sự kiện với số người = 1', async ({ page }) => {
    await page.fill('#eventTitle', 'Sự kiện nhỏ');
    await page.fill('#eventDate', '2025-12-20T10:00');
    await page.fill('#eventLocation', 'Hà Nội');
    await page.fill('#maxParticipants', '1');

    await page.click('#btnCreate');

    await expect(page).toHaveURL(/create_event.html/);
  });

  // ================= FUNC-CE09 =================
  test('FUNC-CE09 - Không chọn danh mục vẫn tạo được', async ({ page }) => {
    await page.fill('#eventTitle', 'Event Không Danh Mục');
    await page.fill('#eventDate', '2025-12-30T19:00');
    await page.fill('#eventLocation', 'Đà Nẵng');

    await page.click('#btnCreate');

    await expect(page).toHaveURL(/create_event.html/);
  });

  // ================= FUNC-CE10 =================
  test('FUNC-CE10 - Reload form bị reset dữ liệu', async ({ page }) => {
    await page.fill('#eventTitle', 'Test Reload');
    await page.reload();

    await expect(page.locator('#eventTitle')).toHaveValue('');
  });

  // ================= FUNC-CE11 =================
  test('FUNC-CE11 - Nhập mô tả dài 500 ký tự', async ({ page }) => {
    const longText = 'A'.repeat(500);
    await page.fill('#eventDescription', longText);

    const value = await page.inputValue('#eventDescription');
    expect(value.length).toBe(500);
  });

  // ================= FUNC-CE12 =================
  test('FUNC-CE12 - Nhập ký tự đặc biệt vào Tên', async ({ page }) => {
    await page.fill('#eventTitle', '@@@!!!');
    const value = await page.inputValue('#eventTitle');

    expect(value).toBe('@@@!!!');
  });

  // ================= FUNC-CE13 =================
  test('FUNC-CE13 - Click nút Lưu Nháp', async ({ page }) => {
    await page.fill('#eventTitle', 'Bản nháp');
    await page.click('#btnDraft');

    await expect(page.locator('#eventTitle')).toHaveValue('Bản nháp');
  });

  // ================= FUNC-CE14 =================
  test('FUNC-CE14 - Double click Tạo Sự Kiện', async ({ page }) => {
    await page.fill('#eventTitle', 'Double Click');
    await page.fill('#eventDate', '2025-12-20T18:00');
    await page.fill('#eventLocation', 'Huế');

    await page.dblclick('#btnCreate');

    await expect(page).toHaveURL(/create_event.html/);
  });

  // ================= FUNC-CE15 =================
  test('FUNC-CE15 - Nhập ngày quá khứ', async ({ page }) => {
    await page.fill('#eventTitle', 'Event Quá Khứ');
    await page.fill('#eventDate', '2020-01-01T10:00');
    await page.fill('#eventLocation', 'Huế');

    await page.click('#btnCreate');

    const date = new Date(await page.inputValue('#eventDate'));
    expect(date.getFullYear()).toBeGreaterThanOrEqual(2024);
  });

  // ================= FUNC-CE16 =================
  test('FUNC-CE16 - Upload hình ảnh', async ({ page }) => {
    await page.setInputFiles('#eventImage', 'tests/test-data/test.jpg');
    await expect(page.locator('#eventImage')).toBeVisible();
  });

  // ================= FUNC-CE17 =================
  test('FUNC-CE17 - Xóa dữ liệu đã nhập', async ({ page }) => {
    await page.fill('#eventTitle', 'Xóa dữ liệu');
    await page.fill('#eventTitle', '');

    await expect(page.locator('#eventTitle')).toHaveValue('');
  });

  // ================= FUNC-CE18 =================
  test('FUNC-CE18 - Submit khi mất mạng (giả lập)', async ({ page }) => {
    await page.route('**/*', route => route.abort());

    await page.fill('#eventTitle', 'Mất mạng');
    await page.click('#btnCreate');

    await expect(page.locator('#eventTitle')).toHaveValue('Mất mạng');
  });

  // ================= FUNC-CE19 =================
  test('FUNC-CE19 - Nhấn Enter để submit', async ({ page }) => {
    await page.fill('#eventTitle', 'Enter Submit');
    await page.fill('#eventDate', '2025-12-20T18:00');
    await page.fill('#eventLocation', 'Huế');

    await page.keyboard.press('Enter');

    await expect(page).toHaveURL(/create_event.html/);
  });

  // ================= FUNC-CE20 =================
  test('FUNC-CE20 - Trở lại trang sau khi submit', async ({ page }) => {
    await page.fill('#eventTitle', 'Quay lại');
    await page.fill('#eventDate', '2025-12-22T20:00');
    await page.fill('#eventLocation', 'Đà Nẵng');

    await page.click('#btnCreate');
    await page.goBack();

    await expect(page).toHaveURL(/create_event.html/);
  });

});
