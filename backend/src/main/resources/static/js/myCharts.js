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
    plugins:{
        legend: {display: false},
        title: {
          display: true,
          text: "Number of alerts per week",
          font: {weight: 'bold'}
        },
        scales:{
            y:{
                beginAtZero: true,
                title:{
                    display: true,
                    text: 'number of alerts'
                }
            }
        }
    }
  }
});