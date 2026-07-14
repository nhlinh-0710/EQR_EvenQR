import { defineConfig } from "@playwright/test";

export default defineConfig({
  testDir: "./tests",
  timeout: 60000, // Tăng timeout lên 60 giây
  retries: 0,
  use: {
    baseURL: "http://localhost:8080/api",
    headless: true,
    // Tăng timeout cho mỗi action
    actionTimeout: 30000,
  },
  // Cấu hình cho API tests
  expect: {
    timeout: 10000, // Timeout cho assertions
  },
  projects: [
    {
      name: "api",
      testMatch: /.*api\.spec\.js/,
    },
    {
      name: "ui",
      testMatch: /.*ui\.spec\.js/,
    },
  ]
});
