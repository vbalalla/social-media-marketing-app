import React, { useState } from 'react';
import { useInboxMessages } from '../hooks/useInboxMessages';
import { useInboxStore } from '../stores/useInboxStore';
import { useWorkspaceStore } from '../stores/useWorkspaceStore';
import { PlatformBadge } from '../components/ui/PlatformBadge';
import { SentimentBadge } from '../components/ui/SentimentBadge';
import { Button } from '../components/ui/Button';
import { useToastStore } from '../stores/useToastStore';
import { api } from '../lib/api';
import { MessageSquare, Send, Sparkles } from 'lucide-react';

export const InboxPage: React.FC = () => {
  const { messages, isLoadingMessages, reply, refetchMessages } = useInboxMessages();
  const { filters, selectedMessageId, setFilter, selectMessage } = useInboxStore();
  const currentWorkspace = useWorkspaceStore((state) => state.currentWorkspace);
  const workspaceId = currentWorkspace?.id;
  const [replyText, setReplyText] = useState('');
  const [isSimulating, setIsSimulating] = useState(false);
  const [isGeneratingReply, setIsGeneratingReply] = useState(false);
  const addToast = useToastStore((state) => state.addToast);

  const selectedMsg = messages.find(m => m.id === selectedMessageId);

  const handleAIAssist = async () => {
    if (!selectedMsg) return;
    setIsGeneratingReply(true);
    try {
      addToast('AI is generating a smart reply...', 'info');
      const res = await api.post('/ai/suggest-reply', { text: selectedMsg.content });
      setReplyText(res.data.reply);
      addToast('Smart reply generated!', 'success');
    } catch (err) {
      addToast('Failed to generate smart reply. Fallback active.', 'warning');
      setReplyText(`Thank you for your inquiry about "${selectedMsg.content}". We will get back to you shortly!`);
    } finally {
      setIsGeneratingReply(false);
    }
  };

  const handleSimulateWebhook = async (text: string) => {
    setIsSimulating(true);
    const mockPayload = {
      object: 'page',
      entry: [
        {
          id: workspaceId || '123456789', // Page ID (hashes to a stable workspace UUID)
          time: Date.now(),
          messaging: [
            {
              sender: { id: 'sender_' + Math.floor(Math.random() * 1000) },
              recipient: { id: workspaceId || '123456789' },
              timestamp: Date.now(),
              message: {
                mid: 'mid.' + Math.random().toString(36).substring(2, 15),
                text: text
              }
            }
          ]
        }
      ]
    };

    try {
      // Post to core-service webhook endpoint through proxy
      await api.post('/core/webhooks/meta', mockPayload);
      addToast('Simulated Meta message webhook event!', 'success');
      setTimeout(() => {
        refetchMessages();
      }, 800);
    } catch (e) {
      addToast('Failed to trigger simulated webhook', 'error');
    } finally {
      setIsSimulating(false);
    }
  };

  const handleSendReply = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!selectedMessageId || !replyText.trim()) return;
    try {
      await reply({ id: selectedMessageId, replyText });
      setReplyText('');
      refetchMessages();
    } catch (e) {}
  };

  return (
    <div style={{ display: 'flex', flexDirection: 'column', height: 'calc(100vh - 2 * var(--spacing-xl))', gap: 'var(--spacing-md)' }}>
      {/* Page Header */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <div>
          <h1 style={{ fontSize: '2rem', fontWeight: 800 }}>Unified Inbox</h1>
          <p style={{ color: 'var(--text-muted)' }}>Aggregate Direct Messages and engagement across platforms</p>
        </div>
        <div style={{ display: 'flex', gap: 'var(--spacing-md)' }}>
          <Button variant="outline" size="sm" onClick={() => handleSimulateWebhook("Wow, I love the brand dashboard!")} disabled={isSimulating}>
            🚀 Simulate positive DM
          </Button>
          <Button variant="outline" size="sm" onClick={() => handleSimulateWebhook("This app is terrible, it crashes constantly.")} disabled={isSimulating}>
            ⚠️ Simulate negative DM
          </Button>
        </div>
      </div>

      {/* Inbox Layout: 3 Columns */}
      <div style={{ display: 'grid', gridTemplateColumns: '220px 1fr 1.2fr', flex: 1, gap: 'var(--spacing-lg)', minHeight: 0 }}>
        
        {/* Column 1: Filters */}
        <div className="glass-card" style={{ display: 'flex', flexDirection: 'column', gap: 'var(--spacing-md)' }}>
          <h3>Filters</h3>
          
          <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--spacing-xs)' }}>
            <label style={{ fontSize: '0.8rem', color: 'var(--text-secondary)' }}>Social Platform</label>
            <select
              value={filters.platform}
              onChange={(e) => setFilter('platform', e.target.value)}
              style={{
                backgroundColor: 'var(--bg-secondary)',
                border: '1px solid var(--border)',
                borderRadius: 'var(--radius-sm)',
                padding: '8px',
                color: 'var(--text-primary)',
                cursor: 'pointer'
              }}
            >
              <option value="">All Platforms</option>
              <option value="FACEBOOK">Facebook</option>
              <option value="INSTAGRAM">Instagram</option>
              <option value="TIKTOK">TikTok</option>
              <option value="LINKEDIN">LinkedIn</option>
              <option value="X">X (Twitter)</option>
            </select>
          </div>

          <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--spacing-xs)' }}>
            <label style={{ fontSize: '0.8rem', color: 'var(--text-secondary)' }}>Status</label>
            <select
              value={filters.status}
              onChange={(e) => setFilter('status', e.target.value)}
              style={{
                backgroundColor: 'var(--bg-secondary)',
                border: '1px solid var(--border)',
                borderRadius: 'var(--radius-sm)',
                padding: '8px',
                color: 'var(--text-primary)',
                cursor: 'pointer'
              }}
            >
              <option value="">All Statuses</option>
              <option value="UNREAD">Unread</option>
              <option value="READ">Read</option>
              <option value="ARCHIVED">Archived</option>
            </select>
          </div>
        </div>

        {/* Column 2: Message List */}
        <div className="glass-card" style={{ display: 'flex', flexDirection: 'column', padding: 0, overflow: 'hidden' }}>
          <div style={{ padding: 'var(--spacing-md)', borderBottom: '1px solid var(--border)' }}>
            <h3>Conversations</h3>
          </div>
          <div style={{ flex: 1, overflowY: 'auto', display: 'flex', flexDirection: 'column' }}>
            {isLoadingMessages ? (
              <div style={{ padding: 'var(--spacing-lg)', textAlign: 'center', color: 'var(--text-muted)' }}>Loading messages...</div>
            ) : messages.length === 0 ? (
              <div style={{ padding: 'var(--spacing-xl)', textAlign: 'center', color: 'var(--text-muted)' }}>
                <MessageSquare size={36} style={{ marginBottom: 'var(--spacing-sm)' }} />
                <p>No messages found</p>
                <span style={{ fontSize: '0.75rem' }}>Simulate a webhook event above to trigger new message ingestion!</span>
              </div>
            ) : (
              messages.map((m) => (
                <div
                  key={m.id}
                  onClick={() => selectMessage(m.id)}
                  style={{
                    padding: 'var(--spacing-md)',
                    borderBottom: '1px solid var(--border)',
                    cursor: 'pointer',
                    backgroundColor: selectedMessageId === m.id ? 'rgba(102, 252, 241, 0.05)' : m.status === 'UNREAD' ? 'rgba(255, 255, 255, 0.02)' : 'transparent',
                    borderLeft: m.status === 'UNREAD' ? '3px solid var(--primary)' : '3px solid transparent',
                    transition: 'var(--transition-smooth)'
                  }}
                >
                  <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '4px' }}>
                    <strong style={{ fontSize: '0.9rem', color: m.status === 'UNREAD' ? 'var(--text-primary)' : 'var(--text-secondary)' }}>
                      {m.senderName}
                    </strong>
                    <span style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>
                      {new Date(m.receivedAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                    </span>
                  </div>
                  <p style={{
                    fontSize: '0.8rem',
                    color: 'var(--text-muted)',
                    whiteSpace: 'nowrap',
                    overflow: 'hidden',
                    textOverflow: 'ellipsis',
                    marginBottom: 'var(--spacing-sm)'
                  }}>
                    {m.content}
                  </p>
                  <div style={{ display: 'flex', gap: '8px' }}>
                    <PlatformBadge platform={m.platform} />
                    <SentimentBadge sentiment={m.sentiment} />
                  </div>
                </div>
              ))
            )}
          </div>
        </div>

        {/* Column 3: Thread Detail */}
        <div className="glass-card" style={{ display: 'flex', flexDirection: 'column', padding: 0, overflow: 'hidden' }}>
          {selectedMsg ? (
            <div style={{ display: 'flex', flexDirection: 'column', height: '100%' }}>
              <div style={{ padding: 'var(--spacing-lg)', borderBottom: '1px solid var(--border)', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <div>
                  <h3 style={{ margin: 0 }}>{selectedMsg.senderName}</h3>
                  <span style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>Platform User ID: {selectedMsg.senderId}</span>
                </div>
                <PlatformBadge platform={selectedMsg.platform} />
              </div>

              {/* Message Thread */}
              <div style={{ flex: 1, padding: 'var(--spacing-lg)', overflowY: 'auto', display: 'flex', flexDirection: 'column', gap: 'var(--spacing-md)' }}>
                {/* Incoming Message Bubble */}
                <div style={{ alignSelf: 'flex-start', maxWidth: '80%' }}>
                  <div style={{
                    backgroundColor: 'var(--bg-secondary)',
                    padding: 'var(--spacing-md)',
                    borderRadius: '0px var(--radius-md) var(--radius-md) var(--radius-md)',
                    border: '1px solid var(--border)'
                  }}>
                    <p style={{ fontSize: '0.9rem' }}>{selectedMsg.content}</p>
                  </div>
                  <span style={{ fontSize: '0.7rem', color: 'var(--text-muted)', marginLeft: '4px' }}>
                    {new Date(selectedMsg.receivedAt).toLocaleString()}
                  </span>
                </div>
              </div>

              {/* Reply Composer */}
              <form onSubmit={handleSendReply} style={{ padding: 'var(--spacing-lg)', borderTop: '1px solid var(--border)' }}>
                <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--spacing-sm)' }}>
                  <textarea
                    rows={2}
                    value={replyText}
                    onChange={(e) => setReplyText(e.target.value)}
                    placeholder="Type your response..."
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
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                    <Button 
                      type="button" 
                      variant="outline" 
                      onClick={handleAIAssist}
                      disabled={isGeneratingReply}
                      style={{ display: 'flex', alignItems: 'center', gap: '6px' }}
                    >
                      <Sparkles size={14} style={{ color: 'var(--primary)' }} />
                      {isGeneratingReply ? 'Generating...' : 'AI Assist'}
                    </Button>
                    <Button type="submit">
                      <Send size={16} style={{ marginRight: '6px' }} /> Send Reply
                    </Button>
                  </div>
                </div>
              </form>
            </div>
          ) : (
            <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', height: '100%', color: 'var(--text-muted)', padding: 'var(--spacing-xl)' }}>
              <MessageSquare size={48} style={{ marginBottom: 'var(--spacing-md)' }} />
              <h3>No Conversation Selected</h3>
              <p style={{ textAlign: 'center', fontSize: '0.85rem' }}>Select an message thread from the conversation list to read history and reply.</p>
            </div>
          )}
        </div>

      </div>
    </div>
  );
};
export default InboxPage;
