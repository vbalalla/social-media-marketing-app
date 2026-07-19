import React, { useState } from 'react';
import { useWorkspaces } from '../hooks/useWorkspaces';
import { ChevronDown, Plus } from 'lucide-react';

export const WorkspaceSwitcher: React.FC = () => {
  const { workspaces, currentWorkspace, selectWorkspace, createWorkspace } = useWorkspaces();
  const [isOpen, setIsOpen] = useState(false);

  const handleCreate = async () => {
    const name = prompt('Enter new workspace name:');
    if (name && name.trim()) {
      try {
        await createWorkspace(name.trim());
      } catch (e) {
        // Handled by hook error notification
      }
    }
  };

  return (
    <div style={{ position: 'relative', width: '100%' }}>
      <button
        onClick={() => setIsOpen(!isOpen)}
        style={{
          display: 'flex',
          alignItems: 'center',
          width: '100%',
          padding: '10px 12px',
          backgroundColor: 'var(--bg-secondary)',
          border: '1px solid var(--border)',
          borderRadius: 'var(--radius-sm)',
          cursor: 'pointer',
          color: 'var(--text-primary)',
          fontSize: '0.9rem',
          fontWeight: 600,
          textAlign: 'left'
        }}
      >
        <div style={{ display: 'flex', flexDirection: 'column', flex: 1, overflow: 'hidden' }}>
          <span style={{ fontSize: '0.7rem', color: 'var(--text-muted)', fontWeight: 500, textTransform: 'uppercase', letterSpacing: '0.05em' }}>
            Active Workspace
          </span>
          <span style={{ textOverflow: 'ellipsis', overflow: 'hidden', whiteSpace: 'nowrap', color: 'var(--primary)' }}>
            {currentWorkspace ? currentWorkspace.name : 'Select Workspace'}
          </span>
        </div>
        <ChevronDown size={16} style={{ marginLeft: '8px', color: 'var(--text-muted)' }} />
      </button>

      {isOpen && (
        <div style={{
          position: 'absolute',
          top: '105%',
          left: 0,
          right: 0,
          backgroundColor: 'var(--bg-secondary)',
          border: '1px solid var(--border)',
          borderRadius: 'var(--radius-sm)',
          boxShadow: 'var(--shadow-lg)',
          zIndex: 50,
          maxHeight: '200px',
          overflowY: 'auto'
        }}>
          {workspaces.map((w) => (
            <button
              key={w.id}
              onClick={() => {
                selectWorkspace(w);
                setIsOpen(false);
              }}
              style={{
                display: 'block',
                width: '100%',
                padding: '10px 12px',
                textAlign: 'left',
                borderBottom: '1px solid var(--border)',
                backgroundColor: currentWorkspace?.id === w.id ? 'rgba(102, 252, 241, 0.05)' : 'transparent',
                color: currentWorkspace?.id === w.id ? 'var(--primary)' : 'var(--text-secondary)',
                cursor: 'pointer'
              }}
            >
              {w.name}
            </button>
          ))}
          <button
            onClick={() => {
              handleCreate();
              setIsOpen(false);
            }}
            style={{
              display: 'flex',
              alignItems: 'center',
              gap: '8px',
              width: '100%',
              padding: '10px 12px',
              textAlign: 'left',
              color: 'var(--primary)',
              fontWeight: 600,
              cursor: 'pointer'
            }}
          >
            <Plus size={16} />
            New Workspace
          </button>
        </div>
      )}
    </div>
  );
};
