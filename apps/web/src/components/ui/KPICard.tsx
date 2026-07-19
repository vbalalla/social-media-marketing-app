import React from 'react';

interface KPICardProps {
  title: string;
  value: string | number;
  change?: string | number;
  trend?: 'up' | 'down' | 'neutral';
}

export const KPICard: React.FC<KPICardProps> = ({
  title,
  value,
  change,
  trend = 'neutral'
}) => {
  return (
    <div className="kpi-card glass-card">
      <span className="kpi-title">{title}</span>
      <span className="kpi-value">{value}</span>
      {change && (
        <span className={`kpi-trend ${trend === 'up' ? 'trend-up' : trend === 'down' ? 'trend-down' : 'text-muted'}`}>
          {trend === 'up' ? '▲' : trend === 'down' ? '▼' : ''} {change}
        </span>
      )}
    </div>
  );
};
