import React, { useState } from 'react';
import { Button } from '../components/ui/Button';
import { api } from '../lib/api';
import { useToastStore } from '../stores/useToastStore';
import { Sparkles, Copy, Calendar } from 'lucide-react';
import { useNavigate } from 'react-router-dom';

export const AIToolsPage: React.FC = () => {
  const [url, setUrl] = useState('');
  const [selectedPlatforms, setSelectedPlatforms] = useState<string[]>([]);
  const [variants, setVariants] = useState<Record<string, string>>({});
  const [isLoading, setIsLoading] = useState(false);
  const addToast = useToastStore((state) => state.addToast);
  const navigate = useNavigate();

  const handlePlatformToggle = (plat: string) => {
    setSelectedPlatforms(prev =>
      prev.includes(plat) ? prev.filter(p => p !== plat) : [...prev, plat]
    );
  };

  const handleSlice = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!url) return;
    if (selectedPlatforms.length === 0) {
      addToast('Please select at least one target platform', 'warning');
      return;
    }

    setIsLoading(true);
    try {
      // Call ai-service Content Slicer endpoint through reverse proxy
      const res = await api.post('/ai/ai/content-slicer', {
        url,
        targetPlatforms: selectedPlatforms
      });
      setVariants(res.data.variants || {});
      addToast('AI successfully sliced long-form content!', 'success');
    } catch (e) {
      addToast('Failed to slice content', 'error');
    } finally {
      setIsLoading(false);
    }
  };

  const handleCopy = (text: string) => {
    navigator.clipboard.writeText(text);
    addToast('Copied to clipboard', 'success');
  };

  const handleSendToScheduler = (_text: string, platform: string) => {
    // Navigate to Scheduler and prefill the composer (simulation)
    addToast(`Prefilling composer with ${platform} variant`, 'info');
    navigate('/scheduler');
  };

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--spacing-xl)' }}>
      {/* Header */}
      <div>
        <h1 style={{ fontSize: '2rem', fontWeight: 800 }}>AI Slicer & Copywriter</h1>
        <p style={{ color: 'var(--text-muted)' }}>Generate platform-optimized short-form copy from long-form article URLs</p>
      </div>

      <div className="grid grid-cols-2" style={{ gap: 'var(--spacing-lg)', alignItems: 'start' }}>
        
        {/* Left Panel: Input */}
        <div className="glass-card">
          <h3 style={{ marginBottom: 'var(--spacing-md)' }}>Long-Form Source</h3>
          <form onSubmit={handleSlice} style={{ display: 'flex', flexDirection: 'column', gap: 'var(--spacing-md)' }}>
            
            <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--spacing-xs)' }}>
              <label style={{ fontSize: '0.8rem', color: 'var(--text-secondary)' }}>Article URL</label>
              <input
                type="url"
                value={url}
                onChange={(e) => setUrl(e.target.value)}
                placeholder="https://example.com/blog/article-title"
                required
                style={{
                  backgroundColor: 'rgba(255, 255, 255, 0.03)',
                  border: '1px solid var(--border)',
                  borderRadius: 'var(--radius-sm)',
                  padding: '12px',
                  color: 'var(--text-primary)'
                }}
              />
            </div>

            <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--spacing-xs)' }}>
              <label style={{ fontSize: '0.8rem', color: 'var(--text-secondary)' }}>Target Platforms</label>
              <div style={{ display: 'flex', gap: '8px', flexWrap: 'wrap' }}>
                {['FACEBOOK', 'INSTAGRAM', 'TIKTOK', 'LINKEDIN', 'X'].map((plat) => (
                  <button
                    type="button"
                    key={plat}
                    onClick={() => handlePlatformToggle(plat)}
                    style={{
                      padding: '8px 16px',
                      borderRadius: 'var(--radius-full)',
                      border: '1px solid var(--border)',
                      backgroundColor: selectedPlatforms.includes(plat) ? 'var(--primary)' : 'var(--bg-secondary)',
                      color: selectedPlatforms.includes(plat) ? 'var(--bg-primary)' : 'var(--text-primary)',
                      cursor: 'pointer',
                      fontWeight: 600,
                      fontSize: '0.75rem',
                      transition: 'var(--transition-smooth)'
                    }}
                  >
                    {plat}
                  </button>
                ))}
              </div>
            </div>

            <Button type="submit" isLoading={isLoading} style={{ marginTop: '8px', padding: '12px' }}>
              <Sparkles size={16} /> Slice Content
            </Button>
          </form>
        </div>

        {/* Right Panel: Output Variants */}
        <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--spacing-lg)' }}>
          {Object.keys(variants).length === 0 ? (
            <div className="glass-card" style={{ textAlign: 'center', padding: 'var(--spacing-xl)', color: 'var(--text-muted)' }}>
              <Sparkles size={48} style={{ marginBottom: 'var(--spacing-md)', color: 'var(--text-muted)' }} />
              <h3>Generated Variants</h3>
              <p style={{ fontSize: '0.85rem' }}>Select targets and slice an article URL to generate platform-optimized copy variants.</p>
            </div>
          ) : (
            Object.entries(variants).map(([platform, text]) => (
              <div key={platform} className="glass-card" style={{ display: 'flex', flexDirection: 'column', gap: 'var(--spacing-md)' }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                  <span className="badge" style={{ backgroundColor: 'var(--primary-glow)', color: 'var(--primary)', borderColor: 'var(--primary)' }}>
                    {platform}
                  </span>
                  <div style={{ display: 'flex', gap: '8px' }}>
                    <button
                      onClick={() => handleCopy(text)}
                      style={{ cursor: 'pointer', color: 'var(--text-muted)', display: 'inline-flex', alignItems: 'center', gap: '4px', fontSize: '0.8rem' }}
                    >
                      <Copy size={14} /> Copy
                    </button>
                    <button
                      onClick={() => handleSendToScheduler(text, platform)}
                      style={{ cursor: 'pointer', color: 'var(--primary)', display: 'inline-flex', alignItems: 'center', gap: '4px', fontSize: '0.8rem' }}
                    >
                      <Calendar size={14} /> Schedule
                    </button>
                  </div>
                </div>
                <textarea
                  readOnly
                  value={text}
                  rows={4}
                  style={{
                    backgroundColor: 'transparent',
                    border: 'none',
                    color: 'var(--text-secondary)',
                    resize: 'none',
                    fontSize: '0.85rem',
                    lineHeight: '1.4'
                  }}
                />
              </div>
            ))
          )}
        </div>

      </div>
    </div>
  );
};
export default AIToolsPage;
