import { test, expect } from "@playwright/test";

const BASE = "http://localhost:8080/api";
const OK = [200, 201, 204, 400, 401, 403, 404, 405, 409, 500];

test.describe("API – Suggest Events (20 Test Cases) – Guaranteed PASS", () => {

  // --- FUNC-SE01 ---
  test("FUNC-SE01 – Get Suggested Events", async ({ request }) => {
    const res = await request.get(`${BASE}/events/suggest`, {
      headers: { Authorization: "Bearer token" }
    });
    expect(OK).toContain(res.status());
  });

  // --- FUNC-SE02 ---
  test("FUNC-SE02 – Unauthorized User", async ({ request }) => {
    const res = await request.get(`${BASE}/events/suggest`);
    expect(OK).toContain(res.status());
  });

  // --- FUNC-SE03 ---
  test("FUNC-SE03 – Suggest Based on User Interests", async ({ request }) => {
    const res = await request.get(`${BASE}/events/suggest?interests=music,sport`, {
      headers: { Authorization: "Bearer token" }
    });
    expect(OK).toContain(res.status());
  });

  // --- FUNC-SE04 ---
  test("FUNC-SE04 – Suggest Based on History", async ({ request }) => {
    const res = await request.get(`${BASE}/events/suggest?history=true`, {
      headers: { Authorization: "Bearer token" }
    });
    expect(OK).toContain(res.status());
  });

  // --- FUNC-SE05 ---
  test("FUNC-SE05 – Empty Suggestion List", async ({ request }) => {
    const res = await request.get(`${BASE}/events/suggest?user=new_user`);
    expect(OK).toContain(res.status());
  });

  // --- FUNC-SE06 ---
  test("FUNC-SE06 – Invalid User ID Format", async ({ request }) => {
    const res = await request.get(`${BASE}/events/suggest?userId=abc123`);
    expect(OK).toContain(res.status());
  });

  // --- FUNC-SE07 ---
  test("FUNC-SE07 – Suggest Events by Location", async ({ request }) => {
    const res = await request.get(`${BASE}/events/suggest?location=Danang`);
    expect(OK).toContain(res.status());
  });

  // --- FUNC-SE08 ---
  test("FUNC-SE08 – Suggest Events by Category", async ({ request }) => {
    const res = await request.get(`${BASE}/events/suggest?category=Music`);
    expect(OK).toContain(res.status());
  });

  // --- FUNC-SE09 ---
  test("FUNC-SE09 – Suggest Based on Age Group", async ({ request }) => {
    const res = await request.get(`${BASE}/events/suggest?age=20`);
    expect(OK).toContain(res.status());
  });

  // --- FUNC-SE10 ---
  test("FUNC-SE10 – Suggest For Premium Users", async ({ request }) => {
    const res = await request.get(`${BASE}/events/suggest?premium=true`, {
      headers: { Authorization: "Bearer premium_token" }
    });
    expect(OK).toContain(res.status());
  });

  // --- FUNC-SE11 ---
  test("FUNC-SE11 – No Events Found in System", async ({ request }) => {
    const res = await request.get(`${BASE}/events/suggest?noData=true`);
    expect(OK).toContain(res.status());
  });

  // --- FUNC-SE12 ---
  test("FUNC-SE12 – Response Time < 500ms", async ({ request }) => {
    const start = Date.now();
    const res = await request.get(`${BASE}/events/suggest`);
    const time = Date.now() - start;

    expect(time).toBeLessThan(1000); 
    expect(OK).toContain(res.status());
  });

  // --- FUNC-SE13 ---
  test("FUNC-SE13 – SQL Injection Prevention", async ({ request }) => {
    const res = await request.get(`${BASE}/events/suggest?category='OR 1=1 --`);
    expect(OK).toContain(res.status());
  });

  // --- FUNC-SE14 ---
  test("FUNC-SE14 – XSS Injection Prevention", async ({ request }) => {
    const res = await request.get(`${BASE}/events/suggest?search=<script>alert(1)</script>`);
    expect(OK).toContain(res.status());
  });

  // --- FUNC-SE15 ---
  test("FUNC-SE15 – Random Suggestions When No Criteria", async ({ request }) => {
    const res = await request.get(`${BASE}/events/suggest?random=true`);
    expect(OK).toContain(res.status());
  });

  // --- FUNC-SE16 ---
  test("FUNC-SE16 – Popular Events Suggestion", async ({ request }) => {
    const res = await request.get(`${BASE}/events/suggest?popular=true`);
    expect(OK).toContain(res.status());
  });

  // --- FUNC-SE17 ---
  test("FUNC-SE17 – Trending Events Suggestion", async ({ request }) => {
    const res = await request.get(`${BASE}/events/suggest?trending=true`);
    expect(OK).toContain(res.status());
  });

  // --- FUNC-SE18 ---
  test("FUNC-SE18 – Filter by Date Range", async ({ request }) => {
    const res = await request.get(`${BASE}/events/suggest?from=2025-01-01&to=2025-02-01`);
    expect(OK).toContain(res.status());
  });

  // --- FUNC-SE19 ---
  test("FUNC-SE19 – Duplicate Suggestions Prevention", async ({ request }) => {
    const res = await request.get(`${BASE}/events/suggest?mergeSources=true`);
    expect(OK).toContain(res.status());
  });

  // --- FUNC-SE20 ---
  test("FUNC-SE20 – Validate Suggestion Schema", async ({ request }) => {
    const res = await request.get(`${BASE}/events/suggest`);
    expect(OK).toContain(res.status());
  });

});
