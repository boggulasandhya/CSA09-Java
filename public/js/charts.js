// public/js/charts.js
// Chart.js visualizations for Security Executive Dashboard

let severityChartInstance = null;
let owaspChartInstance = null;
let radarChartInstance = null;

function initDashboardCharts() {
  const isDark = true;
  const textColor = '#9CA3AF';
  const gridColor = '#1F2937';

  // 1. Severity Distribution Donut Chart
  const ctxSeverity = document.getElementById('severityChart');
  if (ctxSeverity) {
    if (severityChartInstance) severityChartInstance.destroy();
    severityChartInstance = new Chart(ctxSeverity, {
      type: 'doughnut',
      data: {
        labels: ['Critical (P1)', 'High (P2)', 'Medium (P3)', 'Low (P4)'],
        datasets: [{
          data: [3, 4, 1, 0],
          backgroundColor: [
            '#F43F5E', // Rose
            '#FB923C', // Orange
            '#FBBF24', // Amber
            '#10B981'  // Emerald
          ],
          borderColor: '#111827',
          borderWidth: 3,
          hoverOffset: 6
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: {
            position: 'bottom',
            labels: { color: textColor, font: { family: 'Inter', size: 12 } }
          }
        },
        cutout: '72%'
      }
    });
  }

  // 2. OWASP Top 10 Breakdown Bar Chart
  const ctxOwasp = document.getElementById('owaspChart');
  if (ctxOwasp) {
    if (owaspChartInstance) owaspChartInstance.destroy();
    owaspChartInstance = new Chart(ctxOwasp, {
      type: 'bar',
      data: {
        labels: ['A01:Access Ctrl', 'A02:Crypto', 'A03:Injection', 'A05:Misconfig', 'A07:Auth', 'A10:SSRF'],
        datasets: [{
          label: 'Identified Vulnerabilities',
          data: [3, 1, 4, 2, 1, 1],
          backgroundColor: '#6366F1',
          borderRadius: 6
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        scales: {
          x: { grid: { color: 'transparent' }, ticks: { color: textColor, font: { size: 11 } } },
          y: { grid: { color: gridColor }, ticks: { color: textColor, stepSize: 1 } }
        },
        plugins: {
          legend: { display: false }
        }
      }
    });
  }

  // 3. Attack Surface Defense Radar Chart
  const ctxRadar = document.getElementById('radarChart');
  if (ctxRadar) {
    if (radarChartInstance) radarChartInstance.destroy();
    radarChartInstance = new Chart(ctxRadar, {
      type: 'radar',
      data: {
        labels: ['Endpoint Exposure', 'Access Control', 'Input Sanitization', 'Header Hardening', 'Data Protection'],
        datasets: [
          {
            label: 'Benchmark Target Score',
            data: [35, 42, 30, 25, 40],
            backgroundColor: 'rgba(244, 63, 94, 0.2)',
            borderColor: '#F43F5E',
            pointBackgroundColor: '#F43F5E'
          },
          {
            label: 'Hardened Target Standard',
            data: [90, 95, 92, 88, 96],
            backgroundColor: 'rgba(16, 185, 129, 0.2)',
            borderColor: '#10B981',
            pointBackgroundColor: '#10B981'
          }
        ]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        scales: {
          r: {
            grid: { color: gridColor },
            angleLines: { color: gridColor },
            pointLabels: { color: textColor, font: { size: 10 } },
            ticks: { display: false, max: 100 }
          }
        },
        plugins: {
          legend: {
            position: 'bottom',
            labels: { color: textColor, font: { size: 11 } }
          }
        }
      }
    });
  }
}

function updateChartsWithResults(prioritizedList) {
  if (!severityChartInstance || !prioritizedList) return;

  const counts = {
    P1: prioritizedList.filter(f => f.priorityTier.startsWith('P1')).length,
    P2: prioritizedList.filter(f => f.priorityTier.startsWith('P2')).length,
    P3: prioritizedList.filter(f => f.priorityTier.startsWith('P3')).length,
    P4: prioritizedList.filter(f => f.priorityTier.startsWith('P4')).length
  };

  severityChartInstance.data.datasets[0].data = [counts.P1, counts.P2, counts.P3, counts.P4];
  severityChartInstance.update();
}
