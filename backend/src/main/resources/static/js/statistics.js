document.addEventListener("DOMContentLoaded", () => {
    if (typeof chartData === "undefined") return;

    const tooltipTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="tooltip"]'));
    tooltipTriggerList.map(function (tooltipTriggerEl) {
        return new bootstrap.Tooltip(tooltipTriggerEl);
    });
    
    const {
        dailyRaw, hourlyRaw, tagCounts, downtimeHistogram,
        weekdayCounts, locationCounts, tagTrends,
        fromDate, toDate, rawChannelCounts
    } = chartData;

    // === Daily Events Data ===
    const start = new Date(fromDate);
    const end = new Date(toDate);
    const fullDateMap = new Map();
    for (let d = new Date(start); d <= end; d.setDate(d.getDate() + 1)) {
        const isoDate = d.toISOString().split("T")[0];
        fullDateMap.set(isoDate, 0);
    }
    dailyRaw.forEach(({ date, count }) => fullDateMap.set(date, count));
    const dailyData = Array.from(fullDateMap.entries()).map(([date, count]) => ({ date, count }));

    const hourlyData = Array.from({ length: 24 }, (_, h) => {
        const item = hourlyRaw.find(e => e.hour === h);
        return { hour: h, count: item ? item.count : 0 };
    });

    const downtimeOrder = [
        "0–10 min",
        "10–30 min",
        "30–60 min",
        "1–2 h",
        "2–4 h",
        "4–8 h",
        "8+ h"
    ];

    const sortedHistogram = downtimeOrder.map(label => {
        const entry = downtimeHistogram.find(b => b.rangeLabel === label);
        return {
            label,
            count: entry ? entry.count : 0
        };
    });
    

    renderBarChart("dailyEventsChart", dailyData.map(d => d.date), dailyData.map(d => d.count), "Events per Day", "rgba(54, 162, 235, 0.5)");
    renderLineChart("hourlyEventsChart", hourlyData.map(h => h.hour), hourlyData.map(h => h.count), "Events per Hour", "rgba(220, 53, 69, 1)");

    const tagTop = tagCounts.slice(0, 10);
    renderPieChart("tagChart", tagTop.map(t => t.tag), tagTop.map(t => t.count), [
        '#dc3545', '#8bc34a', '#4dd0e1', '#9575cd', '#fbc02d'
    ]);

    renderBarChart("weekdayChart", weekdayCounts.map(w => w.weekday), weekdayCounts.map(w => w.count), "Events", "rgba(255, 159, 64, 0.6)");

    renderBarChart(
        "downtimeChart",
        sortedHistogram.map(b => b.label),
        sortedHistogram.map(b => b.count),
        "Event Count",
        "rgba(54, 162, 235, 0.7)",
        {
            x: { title: { display: true, text: "Downtime Duration" } },
            y: { title: { display: true, text: "Number of Events" }, beginAtZero: true }
        }
    );
    

    const topLocations = locationCounts.slice(0, 5);
    renderPieChart("locationPieChart", topLocations.map(l => l.location), topLocations.map(l => l.count), [
        '#ff6384', '#36a2eb', '#ffce56', '#4bc0c0', '#9966ff'
    ]);

    const tagTrendsCanvas = document.getElementById('tagTrendsChart');
    if (tagTrendsCanvas) {
        if (!Array.isArray(tagTrends) || tagTrends.length === 0) {
            // Render a visible empty placeholder chart
            new Chart(tagTrendsCanvas, {
                type: 'line',
                data: {
                    labels: ['No data'],
                    datasets: [{
                        label: 'No Tag Data',
                        data: [0],
                        borderColor: '#ccc',
                        borderDash: [5, 5],
                        pointRadius: 0,
                        tension: 0.3
                    }]
                },
                options: {
                    responsive: true,
                    scales: {
                        x: {
                            title: {
                                display: true,
                                text: 'Date'
                            }
                        },
                        y: {
                            beginAtZero: true,
                            title: {
                                display: true,
                                text: 'Event Count'
                            }
                        }
                    },
                    plugins: {
                        legend: { display: true },
                        tooltip: { enabled: false }
                    }
                }
            });
        } else {
            const groupedTrends = {};
            tagTrends.forEach(({ date, tag, count }) => {
                if (!groupedTrends[tag]) groupedTrends[tag] = {};
                groupedTrends[tag][date] = count;
            });

            const allTrendDates = [...new Set(tagTrends.map(t => t.date))].sort();
            const trendDatasets = Object.entries(groupedTrends).map(([tag, values]) => ({
                label: tag,
                data: allTrendDates.map(date => values[date] || 0),
                fill: false,
                tension: 0.3
            }));

            new Chart(tagTrendsCanvas, {
                type: 'line',
                data: {
                    labels: allTrendDates,
                    datasets: trendDatasets
                },
                options: {
                    responsive: true,
                    plugins: {
                        legend: { position: 'top' }
                    },
                    scales: {
                        x: {
                            title: {
                                display: true,
                                text: 'Date'
                            }
                        },
                        y: {
                            beginAtZero: true,
                            title: {
                                display: true,
                                text: 'Event Count'
                            }
                        }
                    }
                }
            });
        }
    }


    if (rawChannelCounts.length > 0) {
        renderPieChart("channelChart", rawChannelCounts.map(c => c.type), rawChannelCounts.map(c => c.count), [
            '#0d6efd', '#dc3545', '#ffc107'
        ]);
    }

    [
        'silentDevicesList',
        'topDevicesTable',
        'tagTable',
        'locationTable',
        'channelTable',
        'mostNotifiedTable',
        'avgRestoreTable',
        'stableDevicesTable',
        'recoveryTable'
      ].forEach(id => setupShowMoreCollapse(id));
      
    

    document.querySelector("form")?.addEventListener("submit", function () {
        console.log("Submitting from:", document.getElementById("from").value);
        console.log("Submitting to:", document.getElementById("to").value);
    });
});

