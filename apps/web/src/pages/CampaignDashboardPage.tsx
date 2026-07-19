import React, { useState } from 'react';
import { useCampaigns, Campaign } from '../hooks/useCampaigns';
import { KPICard } from '../components/ui/KPICard';
import { DataTable } from '../components/ui/DataTable';
import { PlatformBadge } from '../components/ui/PlatformBadge';
import { Badge } from '../components/ui/Badge';
import { Button } from '../components/ui/Button';
import { useToastStore } from '../stores/useToastStore';
import { Sparkles } from 'lucide-react';

export const CampaignDashboardPage: React.FC = () => {
  const { campaigns: initialCampaigns, isLoadingCampaigns } = useCampaigns();
  const [campaigns, setCampaigns] = useState<Campaign[]>([]);
  const addToast = useToastStore((state) => state.addToast);

  React.useEffect(() => {
    if (initialCampaigns.length > 0 && campaigns.length === 0) {
      setCampaigns(initialCampaigns);
    }
  }, [initialCampaigns, campaigns]);

  const handleToggleStatus = (id: string) => {
    setCampaigns(prev =>
      prev.map(c => {
        if (c.id === id) {
          const newStatus = c.status === 'ACTIVE' ? 'PAUSED' : 'ACTIVE';
          addToast(`Campaign status updated to ${newStatus}`, 'success');
          return { ...c, status: newStatus as any };
        }
        return c;
      })
    );
  };

  const columns = [
    {
      header: 'Campaign Name',
      accessor: (row: Campaign) => <strong style={{ color: 'var(--text-primary)' }}>{row.name}</strong>
    },
    {
      header: 'Platform',
      accessor: (row: Campaign) => <PlatformBadge platform={row.platform} />
    },
    {
      header: 'Status',
      accessor: (row: Campaign) => (
        <Badge variant={row.status === 'ACTIVE' ? 'success' : row.status === 'PAUSED' ? 'warning' : 'neutral'}>
          {row.status}
        </Badge>
      )
    },
    {
      header: 'Daily Budget',
      accessor: (row: Campaign) => <span>${row.dailyBudget.toFixed(2)}</span>
    },
    {
      header: 'CPA',
      accessor: (row: Campaign) => <strong style={{ color: 'var(--primary)' }}>${row.cpa.toFixed(2)}</strong>
    },
    {
      header: 'Impressions',
      accessor: (row: Campaign) => <span>{row.impressions.toLocaleString()}</span>
    },
    {
      header: 'Total Spend',
      accessor: (row: Campaign) => <span>${row.spend.toLocaleString()}</span>
    },
    {
      header: 'Actions',
      accessor: (row: Campaign) => (
        <Button variant="outline" size="sm" onClick={() => handleToggleStatus(row.id)}>
          {row.status === 'ACTIVE' ? 'Pause' : 'Resume'}
        </Button>
      )
    }
  ];

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--spacing-xl)' }}>
      {/* Header */}
      <div>
        <h1 style={{ fontSize: '2rem', fontWeight: 800 }}>Ad Campaigns</h1>
        <p style={{ color: 'var(--text-muted)' }}>Manage, optimize and track cross-platform paid advertisement channels</p>
      </div>

      {/* KPI summaries */}
      <div className="grid grid-cols-4" style={{ gap: 'var(--spacing-lg)' }}>
        <KPICard title="Total Ad Budget" value="$1,050 / day" />
        <KPICard title="Average CPA" value="$3.78" change="-8% from last week" trend="up" />
        <KPICard title="Total Conversions" value="4,120" change="+12%" trend="up" />
        <KPICard title="Average CTR" value="3.2%" change="Flat" trend="neutral" />
      </div>

      {/* Cross-Platform Ad Reallocation Banner */}
      <div style={{
        background: 'linear-gradient(135deg, rgba(102, 252, 241, 0.1) 0%, rgba(69, 162, 158, 0.1) 100%)',
        border: '1px solid var(--primary)',
        borderRadius: 'var(--radius-md)',
        padding: 'var(--spacing-lg)',
        display: 'flex',
        alignItems: 'center',
        gap: 'var(--spacing-lg)'
      }}>
        <div style={{
          backgroundColor: 'var(--bg-primary)',
          borderRadius: 'var(--radius-full)',
          width: '48px',
          height: '48px',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          color: 'var(--primary)',
          boxShadow: '0 0 10px var(--primary-glow)'
        }}>
          <Sparkles size={24} />
        </div>
        <div style={{ flex: 1 }}>
          <h3 style={{ margin: 0, fontSize: '1.05rem', color: 'var(--primary)' }}>Cross-Platform Ad Budget Reallocation Enabled</h3>
          <p style={{ margin: 0, fontSize: '0.85rem', color: 'var(--text-secondary)' }}>
            The AI engine automatically shifted <strong>$120.00 (30% daily budget)</strong> from Facebook Ads to TikTok Ads 3 hours ago due to a <strong>27% CPA delta</strong>.
          </p>
        </div>
        <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)', textAlign: 'right' }}>
          <span>Status: <strong>OPTIMAL</strong></span>
          <br />
          <span>Last sync: 12m ago</span>
        </div>
      </div>

      {/* Campaigns list */}
      <div className="glass-card" style={{ display: 'flex', flexDirection: 'column', gap: 'var(--spacing-md)' }}>
        <h3 style={{ margin: 0 }}>Active Campaigns</h3>
        {isLoadingCampaigns ? (
          <div style={{ textAlign: 'center', padding: 'var(--spacing-xl)', color: 'var(--text-muted)' }}>Loading campaigns...</div>
        ) : (
          <DataTable columns={columns} data={campaigns} />
        )}
      </div>
    </div>
  );
};
export default CampaignDashboardPage;
