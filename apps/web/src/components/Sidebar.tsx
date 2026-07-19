import React from 'react';
import { NavLink } from 'react-router-dom';
import { 
  LayoutDashboard, 
  MessageSquare, 
  Calendar, 
  TrendingUp, 
  BarChart2,
  Sparkles, 
  Settings, 
  LogOut 
} from 'lucide-react';
import { useAuth } from '../hooks/useAuth';
import { useAuthStore } from '../stores/useAuthStore';
import { WorkspaceSwitcher } from './WorkspaceSwitcher';
import { Avatar } from './ui/Avatar';

export const Sidebar: React.FC = () => {
  const { logout } = useAuth();
  const user = useAuthStore((state) => state.user);

  const menuItems = [
    { name: 'Dashboard', path: '/dashboard', icon: <LayoutDashboard size={20} /> },
    { name: 'Unified Inbox', path: '/inbox', icon: <MessageSquare size={20} /> },
    { name: 'Content Calendar', path: '/scheduler', icon: <Calendar size={20} /> },
    { name: 'Ad Campaigns', path: '/campaigns', icon: <TrendingUp size={20} /> },
    { name: 'Analytics', path: '/analytics', icon: <BarChart2 size={20} /> },
    { name: 'AI Tools', path: '/ai-tools', icon: <Sparkles size={20} /> },
    { name: 'Settings', path: '/settings', icon: <Settings size={20} /> },
  ];

  return (
    <aside style={{
      width: '260px',
      backgroundColor: 'var(--bg-tertiary)',
      borderRight: '1px solid var(--border)',
      display: 'flex',
      flexDirection: 'column',
      height: '100vh',
      padding: 'var(--spacing-lg)'
    }}>
      {/* Brand Header */}
      <div style={{ marginBottom: 'var(--spacing-xl)', display: 'flex', alignItems: 'center', gap: 'var(--spacing-sm)' }}>
        <div style={{
          width: '32px',
          height: '32px',
          borderRadius: 'var(--radius-sm)',
          background: 'linear-gradient(135deg, var(--primary) 0%, var(--secondary) 100%)'
        }} />
        <h2 style={{ fontSize: '1.25rem', margin: 0, fontWeight: 800, background: 'linear-gradient(90deg, #fff 0%, #aaa 100%)', WebkitBackgroundClip: 'text', WebkitTextFillColor: 'transparent' }}>
          SERENDIA
        </h2>
      </div>

      {/* Tenant Workspace Switcher */}
      <WorkspaceSwitcher />

      {/* Navigation Links */}
      <nav style={{ flex: 1, display: 'flex', flexDirection: 'column', gap: 'var(--spacing-xs)', marginTop: 'var(--spacing-lg)' }}>
        {menuItems.map((item) => (
          <NavLink
            key={item.path}
            to={item.path}
            style={({ isActive }) => ({
              display: 'flex',
              alignItems: 'center',
              gap: 'var(--spacing-md)',
              padding: '12px var(--spacing-md)',
              borderRadius: 'var(--radius-sm)',
              color: isActive ? 'var(--primary)' : 'var(--text-secondary)',
              backgroundColor: isActive ? 'rgba(102, 252, 241, 0.05)' : 'transparent',
              transition: 'var(--transition-smooth)',
              borderLeft: isActive ? '3px solid var(--primary)' : '3px solid transparent'
            })}
          >
            {item.icon}
            <span style={{ fontWeight: 500 }}>{item.name}</span>
          </NavLink>
        ))}
      </nav>

      {/* User Info & Footer */}
      <div style={{
        borderTop: '1px solid var(--border)',
        paddingTop: 'var(--spacing-md)',
        display: 'flex',
        flexDirection: 'column',
        gap: 'var(--spacing-md)'
      }}>
        {user && (
          <div style={{ display: 'flex', alignItems: 'center', gap: 'var(--spacing-md)' }}>
            <Avatar name={user.fullName} />
            <div style={{ display: 'flex', flexDirection: 'column', overflow: 'hidden' }}>
              <span style={{ fontWeight: 600, fontSize: '0.9rem', color: 'var(--text-primary)', whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }}>
                {user.fullName}
              </span>
              <span style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>
                {user.role}
              </span>
            </div>
          </div>
        )}
        <button
          onClick={() => logout()}
          style={{
            display: 'flex',
            alignItems: 'center',
            gap: 'var(--spacing-md)',
            padding: '10px var(--spacing-md)',
            color: 'var(--error)',
            borderRadius: 'var(--radius-sm)',
            cursor: 'pointer',
            width: '100%',
            textAlign: 'left',
            transition: 'var(--transition-smooth)'
          }}
          className="btn-logout"
        >
          <LogOut size={20} />
          <span style={{ fontWeight: 500 }}>Logout</span>
        </button>
      </div>
    </aside>
  );
};
