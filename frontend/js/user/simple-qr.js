// Simple QR Code Generator (without external libraries)
class SimpleQRCode {
    static generateTextQR(text, size = 200) {
        // Tạo QR code dạng text pattern đơn giản
        const container = document.createElement('div');
        container.style.cssText = `
            width: ${size}px;
            height: ${size}px;
            background: white;
            border: 2px solid #6366f1;
            border-radius: 8px;
            display: flex;
            align-items: center;
            justify-content: center;
            flex-direction: column;
            font-family: monospace;
            font-size: 12px;
            text-align: center;
            padding: 10px;
            box-sizing: border-box;
        `;
        
        // Tạo pattern QR đơn giản
        const qrPattern = document.createElement('div');
        qrPattern.style.cssText = `
            width: 100px;
            height: 100px;
            background: 
                linear-gradient(90deg, #6366f1 10px, transparent 10px),
                linear-gradient(#6366f1 10px, transparent 10px),
                linear-gradient(45deg, #6366f1 25%, transparent 25%);
            background-size: 20px 20px, 20px 20px, 20px 20px;
            border: 3px solid #6366f1;
            margin-bottom: 10px;
        `;
        
        const textDiv = document.createElement('div');
        textDiv.textContent = `QR: ${text.substring(0, 20)}${text.length > 20 ? '...' : ''}`;
        textDiv.style.color = '#6366f1';
        textDiv.style.fontWeight = 'bold';
        
        container.appendChild(qrPattern);
        container.appendChild(textDiv);
        
        return container;
    }
    
    static generateAPIQR(text, size = 200) {
        // Sử dụng API QR code
        const img = document.createElement('img');
        img.src = `https://api.qrserver.com/v1/create-qr-code/?size=${size}x${size}&data=${encodeURIComponent(text)}`;
        img.style.cssText = `
            width: ${size}px;
            height: ${size}px;
            border: 2px solid #6366f1;
            border-radius: 8px;
        `;
        
        img.onerror = function() {
            // Nếu API fail, thay thế bằng text QR
            const parent = this.parentNode;
            const textQR = SimpleQRCode.generateTextQR(text, size);
            parent.replaceChild(textQR, this);
        };
        
        return img;
    }
    
    static generate(text, container, options = {}) {
        const size = options.size || 200;
        const useAPI = options.useAPI !== false; // Default true
        
        // Clear container
        container.innerHTML = '';
        
        let qrElement;
        if (useAPI) {
            qrElement = this.generateAPIQR(text, size);
        } else {
            qrElement = this.generateTextQR(text, size);
        }
        
        container.appendChild(qrElement);
        
        // Thêm thông tin bổ sung
        const info = document.createElement('div');
        info.style.cssText = `
            margin-top: 10px;
            font-size: 12px;
            color: #6b7280;
            text-align: center;
        `;
        info.innerHTML = `
            <div>Data: ${text.length} ký tự</div>
            <div>Tạo lúc: ${new Date().toLocaleTimeString('vi-VN')}</div>
        `;
        
        container.appendChild(info);
        
        return qrElement;
    }
}

// Export cho sử dụng global
window.SimpleQRCode = SimpleQRCode;
