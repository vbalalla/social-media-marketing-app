import React, { useState } from 'react';
import { useWorkspaces } from '../hooks/useWorkspaces';
import { api } from '../lib/api';
import { useToastStore } from '../stores/useToastStore';
import { Button } from '../components/ui/Button';
import { useQuery } from '@tanstack/react-query';
import { CheckCircle, Globe, Trash2, Shield, Users } from 'lucide-react';

export const SettingsPage: React.FC = () => {
  const { currentWorkspace } = useWorkspaces();
  const [workspaceName, setWorkspaceName] = useState(currentWorkspace?.name || '');
  const addToast = useToastStore((state) => state.addToast);

  const workspaceId = currentWorkspace?.id;

  // Fetch connected accounts
  const { data: accounts = [], refetch: refetchAccounts } = useQuery({
    queryKey: ['social-accounts', workspaceId],
    queryFn: async () => {
      if (!workspaceId) return [];
      const res = await api.get(`/core/workspaces/${workspaceId}/social-accounts`);
      return res.data || [];
    },
    enabled: !!workspaceId
  });

  const handleOAuthConnect = async (platform: string) => {
    if (!workspaceId) {
      addToast('Please select a workspace first', 'warning');
      return;
    }

    try {
      // Fetch OAuth URL from core-service
      const res = await api.get('/core/oauth/init', {
        params: { platform, workspaceId }
      });
      const authUrl = res.data.authorizationUrl;
      
      addToast(`Redirecting to ${platform} Authorization...`, 'info');
      
      // Redirect browser
      window.location.href = authUrl;
    } catch (e) {
      addToast(`Failed to initialize OAuth for ${platform}`, 'error');
    }
  };

  const handleDisconnect = async (id: string) => {
    if (!confirm('Are you sure you want to disconnect this account?')) return;
    try {
      await api.delete(`/core/social-accounts/${id}`);
      addToast('Account disconnected successfully', 'success');
      refetchAccounts();
    } catch (e) {
      addToast('Failed to disconnect account', 'error');
    }
  };

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--spacing-xl)' }}>
      {/* Header */}
      <div>
        <h1 style={{ fontSize: '2rem', fontWeight: 800 }}>Workspace Settings</h1>
        <p style={{ color: 'var(--text-muted)' }}>Configure tenants, team memberships and connect social integrations</p>
      </div>

      <div className="grid grid-cols-2" style={{ gap: 'var(--spacing-lg)', alignItems: 'start' }}>
        
        {/* Left Column: Workspace Info */}
        <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--spacing-lg)' }}>
          
          {/* General settings */}
          <div className="glass-card">
            <h3 style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: 'var(--spacing-md)' }}>
              <Shield size={20} /> Workspace Profile
            </h3>
            <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--spacing-md)' }}>
              <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--spacing-xs)' }}>
                <label style={{ fontSize: '0.8rem', color: 'var(--text-secondary)' }}>Workspace Name</label>
                <input
                  type="text"
                  value={workspaceName || currentWorkspace?.name || ''}
                  onChange={(e) => setWorkspaceName(e.target.value)}
                  style={{
                    backgroundColor: 'rgba(255, 255, 255, 0.03)',
                    border: '1px solid var(--border)',
                    borderRadius: 'var(--radius-sm)',
                    padding: '10px',
                    color: 'var(--text-primary)'
                  }}
                />
              </div>

              <div style={{ display: 'flex', justifyContent: 'flex-end' }}>
                <Button size="sm" onClick={() => addToast('Workspace profile updated!', 'success')}>
                  Save Profile
                </Button>
              </div>
            </div>
          </div>

          {/* Members list */}
          <div className="glass-card">
            <h3 style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: 'var(--spacing-md)' }}>
              <Users size={20} /> Team Members
            </h3>
            <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--spacing-sm)' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '10px', borderBottom: '1px solid var(--border)' }}>
                <div>
                  <strong style={{ fontSize: '0.85rem' }}>Workspace Administrator (Owner)</strong>
                  <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>Role: OWNER</div>
                </div>
                <span className="badge badge-success">Active</span>
              </div>
              <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)', textAlign: 'center', padding: '12px 0' }}>
                No other members invited.
              </div>
            </div>
          </div>

        </div>

        {/* Right Column: Social Integrations */}
        <div className="glass-card" style={{ display: 'flex', flexDirection: 'column', gap: 'var(--spacing-md)' }}>
          <h3 style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
            <Globe size={20} /> Connected Social Channels
          </h3>
          <p style={{ fontSize: '0.85rem', color: 'var(--text-muted)' }}>
            Authorize Serendia to schedule organic posts and fetch messages on your behalf.
          </p>

          <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--spacing-md)', marginTop: 'var(--spacing-sm)' }}>
            {/* Meta (Facebook/Instagram) */}
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '14px', backgroundColor: 'rgba(255,255,255,0.02)', border: '1px solid var(--border)', borderRadius: 'var(--radius-sm)' }}>
              <div>
                <strong>Meta Developer integration</strong>
                <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>Connect Facebook Pages & Instagram Profiles</div>
              </div>
              <Button size="sm" onClick={() => handleOAuthConnect('META')}>
                Connect Meta
              </Button>
            </div>

            {/* TikTok */}
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '14px', backgroundColor: 'rgba(255,255,255,0.02)', border: '1px solid var(--border)', borderRadius: 'var(--radius-sm)' }}>
              <div>
                <strong>TikTok v2 API Integration</strong>
                <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>Connect TikTok Business accounts</div>
              </div>
              <Button size="sm" onClick={() => handleOAuthConnect('TIKTOK')}>
                Connect TikTok
              </Button>
            </div>

            {/* LinkedIn */}
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '14px', backgroundColor: 'rgba(255,255,255,0.02)', border: '1px solid var(--border)', borderRadius: 'var(--radius-sm)' }}>
              <div>
                <strong>LinkedIn Integration</strong>
                <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>Connect LinkedIn Professional profiles & Pages</div>
              </div>
              <Button size="sm" onClick={() => handleOAuthConnect('LINKEDIN')}>
                Connect LinkedIn
              </Button>
            </div>

            {/* X / Twitter */}
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '14px', backgroundColor: 'rgba(255,255,255,0.02)', border: '1px solid var(--border)', borderRadius: 'var(--radius-sm)' }}>
              <div>
                <strong>X (Twitter) Feed</strong>
                <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>Connect X Social Feed Integration</div>
              </div>
              <Button size="sm" onClick={() => handleOAuthConnect('X')}>
                Connect X
              </Button>
            </div>
          </div>

          {/* List of active accounts */}
          <div style={{ marginTop: 'var(--spacing-lg)' }}>
            <h4>Active Integrations</h4>
            <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--spacing-sm)', marginTop: 'var(--spacing-sm)' }}>
              {accounts.length === 0 ? (
                <div style={{ textAlign: 'center', padding: 'var(--spacing-lg)', border: '1px dashed var(--border)', borderRadius: 'var(--radius-sm)', color: 'var(--text-muted)', fontSize: '0.85rem' }}>
                  No social accounts connected to this workspace yet.
                </div>
              ) : (
                accounts.map((acc: any) => (
                  <div key={acc.id} style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '10px var(--spacing-md)', border: '1px solid var(--border)', borderRadius: 'var(--radius-sm)', backgroundColor: 'var(--bg-secondary)' }}>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                      <CheckCircle size={16} style={{ color: 'var(--success)' }} />
                      <strong style={{ fontSize: '0.85rem' }}>{acc.displayName}</strong>
                      <span className="badge badge-neutral" style={{ fontSize: '0.65rem' }}>{acc.platform}</span>
                    </div>
                    <button
                      onClick={() => handleDisconnect(acc.id)}
                      style={{ cursor: 'pointer', color: 'var(--error)' }}
                    >
                      <Trash2 size={16} />
                    </button>
                  </div>
                ))
              )}
            </div>
          </div>
        </div>

      </div>
    </div>
  );
};
export default SettingsPage;
