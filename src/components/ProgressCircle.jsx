import React from "react";

export default function ProgressCircle({ value, max, label, unit, color }) {
  const radius = 80;
  const stroke = 14;
  const normalizedRadius = radius - stroke / 2;
  const circumference = normalizedRadius * 2 * Math.PI;
  const percent = Math.max(0, Math.min(1, value / max));
  const strokeDashoffset = circumference * (1 - percent);
  const percentValue = Math.round(percent * 100);

  return (
    <div className="progress-circle">
      <svg height={radius * 2} width={radius * 2}>
        <circle
          stroke="#e5e7eb"
          fill="transparent"
          strokeWidth={stroke}
          r={normalizedRadius}
          cx={radius}
          cy={radius}
        />
        <circle
          stroke={color}
          fill="transparent"
          strokeWidth={stroke}
          strokeLinecap="round"
          strokeDasharray={circumference + ' ' + circumference}
          style={{ strokeDashoffset, transition: 'stroke-dashoffset 0.5s' }}
          r={normalizedRadius}
          cx={radius}
          cy={radius}
        />
      </svg>
      <div className="progress-circle-content">
        <div className="progress-circle-percent" style={{ fontSize: 26, fontWeight: 700, color: color, marginBottom: 8 }}>{percentValue}%</div>
        <div className="progress-circle-value" style={{ fontSize: 32, fontWeight: 800, color: '#22223b', marginBottom: 4 }}>
          {value} {unit}
        </div>
        <div className="progress-circle-label" style={{ fontSize: 14, color: '#888', marginBottom: 4 }}>
          Kalan: {value} / {max} {unit}
        </div>
      </div>
    </div>
  );
}
