import React, { useEffect, useState } from 'react';
import { useSearchParams, useNavigate } from 'react-router-dom';
import { api } from '../lib/api';
import { useToastStore } from '../stores/useToastStore';
import { useOnboardingStore } from '../stores/useOnboardingStore';
import { Loader } from 'lucide-react';

export const OAuthCallbackPage: React.FC = () => {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const addToast = useToastStore((state) => state.addToast);
  const [status, setStatus] = useState('Verifying OAuth authorization code...');

  useEffect(() => {
    const code = searchParams.get('code');
    const state = searchParams.get('state');

    if (!code || !state) {
      addToast('Invalid OAuth Callback parameters', 'error');
      navigate('/settings');
      return;
    }

    const exchangeCode = async () => {
      try {
        const wasComplete = useOnboardingStore.getState().onboardingComplete;

        await api.get('/core/oauth/callback', {
          params: { code, state }
        });

        // Set onboardingComplete to true since they just connected a social account
        useOnboardingStore.getState().setOnboardingComplete(true);

        setStatus('Successfully connected social account!');
        addToast('Social account connected!', 'success');
        setTimeout(() => {
          navigate(wasComplete ? '/settings' : '/setup');
        }, 1500);
      } catch (e: any) {
        const msg = e.response?.data?.message || 'Failed to authorize connected social account';
        setStatus('Authorization failed: ' + msg);
        addToast(msg, 'error');
        setTimeout(() => {
          const wasComplete = useOnboardingStore.getState().onboardingComplete;
          navigate(wasComplete ? '/settings' : '/setup');
        }, 3000);
      }
    };

    exchangeCode();
  }, [searchParams, navigate, addToast]);

  return (
    <div style={{
      display: 'flex',
      flexDirection: 'column',
      alignItems: 'center',
      justifyContent: 'center',
      minHeight: '100vh',
      gap: 'var(--spacing-md)'
    }}>
      <Loader size={36} className="animate-pulse" style={{ color: 'var(--primary)' }} />
      <h3>Social Account Authorization</h3>
      <p style={{ color: 'var(--text-muted)' }}>{status}</p>
    </div>
  );
};
export default OAuthCallbackPage;
