// ==========================================
// STATISTICS PAGE LOGIC
// ==========================================

// Tránh duplicate declaration
if (typeof window.API_BASE_URL === 'undefined') {
    window.API_BASE_URL = 'http://localhost:8080/api';
}
// Dùng window.API_BASE_URL trực tiếp hoặc kiểm tra trước khi khai báo
const STATS_API_BASE_URL = window.API_BASE_URL;

let timeSeriesChartInstance = null;
let eventStatsChartInstance = null;

// ==========================================
// INITIALIZATION
// ==========================================

document.addEventListener('DOMContentLoaded', async () => {
    console.log('📊 Statistics page loaded');
    
    // Kiểm tra đăng nhập
    const currentUser = JSON.parse(localStorage.getItem('currentUser'));
    console.log('👤 Current user:', currentUser);
    
    // Update user name in header
    if (currentUser) {
        const userNameElement = document.getElementById('userName');
        if (userNameElement) {
            userNameElement.textContent = currentUser.name || 'Người dùng';
        }
    }
    
    if (!currentUser) {
        console.error('❌ Chưa đăng nhập');
        alert('Vui lòng đăng nhập để xem thống kê');
        window.location.href = '../../index.html';
        return;
    }

    // Lấy organizerId (có thể là user_id hoặc userId)
    const organizerId = currentUser.user_id || currentUser.userId || currentUser.id;
    console.log('🔑 Organizer ID:', organizerId);
    
    if (!organizerId) {
        console.error('❌ Không tìm thấy organizerId');
        alert('Không tìm thấy thông tin tài khoản. Vui lòng đăng nhập lại.');
        window.location.href = '../../index.html';
        return;
    }

    // Load statistics
    await loadStatistics(organizerId);
});

// ==========================================
// LOAD STATISTICS
// ==========================================

// LINH
async function loadStatistics(organizerId) {
    try {
        showLoading();
        
        console.log('🔍 Fetching statistics for organizer:', organizerId);
        console.log('🌐 API URL:', `${STATS_API_BASE_URL}/statistics/organizer/${organizerId}`);
        
        const response = await fetch(`${STATS_API_BASE_URL}/statistics/organizer/${organizerId}`);
        
        console.log('📡 Response status:', response.status);
        console.log('📡 Response ok:', response.ok);
        
        if (!response.ok) {
            const errorText = await response.text();
            console.error('❌ Error response:', errorText);
            
            if (response.status === 404) {
                throw new Error('API endpoint không tồn tại. Vui lòng kiểm tra backend.');
            } else if (response.status === 500) {
                throw new Error('Lỗi server. Vui lòng kiểm tra backend logs.');
            } else if (response.status === 0) {
                throw new Error('Không thể kết nối tới server. Vui lòng kiểm tra backend có đang chạy không.');
            } else {
                throw new Error(`HTTP error! status: ${response.status} - ${errorText}`);
            }
        }
        
        const data = await response.json();
        console.log('📊 Statistics data:', data);
        
        // Kiểm tra nếu không có dữ liệu
        if (data.totalEvents === 0) {
            showEmptyState();
            return;
        }
        
        // Hiển thị dữ liệu
        displayOverviewStats(data);
        displayMainChart(data.timeSeriesStats);
        displayEventTable(data.eventStats);
        
        // Show sections
        document.getElementById('main-chart-section').style.display = 'block';
        
        hideLoading();
        
    } catch (error) {
        console.error('❌ Error loading statistics:', error);
        hideLoading();
        
        // Hiển thị lỗi chi tiết
        let errorMessage = 'Không thể tải thống kê.\n\n';
        
        if (error.message.includes('Failed to fetch') || error.message.includes('NetworkError')) {
            errorMessage += '❌ Lỗi kết nối:\n';
            errorMessage += '- Backend chưa chạy (port 8080)\n';
            errorMessage += '- Hoặc có vấn đề về CORS\n\n';
            errorMessage += 'Hãy chạy backend: mvn spring-boot:run';
        } else {
            errorMessage += error.message;
        }
        
        showError(errorMessage);
    }
}
// LINH

// ==========================================
// DISPLAY OVERVIEW STATS
// ==========================================

// Store original data for filtering
let originalData = null;

function displayOverviewStats(data) {
    // Store data for filtering
    originalData = data;
    
    // Set default date range (last 6 months)
    const endDate = new Date();
    const startDate = new Date();
    startDate.setMonth(startDate.getMonth() - 6);
    
    document.getElementById('endDate').valueAsDate = endDate;
    document.getElementById('startDate').valueAsDate = startDate;
}

// ==========================================
// DISPLAY MAIN CHART
// ==========================================

let mainChartInstance = null;

