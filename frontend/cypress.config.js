const { defineConfig } = require("cypress");

module.exports = defineConfig({
  e2e: {
    // Không set baseUrl để Cypress có thể mở file HTML trực tiếp từ thư mục frontend
    // Nếu có server chạy (Live Server tại http://localhost:5500), 
    // có thể uncomment dòng dưới và sử dụng baseUrl trong test files
    // baseUrl: "http://localhost:5500",
    viewportWidth: 1280,
    viewportHeight: 720,
    video: false,
    screenshotOnRunFailure: true,
    setupNodeEvents(on, config) {
      // implement node event listeners here
    },
  },
});
