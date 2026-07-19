import React, { useEffect } from 'react';
import { Outlet } from 'react-router-dom';
import { Sidebar } from './Sidebar';
import { ToastContainer } from './ui/Toast';
import { initInboxSocket } from '../lib/inboxSocket';

export const AppShell: React.FC = () => {
  useEffect(() => {
    const cleanup = initInboxSocket();
    return () => {
      cleanup();
    };
  }, []);

  return (
    <div style={{ display: 'flex', minHeight: '100vh' }}>
      <Sidebar />
      <main style={{ flex: 1, padding: 'var(--spacing-xl)', overflowY: 'auto', maxHeight: '100vh' }}>
        <Outlet />
      </main>
      <ToastContainer />
    </div>
  );
};
