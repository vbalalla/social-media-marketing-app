import React from 'react';
import { KPICard } from '../components/ui/KPICard';
import { 
  AreaChart, 
  Area, 
  XAxis, 
  YAxis, 
  CartesianGrid, 
  Tooltip as ChartTooltip, 
  ResponsiveContainer,
  BarChart,
  Bar,
  Legend
} from 'recharts';
import { useWorkspaces } from '../hooks/useWorkspaces';

const engagementData = [
  { name: 'Mon', organic: 4000, paid: 2400 },
  { name: 'Tue', organic: 3000, paid: 1398 },
  { name: 'Wed', organic: 2000, paid: 9800 },
  { name: 'Thu', organic: 2780, paid: 3908 },
  { name: 'Fri', organic: 1890, paid: 4800 },
  { name: 'Sat', organic: 2390, paid: 3800 },
  { name: 'Sun', organic: 3490, paid: 4300 },
];

export const DashboardPage: React.FC = () => {
  const { currentWorkspace } = useWorkspaces();

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--spacing-xl)' }}>
      <div>
        <h1 style={{ fontSize: '2rem', fontWeight: 800 }}>
          Dashboard
        </h1>
        <p style={{ color: 'var(--text-muted)' }}>
          Overview of organic and paid marketing efforts in <strong style={{ color: 'var(--primary)' }}>{currentWorkspace?.name || 'Active Workspace'}</strong>
        </p>
      </div>

      {/* KPI Cards Row */}
      <div className="grid grid-cols-4" style={{ gap: 'var(--spacing-lg)' }}>
        <KPICard title="Connected Accounts" value="4" change="+1 this week" trend="up" />
        <KPICard title="Scheduled Posts" value="8" change="Next tomorrow 9:00 AM" trend="neutral" />
        <KPICard title="Inbox Unread" value="14" change="-15% vs yesterday" trend="up" />
        <KPICard title="Total Ad Spend" value="$21,350" change="+$4,200 this month" trend="down" />
      </div>

      {/* Analytics Charts */}
      <div className="grid grid-cols-2" style={{ gap: 'var(--spacing-lg)' }}>
        {/* Engagement Trend Chart */}
        <div className="glass-card">
          <h3 style={{ marginBottom: 'var(--spacing-lg)' }}>Engagement Velocity</h3>
          <div style={{ width: '100%', height: '300px' }}>
            <ResponsiveContainer width="100%" height="100%">
              <AreaChart data={engagementData} margin={{ top: 10, right: 30, left: 0, bottom: 0 }}>
                <defs>
                  <linearGradient id="colorOrganic" x1="0" y1="0" x2="0" y2="1">
                    <stop offset="5%" stopColor="var(--primary)" stopOpacity={0.8}/>
                    <stop offset="95%" stopColor="var(--primary)" stopOpacity={0}/>
                  </linearGradient>
                  <linearGradient id="colorPaid" x1="0" y1="0" x2="0" y2="1">
                    <stop offset="5%" stopColor="var(--secondary)" stopOpacity={0.8}/>
                    <stop offset="95%" stopColor="var(--secondary)" stopOpacity={0}/>
                  </linearGradient>
                </defs>
                <CartesianGrid strokeDasharray="3 3" stroke="var(--border)" />
                <XAxis dataKey="name" stroke="var(--text-muted)" />
                <YAxis stroke="var(--text-muted)" />
                <ChartTooltip contentStyle={{ backgroundColor: 'var(--bg-secondary)', borderColor: 'var(--border)' }} />
                <Area type="monotone" dataKey="organic" stroke="var(--primary)" fillOpacity={1} fill="url(#colorOrganic)" />
                <Area type="monotone" dataKey="paid" stroke="var(--secondary)" fillOpacity={1} fill="url(#colorPaid)" />
              </AreaChart>
            </ResponsiveContainer>
          </div>
        </div>

        {/* Reach Split Chart */}
        <div className="glass-card">
          <h3 style={{ marginBottom: 'var(--spacing-lg)' }}>Organic vs Paid Reach Split</h3>
          <div style={{ width: '100%', height: '300px' }}>
            <ResponsiveContainer width="100%" height="100%">
              <BarChart data={engagementData}>
                <CartesianGrid strokeDasharray="3 3" stroke="var(--border)" />
                <XAxis dataKey="name" stroke="var(--text-muted)" />
                <YAxis stroke="var(--text-muted)" />
                <ChartTooltip contentStyle={{ backgroundColor: 'var(--bg-secondary)', borderColor: 'var(--border)' }} />
                <Legend />
                <Bar dataKey="organic" fill="var(--primary)" radius={[4, 4, 0, 0]} />
                <Bar dataKey="paid" fill="var(--secondary)" radius={[4, 4, 0, 0]} />
              </BarChart>
            </ResponsiveContainer>
          </div>
        </div>
      </div>
    </div>
  );
};
export default DashboardPage;
