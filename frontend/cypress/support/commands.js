// ***********************************************
// Custom commands for EventQR
// ***********************************************

// Helper command để visit file HTML bằng đường dẫn tương đối
// Cypress không hỗ trợ file:// protocol, nên dùng đường dẫn tương đối từ projectRoot
Cypress.Commands.add('visitFile', (filePath) => {
  // Dùng đường dẫn tương đối trực tiếp, Cypress sẽ resolve từ projectRoot
  // Đảm bảo đường dẫn không bắt đầu bằng /
  const relativePath = filePath.startsWith('/') ? filePath.slice(1) : filePath;
  cy.visit(relativePath, { failOnStatusCode: false });
});