import React, { useState } from 'react';
import { ResponsiveContainer, AreaChart, Area, XAxis, YAxis, CartesianGrid, Tooltip, Legend, PieChart, Pie, Cell } from 'recharts';
import { useToastStore } from '../stores/useToastStore';
import { Download, Sparkles, TrendingUp, BarChart2 } from 'lucide-react';
import { DataTable } from '../components/ui/DataTable';

const MOCK_TIME_SERIES = [
  { date: 'Jul 12', organicReach: 1200, paidImpressions: 2400, engagementRate: 4.2 },
  { date: 'Jul 13', organicReach: 1500, paidImpressions: 3100, engagementRate: 4.8 },
  { date: 'Jul 14', organicReach: 1800, paidImpressions: 2900, engagementRate: 4.5 },
  { date: 'Jul 15', organicReach: 2400, paidImpressions: 4200, engagementRate: 5.1 },
  { date: 'Jul 16', organicReach: 2100, paidImpressions: 3800, engagementRate: 4.9 },
  { date: 'Jul 17', organicReach: 2800, paidImpressions: 5100, engagementRate: 5.6 },
  { date: 'Jul 18', organicReach: 3200, paidImpressions: 6400, engagementRate: 6.2 },
];

const MOCK_PLATFORM_DATA = [
  { name: 'Meta', value: 5500, color: 'var(--primary)' },
  { name: 'TikTok', value: 3400, color: 'var(--success)' },
  { name: 'LinkedIn', value: 1200, color: 'var(--info)' },
  { name: 'Twitter/X', value: 800, color: 'var(--text-muted)' },
];

const MOCK_POSTS = [
  { id: '1', title: 'How Serendia optimizes ad workflows', platform: 'LINKEDIN', reach: 2400, engagement: '12.4%', cpc: '$0.42' },
  { id: '2', title: 'Summer Sale Promo Video 2026', platform: 'TIKTOK', reach: 18200, engagement: '9.8%', cpc: '$0.18' },
  { id: '3', title: 'Product Launch Carousel Ad', platform: 'META', reach: 12500, engagement: '7.5%', cpc: '$0.35' },
  { id: '4', title: 'Announcing our new automated budget reallocation', platform: 'META', reach: 9800, engagement: '6.9%', cpc: '$0.28' },
];

