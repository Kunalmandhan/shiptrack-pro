import {
  Chart as ChartJS,
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  Title,
  Tooltip,
  Legend,
  Filler,
} from 'chart.js';
import { Line } from 'react-chartjs-2';

// Register Chart.js modules
ChartJS.register(
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  Title,
  Tooltip,
  Legend,
  Filler
);

const MOCK_DATA_SERIES = [
  { label: 'Jul 13', total: 14, delivered: 11 },
  { label: 'Jul 15', total: 18, delivered: 15 },
  { label: 'Jul 17', total: 22, delivered: 18 },
  { label: 'Jul 19', total: 19, delivered: 16 },
  { label: 'Jul 21', total: 25, delivered: 21 },
  { label: 'Jul 23', total: 30, delivered: 26 },
  { label: 'Jul 25', total: 28, delivered: 24 },
  { label: 'Jul 26', total: 32, delivered: 27 },
];

export default function ShipmentVolumeChart({ dataSeries = MOCK_DATA_SERIES }) {
  const labels = dataSeries.map((d) => d.label);
  const totalValues = dataSeries.map((d) => d.total);
  const deliveredValues = dataSeries.map((d) => d.delivered);

  const data = {
    labels,
    datasets: [
      {
        label: 'Total Volume',
        data: totalValues,
        borderColor: '#6366f1',
        backgroundColor: 'rgba(99, 102, 241, 0.15)',
        borderWidth: 3,
        tension: 0.4,
        fill: true,
        pointBackgroundColor: '#6366f1',
        pointRadius: 4,
      },
      {
        label: 'Delivered',
        data: deliveredValues,
        borderColor: '#10b981',
        backgroundColor: 'rgba(16, 185, 129, 0.1)',
        borderWidth: 3,
        tension: 0.4,
        fill: true,
        pointBackgroundColor: '#10b981',
        pointRadius: 4,
      },
    ],
  };

  const options = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: {
        position: 'top',
        align: 'end',
        labels: {
          color: '#9ca3af',
          font: { size: 11, weight: '600' },
          usePointStyle: true,
          boxWidth: 8,
        },
      },
      tooltip: {
        backgroundColor: '#1e293b',
        titleColor: '#ffffff',
        bodyColor: '#cbd5e1',
        borderColor: 'rgba(255, 255, 255, 0.1)',
        borderWidth: 1,
        padding: 10,
        cornerRadius: 12,
      },
    },
    scales: {
      x: {
        grid: { color: 'rgba(255, 255, 255, 0.05)' },
        ticks: { color: '#9ca3af', font: { size: 10 } },
      },
      y: {
        grid: { color: 'rgba(255, 255, 255, 0.05)' },
        ticks: { color: '#9ca3af', font: { size: 10 } },
        beginAtZero: true,
      },
    },
  };

  return (
    <div className="w-full h-full min-h-[260px]">
      <Line data={data} options={options} />
    </div>
  );
}
