import { Chart as ChartJS, ArcElement, Tooltip, Legend } from 'chart.js';
import { Doughnut } from 'react-chartjs-2';

ChartJS.register(ArcElement, Tooltip, Legend);

const DEFAULT_DISTRIBUTION = {
  DELIVERED: 45,
  IN_TRANSIT: 25,
  OUT_FOR_DELIVERY: 15,
  DELAYED: 10,
  CREATED: 5,
};

export default function StatusDistributionChart({ distribution = DEFAULT_DISTRIBUTION }) {
  const data = {
    labels: ['Delivered', 'In Transit', 'Out for Delivery', 'Delayed', 'Created'],
    datasets: [
      {
        data: [
          distribution.DELIVERED || 0,
          distribution.IN_TRANSIT || 0,
          distribution.OUT_FOR_DELIVERY || 0,
          distribution.DELAYED || 0,
          distribution.CREATED || 0,
        ],
        backgroundColor: [
          '#10b981', // Delivered (Emerald)
          '#3b82f6', // In Transit (Blue)
          '#f59e0b', // Out for Delivery (Amber)
          '#ef4444', // Delayed (Red)
          '#64748b', // Created (Slate)
        ],
        borderColor: '#1e293b',
        borderWidth: 3,
        hoverOffset: 6,
      },
    ],
  };

  const options = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: {
        position: 'bottom',
        labels: {
          color: '#9ca3af',
          font: { size: 10, weight: '600' },
          usePointStyle: true,
          padding: 14,
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
    cutout: '70%',
  };

  return (
    <div className="w-full h-full min-h-[240px] flex items-center justify-center">
      <Doughnut data={data} options={options} />
    </div>
  );
}