export const AnalyticsPage: React.FC = () => {
  const addToast = useToastStore((state) => state.addToast);
  const [range, setRange] = useState('7d');

  const handleExportPDF = () => {
    addToast('Generating PDF Analytics report...', 'info');
    setTimeout(() => {
      addToast('Analytics report PDF downloaded successfully.', 'success');
    }, 2000);
  };

  const columns = [
    { header: 'Post Description', accessor: (row: any) => row.title },
    { header: 'Platform', accessor: (row: any) => row.platform },
    { header: 'Reach', accessor: (row: any) => row.reach.toLocaleString() },
    { header: 'Engagement Rate', accessor: (row: any) => row.engagement },
    { header: 'Cost per Click', accessor: (row: any) => row.cpc },
  ];

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--spacing-xl)' }}>
      {/* Header */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: 'var(--spacing-md)' }}>
        <div>
          <h2>Analytics Dashboard</h2>
          <p style={{ color: 'var(--text-muted)' }}>Cross-platform reach, impression metrics, and paid campaign performance.</p>
        </div>
        <div style={{ display: 'flex', alignItems: 'center', gap: 'var(--spacing-sm)' }}>
          <div style={{ display: 'flex', backgroundColor: 'rgba(255,255,255,0.05)', borderRadius: 'var(--radius-sm)', padding: '4px', border: '1px solid var(--border)' }}>
            <button 
              onClick={() => setRange('7d')} 
              className={`button button-sm ${range === '7d' ? 'button-primary' : ''}`}
              style={{ padding: '6px 12px', border: 'none', background: range === '7d' ? 'var(--primary-gradient)' : 'transparent', color: '#fff', cursor: 'pointer', borderRadius: 'var(--radius-sm)' }}
            >
              7d
            </button>
            <button 
              onClick={() => setRange('30d')} 
              className={`button button-sm ${range === '30d' ? 'button-primary' : ''}`}
              style={{ padding: '6px 12px', border: 'none', background: range === '30d' ? 'var(--primary-gradient)' : 'transparent', color: '#fff', cursor: 'pointer', borderRadius: 'var(--radius-sm)' }}
            >
              30d
            </button>
            <button 
              onClick={() => setRange('90d')} 
              className={`button button-sm ${range === '90d' ? 'button-primary' : ''}`}
              style={{ padding: '6px 12px', border: 'none', background: range === '90d' ? 'var(--primary-gradient)' : 'transparent', color: '#fff', cursor: 'pointer', borderRadius: 'var(--radius-sm)' }}
            >
              90d
            </button>
          </div>
          <button onClick={handleExportPDF} className="button button-secondary" style={{ display: 'flex', alignItems: 'center', gap: 'var(--spacing-xs)' }}>
            <Download size={16} /> Export PDF
          </button>
        </div>
      </div>

      {/* Grid: Charts */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(320px, 1fr))', gap: 'var(--spacing-lg)' }}>
        {/* Engagement Line Chart */}
        <div className="glass-card" style={{ minHeight: '380px', display: 'flex', flexDirection: 'column' }}>
          <h3 style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: 'var(--spacing-md)' }}>
            <TrendingUp size={20} style={{ color: 'var(--primary)' }} /> Reach & Impressions Trend
          </h3>
          <div style={{ flex: 1, minHeight: '300px' }}>
            <ResponsiveContainer width="100%" height="100%">
              <AreaChart data={MOCK_TIME_SERIES} margin={{ top: 10, right: 10, left: -20, bottom: 0 }}>
                <defs>
                  <linearGradient id="colorOrganic" x1="0" y1="0" x2="0" y2="1">
                    <stop offset="5%" stopColor="var(--primary)" stopOpacity={0.3}/>
                    <stop offset="95%" stopColor="var(--primary)" stopOpacity={0}/>
                  </linearGradient>
                  <linearGradient id="colorPaid" x1="0" y1="0" x2="0" y2="1">
                    <stop offset="5%" stopColor="var(--success)" stopOpacity={0.3}/>
                    <stop offset="95%" stopColor="var(--success)" stopOpacity={0}/>
                  </linearGradient>
                </defs>
                <CartesianGrid strokeDasharray="3 3" stroke="rgba(255,255,255,0.05)" />
                <XAxis dataKey="date" stroke="var(--text-muted)" fontSize={11} />
                <YAxis stroke="var(--text-muted)" fontSize={11} />
                <Tooltip 
                  contentStyle={{ backgroundColor: 'var(--bg-card)', borderColor: 'var(--border)', borderRadius: 'var(--radius-sm)' }}
                  labelStyle={{ color: 'var(--text-muted)' }}
                />
                <Legend verticalAlign="top" height={36} iconType="circle" />
                <Area type="monotone" name="Organic Reach" dataKey="organicReach" stroke="var(--primary)" fillOpacity={1} fill="url(#colorOrganic)" />
                <Area type="monotone" name="Paid Impressions" dataKey="paidImpressions" stroke="var(--success)" fillOpacity={1} fill="url(#colorPaid)" />
              </AreaChart>
            </ResponsiveContainer>
          </div>
        </div>

        {/* Platform Share Pie Chart */}
        <div className="glass-card" style={{ minHeight: '380px', display: 'flex', flexDirection: 'column' }}>
          <h3 style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: 'var(--spacing-md)' }}>
            <BarChart2 size={20} style={{ color: 'var(--success)' }} /> Audience Share by Platform
          </h3>
          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', flex: 1, minHeight: '300px', gap: 'var(--spacing-lg)', flexWrap: 'wrap' }}>
            <div style={{ width: '180px', height: '180px' }}>
              <ResponsiveContainer width="100%" height="100%">
                <PieChart>
                  <Pie
                    data={MOCK_PLATFORM_DATA}
                    cx="50%"
                    cy="50%"
                    innerRadius={55}
                    outerRadius={75}
                    paddingAngle={3}
                    dataKey="value"
                  >
                    {MOCK_PLATFORM_DATA.map((entry, index) => (
                      <Cell key={`cell-${index}`} fill={entry.color} />
                    ))}
                  </Pie>
                  <Tooltip 
                    contentStyle={{ backgroundColor: 'var(--bg-card)', borderColor: 'var(--border)', borderRadius: 'var(--radius-sm)' }}
                  />
                </PieChart>
              </ResponsiveContainer>
            </div>
            
            {/* Legend Column */}
            <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
              {MOCK_PLATFORM_DATA.map((item, idx) => (
                <div key={idx} style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                  <div style={{ width: '10px', height: '10px', borderRadius: '50%', backgroundColor: item.color }} />
                  <div>
                    <div style={{ fontSize: '0.85rem', fontWeight: 600 }}>{item.name}</div>
                    <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>{item.value.toLocaleString()} reach</div>
                  </div>
                </div>
              ))}
            </div>
          </div>
        </div>
      </div>

      {/* Top Posts Table */}
      <div className="glass-card">
        <h3 style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: 'var(--spacing-md)' }}>
          <Sparkles size={20} style={{ color: 'var(--primary)' }} /> Top Performing Content
        </h3>
        <DataTable columns={columns} data={MOCK_POSTS} />
      </div>
    </div>
  );
};
export default AnalyticsPage;
