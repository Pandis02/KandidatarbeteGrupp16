var xValues = ["Mon", "Tue", "Wed", "Thu", "Sat", "Sun"];
var yValues = [1,0, 2, 1, 0, 5];
var color = 'rgba(54, 87, 235, 0.47)';

new Chart("alertChart", {
  type: "bar",
  data: {
    labels: xValues,
    datasets: [{
      backgroundColor: color,
      data: yValues,
      borderRadius: 5
    }]
  },
  options: {
    responsive: true,
    plugins: {
      legend: { display: false },
      title: {
        display: true,
        text: "Number of alerts per week",
        font: { weight: 'bold' }
      }
    },
    scales: {
      y: {
        beginAtZero: true,
        title: {
          display: true,
        },
        ticks: {
          stepSize: 1 
        }
      }
    }
  }
});



/* LINE CHART */
var xTime = ["00:00", "04:00", "08:00", "12:00", "16:00", "20:00", "23:59"];

const ctx = document.getElementById('notificationChart').getContext('2d');
const notificationChart = new Chart(ctx, {
    type: 'line',
    data: {
        labels: xTime, 
        datasets: [{
            label: 'Number of Notifications',
            data: [5, 10, 15, 8, 12, 18, 7], 
            borderColor: 'blue',
            fill: false,
            tension: 0.3
        }]
    },
    options: {
        responsive: true,
    }
});