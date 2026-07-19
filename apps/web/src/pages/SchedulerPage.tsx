import React, { useState } from 'react';
import FullCalendar from '@fullcalendar/react';
import dayGridPlugin from '@fullcalendar/daygrid';
import timeGridPlugin from '@fullcalendar/timegrid';
import interactionPlugin from '@fullcalendar/interaction';
import { useWorkspaces } from '../hooks/useWorkspaces';
import { Button } from '../components/ui/Button';
import { Modal } from '../components/ui/Modal';
import { api } from '../lib/api';
import { useToastStore } from '../stores/useToastStore';
import { useQuery } from '@tanstack/react-query';
import { Plus } from 'lucide-react';

export const SchedulerPage: React.FC = () => {
  const { currentWorkspace } = useWorkspaces();
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [content, setContent] = useState('');
  const [scheduledAt, setScheduledAt] = useState('');
  const [selectedPlatforms, setSelectedPlatforms] = useState<string[]>([]);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const addToast = useToastStore((state) => state.addToast);

  const workspaceId = currentWorkspace?.id;

  // Fetch scheduled posts
  const { data: posts = [], refetch } = useQuery({
    queryKey: ['posts', workspaceId],
    queryFn: async () => {
      if (!workspaceId) return [];
      const res = await api.get(`/workspaces/${workspaceId}/posts`, {
        params: { page: 0, size: 100 }
      });
      return res.data.content || [];
    },
    enabled: !!workspaceId
  });

  // Map posts into FullCalendar event objects
  const events = posts
    .filter((p: any) => p.scheduledAt)
    .map((p: any) => ({
      id: p.id,
      title: p.content ? (p.content.substring(0, 30) + '...') : 'Post',
      start: p.scheduledAt,
      color: p.status === 'PUBLISHED' ? 'var(--success)' : p.status === 'FAILED' ? 'var(--error)' : 'var(--primary)',
      textColor: 'var(--bg-primary)'
    }));

  const handlePlatformToggle = (platform: string) => {
    setSelectedPlatforms(prev =>
      prev.includes(platform)
        ? prev.filter(p => p !== platform)
        : [...prev, platform]
    );
  };

  const handleCreatePost = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!workspaceId || selectedPlatforms.length === 0) {
      addToast('Select at least one social platform target', 'warning');
      return;
    }
    setIsSubmitting(true);
    const payload = {
      content,
      mediaUrls: [],
      platforms: selectedPlatforms,
      scheduledAt: scheduledAt ? new Date(scheduledAt).toISOString() : null
    };

    try {
      await api.post(`/workspaces/${workspaceId}/posts`, payload);
      addToast(scheduledAt ? 'Post scheduled successfully!' : 'Post queued for immediate publishing!', 'success');
      setIsModalOpen(false);
      setContent('');
      setScheduledAt('');
      setSelectedPlatforms([]);
      refetch();
    } catch (e) {
      addToast('Failed to create post', 'error');
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--spacing-xl)' }}>
      {/* Header */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <div>
          <h1 style={{ fontSize: '2rem', fontWeight: 800 }}>Content Calendar</h1>
          <p style={{ color: 'var(--text-muted)' }}>Visualize, plan, and schedule post campaigns</p>
        </div>
        <Button onClick={() => setIsModalOpen(true)}>
          <Plus size={18} /> Compose Post
        </Button>
      </div>

      {/* Full Calendar container */}
      <div className="glass-card" style={{ padding: 'var(--spacing-lg)' }}>
        <FullCalendar
          plugins={[dayGridPlugin, timeGridPlugin, interactionPlugin]}
          initialView="dayGridMonth"
          headerToolbar={{
            left: 'prev,next today',
            center: 'title',
            right: 'dayGridMonth,timeGridWeek,timeGridDay'
          }}
          events={events}
          editable={true}
          selectable={true}
          height="70vh"
        />
      </div>

      {/* New Post Modal */}
      <Modal isOpen={isModalOpen} onClose={() => setIsModalOpen(false)} title="Compose Post">
        <form onSubmit={handleCreatePost} style={{ display: 'flex', flexDirection: 'column', gap: 'var(--spacing-md)' }}>
          <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--spacing-xs)' }}>
            <label style={{ fontSize: '0.8rem', fontWeight: 600, color: 'var(--text-secondary)' }}>Content Text</label>
            <textarea
              value={content}
              onChange={(e) => setContent(e.target.value)}
              placeholder="What would you like to share?"
              rows={4}
              required
              style={{
                backgroundColor: 'rgba(255, 255, 255, 0.03)',
                border: '1px solid var(--border)',
                borderRadius: 'var(--radius-sm)',
                padding: '12px',
                color: 'var(--text-primary)',
                resize: 'none'
              }}
            />
          </div>

          <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--spacing-xs)' }}>
            <label style={{ fontSize: '0.8rem', fontWeight: 600, color: 'var(--text-secondary)' }}>Platform Targets</label>
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
                    fontSize: '0.75rem'
                  }}
                >
                  {plat}
                </button>
              ))}
            </div>
          </div>

          <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--spacing-xs)' }}>
            <label style={{ fontSize: '0.8rem', fontWeight: 600, color: 'var(--text-secondary)' }}>Schedule Date (Optional)</label>
            <input
              type="datetime-local"
              value={scheduledAt}
              onChange={(e) => setScheduledAt(e.target.value)}
              style={{
                backgroundColor: 'rgba(255, 255, 255, 0.03)',
                border: '1px solid var(--border)',
                borderRadius: 'var(--radius-sm)',
                padding: '12px',
                color: 'var(--text-primary)'
              }}
            />
          </div>

          <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '12px', marginTop: 'var(--spacing-md)' }}>
            <Button type="button" variant="secondary" onClick={() => setIsModalOpen(false)}>
              Cancel
            </Button>
            <Button type="submit" isLoading={isSubmitting}>
              {scheduledAt ? 'Schedule' : 'Publish Now'}
            </Button>
          </div>
        </form>
      </Modal>
    </div>
  );
};
export default SchedulerPage;
