# Hướng dẫn cài đặt WebSocket Libraries

Nếu Stomp.js không load được từ CDN, hãy tải về local:

## Cách 1: Tải thủ công

1. Tải file Stomp.js từ một trong các link sau:
   - https://cdn.jsdelivr.net/npm/@stomp/stompjs@7/bundles/stomp.umd.min.js
   - https://unpkg.com/@stomp/stompjs@7/bundles/stomp.umd.min.js

2. Lưu file vào: `frontend/js/libs/stomp.umd.min.js`

3. Cập nhật `feedback.html` để sử dụng local file:
   ```html
   <script src="../../js/libs/stomp.umd.min.js"></script>
   ```

## Cách 2: Sử dụng npm (nếu có)

```bash
cd frontend
npm install @stomp/stompjs@7
# File sẽ ở: node_modules/@stomp/stompjs/bundles/stomp.umd.min.js
```

Sau đó copy vào `frontend/js/libs/`