function renderBarChart(id, labels, data, label, color, customScales = {}) {
    new Chart(document.getElementById(id), {
        type: 'bar',
        data: {
            labels: labels,
            datasets: [{
                label: label,
                data: data,
                backgroundColor: color
            }]
        },
        options: {
            responsive: true,
            scales: {
                y: {
                    beginAtZero: true,
                    ...customScales.y
                },
                x: {
                    ...customScales.x
                }
            }
        }
    });
}

function renderLineChart(id, labels, data, label, borderColor) {
    new Chart(document.getElementById(id), {
        type: 'line',
        data: {
            labels: labels,
            datasets: [{
                label: label,
                data: data,
                borderColor: borderColor,
                fill: false,
                tension: 0.3
            }]
        },
        options: {
            responsive: true
        }
    });
}

function renderPieChart(id, labels, data, colors) {
    new Chart(document.getElementById(id), {
        type: 'pie',
        data: {
            labels: labels,
            datasets: [{
                data: data,
                backgroundColor: colors,
                borderWidth: 1
            }]
        },
        options: {
            responsive: true,
            plugins: {
                legend: {
                    position: 'right',
                    labels: {
                        boxWidth: 20,
                        padding: 15,
                        font: { size: 14 }
                    }
                }
            },
            layout: {
                padding: 10
            }
        }
    });
}

function exportDashboardAsImage() {
    const dashboard = document.getElementById("dashboard");
    html2canvas(dashboard).then(canvas => {
      const link = document.createElement('a');
      link.download = 'dashboard.png';
      link.href = canvas.toDataURL();
      link.click();
    });
  }
  
  async function exportDashboardAsPDF() {
    const dashboard = document.getElementById("dashboard");
    const canvas = await html2canvas(dashboard);
    const imgData = canvas.toDataURL("image/png");
  
    const { jsPDF } = window.jspdf;
    const pdf = new jsPDF({
      orientation: "portrait",
      unit: "px",
      format: [canvas.width, canvas.height]
    });
  
    pdf.addImage(imgData, "PNG", 0, 0, canvas.width, canvas.height);
    pdf.save("dashboard.pdf");
  }
  

  



function setupShowMoreCollapse(containerId) {
    const container = document.getElementById(containerId);
    if (!container) return;

    const hiddenRows = container.querySelectorAll('.extra-row.d-none');
    if (hiddenRows.length === 0) return;

    const cardBody = container.closest('.card-body') || container.parentElement;
    const controls = document.createElement('div');
    controls.className = 'text-center mt-3';

    const showMoreBtn = document.createElement('button');
    showMoreBtn.className = 'btn btn-sm btn-outline-secondary';
    showMoreBtn.textContent = 'Show More';

    const collapseBtn = document.createElement('button');
    collapseBtn.className = 'btn btn-sm btn-outline-danger ms-2 d-none';
    collapseBtn.textContent = 'Collapse All';

    controls.appendChild(showMoreBtn);
    controls.appendChild(collapseBtn);
    cardBody.appendChild(controls);

    showMoreBtn.addEventListener('click', async () => {
        showMoreBtn.disabled = true;
        showMoreBtn.innerHTML = `<span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span> Loading...`;
        await new Promise(resolve => setTimeout(resolve, 300)); // simulate slight loading delay

        const hiddenRows = container.querySelectorAll('.extra-row.d-none');
        for (let i = 0; i < 5 && i < hiddenRows.length; i++) {
            hiddenRows[i].classList.remove('d-none');
            hiddenRows[i].style.opacity = 0;
            hiddenRows[i].style.transition = 'opacity 0.5s';
            requestAnimationFrame(() => hiddenRows[i].style.opacity = 1);
        }

        const stillHidden = container.querySelectorAll('.extra-row.d-none').length;
        if (stillHidden > 0) {
            showMoreBtn.disabled = false;
            showMoreBtn.textContent = 'Show More';
        } else {
            showMoreBtn.disabled = true;
            showMoreBtn.textContent = 'No more items';
        }
        collapseBtn.classList.remove('d-none');
    });

    collapseBtn.addEventListener('click', () => {
        container.querySelectorAll('.extra-row').forEach(row => {
            row.classList.add('d-none');
            row.style.opacity = '';
            row.style.transition = '';
        });
        showMoreBtn.disabled = false;
        showMoreBtn.textContent = 'Show More';
        collapseBtn.classList.add('d-none');
    });
}

  


