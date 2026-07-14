console.log("load dashboard.js sucess");

// Check authentication on page load
document.addEventListener("DOMContentLoaded", function () {
    // Check auth first (will redirect if not logged in)
    const userData = initAuth();
    if (!userData) return; // Stop if redirected
    
    console.log('User authenticated:', userData);
    
    // Continue with dashboard initialization
    initializeDashboardChart();
});

function initializeDashboardChart() {
    const chartElement = document.getElementById('areaChart');
    if (!chartElement) return; // Chart not on this page
    
    const ctx = chartElement.getContext('2d');
    new Chart(ctx, {
        type: 'line',
        data: {
            labels: ['Tháng 1', 'Tháng 2', 'Tháng 3', 'Tháng 4', 'Tháng 5', 'Tháng 6', 'Tháng 7', 'Tháng 8'],
            datasets: [{
                label: 'Người tham dự',
                data: [80, 10, 50, 70, 80, 70, 56, 140,],
                fill: true,
                backgroundColor: 'rgba(54, 162, 235, 0.3)',
                borderColor: 'rgba(54, 162, 235, 1)',
                borderWidth: 2,
                tension: 0.4,
                pointBackgroundColor: 'white',
                pointBorderColor: 'rgba(54, 162, 235, 1)',
                pointRadius: 5,
                pointHoverRadius: 7
            }]
        },
        options: {
            responsive: true,
            plugins: {
                legend: {
                    labels: {
                        font: {
                            family: 'Poppins'
                        }
                    }
                }
            },
            scales: {
                y: {
                    beginAtZero: true,
                    ticks: {
                        stepSize: 50
                    }
                }
            }
        }
    });
}
