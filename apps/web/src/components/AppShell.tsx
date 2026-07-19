import React from 'react';
import { Outlet } from 'react-router-dom';
import { Sidebar } from './Sidebar';
import { ToastContainer } from './ui/Toast';

export const AppShell: React.FC = () => {
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
