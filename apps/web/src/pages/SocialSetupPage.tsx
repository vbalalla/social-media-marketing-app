import React from 'react';
import { useNavigate, Navigate } from 'react-router-dom';
import { useAuthStore } from '../stores/useAuthStore';
import { useOnboardingStore } from '../stores/useOnboardingStore';
import { useWorkspaces } from '../hooks/useWorkspaces';
import { useToastStore } from '../stores/useToastStore';
import { useQuery } from '@tanstack/react-query';
import { api } from '../lib/api';
import { Button } from '../components/ui/Button';
import { Facebook, Linkedin, Twitter, Music2, CheckCircle2, ArrowRight } from 'lucide-react';

export const SocialSetupPage: React.FC = () => {
  const token = useAuthStore((state) => state.accessToken);
  const setOnboardingComplete = useOnboardingStore((state) => state.setOnboardingComplete);
  const addToast = useToastStore((state) => state.addToast);
  const navigate = useNavigate();

  // If not logged in, redirect to login page immediately
  if (!token) {
    return <Navigate to="/login" replace />;
  }

  const { currentWorkspace: workspace } = useWorkspaces();
  const workspaceId = workspace?.id;

  // Fetch connected social accounts to see what is already set up
  const { data: accounts = [] } = useQuery({
    queryKey: ['social-accounts', workspaceId],
    queryFn: async () => {
      if (!workspaceId) return [];
      const res = await api.get(`/core/workspaces/${workspaceId}/social-accounts`);
      return res.data || [];
    },
    enabled: !!workspaceId
  });

  const isMetaConnected = accounts.some((a: any) => a.platform === 'FACEBOOK' || a.platform === 'INSTAGRAM');
  const isTikTokConnected = accounts.some((a: any) => a.platform === 'TIKTOK');
  const isLinkedInConnected = accounts.some((a: any) => a.platform === 'LINKEDIN');
  const isXConnected = accounts.some((a: any) => a.platform === 'X');

  const connectedCount = [isMetaConnected, isTikTokConnected, isLinkedInConnected, isXConnected].filter(Boolean).length;

  const handleOAuthConnect = async (platform: string) => {
    if (!workspaceId) {
      addToast('Please create or select a workspace first', 'warning');
      return;
    }

    try {
      const res = await api.get('/core/oauth/init', {
        params: { platform, workspaceId }
      });
      const authUrl = res.data.authorizationUrl;
      addToast(`Redirecting to ${platform} authorization...`, 'info');
      window.location.href = authUrl;
    } catch (e) {
      addToast(`Failed to initialize OAuth for ${platform}`, 'error');
    }
  };

  const handleContinue = () => {
    setOnboardingComplete(true);
    addToast('Onboarding complete! Welcome to Serendia.', 'success');
    navigate('/dashboard');
  };

  return (
    <div style={{
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'center',
      minHeight: '100vh',
      padding: 'var(--spacing-xl)',
      background: 'radial-gradient(circle at center, var(--bg-secondary) 0%, var(--bg-primary) 100%)'
    }}>
      <div className="glass-card" style={{ width: '100%', maxWidth: '800px', padding: 'var(--spacing-xl)' }}>
        {/* Header */}
        <div style={{ textAlign: 'center', marginBottom: 'var(--spacing-xl)' }}>
          <div style={{
            width: '56px',
            height: '56px',
            borderRadius: 'var(--radius-md)',
            background: 'linear-gradient(135deg, var(--primary) 0%, var(--secondary) 100%)',
            margin: '0 auto var(--spacing-md) auto',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            color: 'white',
            fontWeight: 800,
            fontSize: '1.5rem'
          }}>S</div>
          <h1 style={{ fontSize: '2rem', fontWeight: 800 }}>Connect Your Social Channels</h1>
          <p style={{ color: 'var(--text-muted)', marginTop: 'var(--spacing-xs)' }}>
            To start scheduling posts and receiving messages, connect at least one channel below.
          </p>
        </div>

        {/* Progress Bar */}
        <div style={{ marginBottom: 'var(--spacing-xl)' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.85rem', color: 'var(--text-secondary)', marginBottom: '8px' }}>
            <span>Onboarding Progress</span>
            <span>{connectedCount} of 4 connected</span>
          </div>
          <div style={{ width: '100%', height: '8px', backgroundColor: 'rgba(255,255,255,0.05)', borderRadius: '4px', overflow: 'hidden' }}>
            <div style={{
              width: `${(connectedCount / 4) * 100}%`,
              height: '100%',
              background: 'linear-gradient(90deg, var(--primary) 0%, var(--secondary) 100%)',
              transition: 'width 0.4s ease'
            }} />
          </div>
        </div>

        {/* Platforms Grid */}
        <div style={{
          display: 'grid',
          gridTemplateColumns: 'repeat(auto-fit, minmax(320px, 1fr))',
          gap: 'var(--spacing-md)',
          marginBottom: 'var(--spacing-xl)'
        }}>
          {/* Meta (Facebook/Instagram) */}
          <div style={platformCardStyle(isMetaConnected)}>
            <div style={{ display: 'flex', alignItems: 'center', gap: 'var(--spacing-sm)' }}>
              <div style={iconContainerStyle('#1877F2')}>
                <Facebook size={20} color="white" />
              </div>
              <div>
                <h3 style={{ fontWeight: 700 }}>Meta Integration</h3>
                <p style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>Facebook Pages & Instagram Profiles</p>
              </div>
            </div>
            {isMetaConnected ? (
              <span style={connectedBadgeStyle}><CheckCircle2 size={14} /> Connected</span>
            ) : (
              <Button size="sm" onClick={() => handleOAuthConnect('META')}>Connect</Button>
            )}
          </div>

          {/* TikTok */}
          <div style={platformCardStyle(isTikTokConnected)}>
            <div style={{ display: 'flex', alignItems: 'center', gap: 'var(--spacing-sm)' }}>
              <div style={iconContainerStyle('#000000')}>
                <Music2 size={20} color="white" />
              </div>
              <div>
                <h3 style={{ fontWeight: 700 }}>TikTok</h3>
                <p style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>TikTok Business accounts</p>
              </div>
            </div>
            {isTikTokConnected ? (
              <span style={connectedBadgeStyle}><CheckCircle2 size={14} /> Connected</span>
            ) : (
              <Button size="sm" onClick={() => handleOAuthConnect('TIKTOK')}>Connect</Button>
            )}
          </div>

          {/* LinkedIn */}
          <div style={platformCardStyle(isLinkedInConnected)}>
            <div style={{ display: 'flex', alignItems: 'center', gap: 'var(--spacing-sm)' }}>
              <div style={iconContainerStyle('#0A66C2')}>
                <Linkedin size={20} color="white" />
              </div>
              <div>
                <h3 style={{ fontWeight: 700 }}>LinkedIn</h3>
                <p style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>LinkedIn Professional / Organization</p>
              </div>
            </div>
            {isLinkedInConnected ? (
              <span style={connectedBadgeStyle}><CheckCircle2 size={14} /> Connected</span>
            ) : (
              <Button size="sm" onClick={() => handleOAuthConnect('LINKEDIN')}>Connect</Button>
            )}
          </div>

          {/* X / Twitter */}
          <div style={platformCardStyle(isXConnected)}>
            <div style={{ display: 'flex', alignItems: 'center', gap: 'var(--spacing-sm)' }}>
              <div style={iconContainerStyle('#1DA1F2')}>
                <Twitter size={20} color="white" />
              </div>
              <div>
                <h3 style={{ fontWeight: 700 }}>X (Twitter)</h3>
                <p style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>X Social Feed Integration</p>
              </div>
            </div>
            {isXConnected ? (
              <span style={connectedBadgeStyle}><CheckCircle2 size={14} /> Connected</span>
            ) : (
              <Button size="sm" onClick={() => handleOAuthConnect('X')}>Connect</Button>
            )}
          </div>
        </div>

        {/* Footer Actions */}
        <div style={{
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center',
          borderTop: '1px solid var(--border)',
          paddingTop: 'var(--spacing-lg)'
        }}>
          <button
            onClick={() => handleContinue()}
            style={{
              background: 'none',
              border: 'none',
              color: 'var(--text-secondary)',
              cursor: 'pointer',
              fontSize: '0.9rem',
              textDecoration: 'underline'
            }}
          >
            Skip for now
          </button>

          <Button
            onClick={handleContinue}
            disabled={connectedCount === 0}
            style={{ display: 'flex', alignItems: 'center', gap: '8px' }}
          >
            Continue to Dashboard <ArrowRight size={16} />
          </Button>
        </div>
      </div>
    </div>
  );
};

// Styles
const platformCardStyle = (connected: boolean): React.CSSProperties => ({
  display: 'flex',
  justifyContent: 'space-between',
  alignItems: 'center',
  padding: '16px',
  borderRadius: 'var(--radius-md)',
  border: connected ? '1px solid var(--success)' : '1px solid var(--border)',
  backgroundColor: connected ? 'rgba(46, 204, 113, 0.03)' : 'rgba(255, 255, 255, 0.02)',
  transition: 'all 0.2s ease'
});

const iconContainerStyle = (bgColor: string): React.CSSProperties => ({
  width: '36px',
  height: '36px',
  borderRadius: 'var(--radius-sm)',
  backgroundColor: bgColor,
  display: 'flex',
  alignItems: 'center',
  justifyContent: 'center'
});

const connectedBadgeStyle: React.CSSProperties = {
  display: 'flex',
  alignItems: 'center',
  gap: '4px',
  fontSize: '0.8rem',
  fontWeight: 600,
  color: 'var(--success)',
  backgroundColor: 'rgba(46, 204, 113, 0.1)',
  padding: '6px 12px',
  borderRadius: '12px'
};

export default SocialSetupPage;