function displayMainChart(timeSeriesData) {
    const ctx = document.getElementById('mainChart').getContext('2d');
    
    // Destroy existing chart if exists
    if (mainChartInstance) {
        mainChartInstance.destroy();
    }
    
    // Format labels (2024-01 -> Tháng 1)
    const labels = timeSeriesData.map(item => {
        const [year, month] = item.period.split('-');
        return `Tháng ${parseInt(month)}`;
    });
    
    const values = timeSeriesData.map(item => item.registrationCount);
    
    mainChartInstance = new Chart(ctx, {
        type: 'line',
        data: {
            labels: labels,
            datasets: [{
                label: 'Người tham dự',
                data: values,
                fill: true,
                backgroundColor: function(context) {
                    const ctx = context.chart.ctx;
                    const gradient = ctx.createLinearGradient(0, 0, 0, 400);
                    gradient.addColorStop(0, 'rgba(66, 153, 225, 0.4)');
                    gradient.addColorStop(1, 'rgba(66, 153, 225, 0.05)');
                    return gradient;
                },
                borderColor: '#4299e1',
                borderWidth: 3,
                tension: 0.4,
                pointBackgroundColor: 'white',
                pointBorderColor: '#4299e1',
                pointRadius: 6,
                pointHoverRadius: 8,
                pointBorderWidth: 3,
                pointHoverBorderWidth: 3
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: {
                    display: true,
                    position: 'top',
                    align: 'end',
                    labels: {
                        font: {
                            family: 'Poppins',
                            size: 13
                        },
                        padding: 15,
                        usePointStyle: true,
                        pointStyle: 'rect'
                    }
                },
                tooltip: {
                    backgroundColor: 'rgba(0, 0, 0, 0.8)',
                    padding: 12,
                    titleFont: {
                        family: 'Poppins',
                        size: 14
                    },
                    bodyFont: {
                        family: 'Poppins',
                        size: 13
                    },
                    callbacks: {
                        label: function(context) {
                            return `Người tham dự: ${context.parsed.y}`;
                        }
                    }
                }
            },
            scales: {
                y: {
                    beginAtZero: true,
                    ticks: {
                        stepSize: 20,
                        font: {
                            family: 'Poppins',
                            size: 12
                        },
                        color: '#718096'
                    },
                    grid: {
                        color: '#e2e8f0',
                        drawBorder: false
                    }
                },
                x: {
                    ticks: {
                        font: {
                            family: 'Poppins',
                            size: 12
                        },
                        color: '#718096'
                    },
                    grid: {
                        display: false,
                        drawBorder: false
                    }
                }
            }
        }
    });
}

// ==========================================
// DATE FILTER
// ==========================================

function applyDateFilter() {
    if (!originalData) return;
    
    const startDate = document.getElementById('startDate').value;
    const endDate = document.getElementById('endDate').value;
    
    if (!startDate || !endDate) {
        alert('Vui lòng chọn cả ngày bắt đầu và ngày kết thúc');
        return;
    }
    
    // Filter time series data
    const filteredData = originalData.timeSeriesStats.filter(item => {
        const itemDate = item.period + '-01'; // Convert "2024-01" to "2024-01-01"
        return itemDate >= startDate && itemDate <= endDate;
    });
    
    // Update chart
    displayMainChart(filteredData);
}

// ==========================================
// DISPLAY EVENT TABLE
// ==========================================

function displayEventTable(eventStats) {
    const tbody = document.getElementById('event-stats-tbody');
    tbody.innerHTML = '';
    
    if (!eventStats || eventStats.length === 0) {
        tbody.innerHTML = '<tr><td colspan="4" style="text-align: center; color: #999;">Không có dữ liệu</td></tr>';
        return;
    }
    
    // Sort by registration count descending
    const sortedEvents = [...eventStats].sort((a, b) => b.registrationCount - a.registrationCount);
    
    sortedEvents.forEach(event => {
        const row = document.createElement('tr');
        
        // Xác định class cho status
        let statusClass = 'draft';
        const status = (event.eventStatus || '').toLowerCase();
        if (status.includes('active') || status.includes('published')) statusClass = 'active';
        else if (status.includes('completed') || status.includes('ended')) statusClass = 'completed';
        else if (status.includes('cancelled')) statusClass = 'cancelled';
        
        row.innerHTML = `
            <td class="event-title">${escapeHtml(event.eventTitle)}</td>
            <td>${event.eventDate || 'N/A'}</td>
            <td><span class="status-badge ${statusClass}">${event.eventStatus || 'N/A'}</span></td>
            <td><strong>${event.registrationCount}</strong> người</td>
        `;
        
        tbody.appendChild(row);
    });
    
    document.getElementById('event-table-section').style.display = 'block';
}

// ==========================================
// UTILITY FUNCTIONS
// ==========================================

function showLoading() {
    document.getElementById('loading-state').style.display = 'block';
    document.getElementById('main-chart-section').style.display = 'none';
    document.getElementById('event-table-section').style.display = 'none';
    document.getElementById('empty-state').style.display = 'none';
}

function hideLoading() {
    document.getElementById('loading-state').style.display = 'none';
}

function showEmptyState() {
    document.getElementById('loading-state').style.display = 'none';
    document.getElementById('main-chart-section').style.display = 'none';
    document.getElementById('event-table-section').style.display = 'none';
    document.getElementById('empty-state').style.display = 'block';
}

function showError(message) {
    // Tạo error display thay vì alert
    const container = document.querySelector('.statistics-container');
    container.innerHTML = `
        <div style="
            text-align: center;
            padding: 3rem;
            background: white;
            border-radius: 12px;
            box-shadow: 0 2px 10px rgba(0, 0, 0, 0.08);
            max-width: 600px;
            margin: 2rem auto;
        ">
            <i class="fas fa-exclamation-triangle" style="
                font-size: 4rem;
                color: #f56565;
                margin-bottom: 1.5rem;
            "></i>
            <h3 style="
                font-size: 1.5rem;
                color: #2d3748;
                margin-bottom: 1rem;
            ">Lỗi Tải Thống Kê</h3>
            <p style="
                color: #718096;
                white-space: pre-wrap;
                line-height: 1.6;
                margin-bottom: 2rem;
            ">${message}</p>
            <button onclick="location.reload()" style="
                padding: 0.75rem 1.5rem;
                background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                color: white;
                border: none;
                border-radius: 8px;
                font-weight: 600;
                cursor: pointer;
                font-size: 1rem;
            ">
                <i class="fas fa-redo"></i> Thử Lại
            </button>
        </div>
    `;
}

function escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

console.log('✅ Statistics script loaded');

