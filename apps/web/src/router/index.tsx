import { createBrowserRouter, Navigate } from 'react-router-dom';
import LoginPage from '../pages/LoginPage';
import RegisterPage from '../pages/RegisterPage';
import DashboardPage from '../pages/DashboardPage';
import InboxPage from '../pages/InboxPage';
import SchedulerPage from '../pages/SchedulerPage';
import CampaignDashboardPage from '../pages/CampaignDashboardPage';
import AnalyticsPage from '../pages/AnalyticsPage';
import AIToolsPage from '../pages/AIToolsPage';
import SettingsPage from '../pages/SettingsPage';
import OAuthCallbackPage from '../pages/OAuthCallbackPage';
import SocialSetupPage from '../pages/SocialSetupPage';
import { ProtectedRoute } from '../components/ProtectedRoute';
import { AppShell } from '../components/AppShell';

export const router = createBrowserRouter([
  {
    path: '/login',
    element: <LoginPage />
  },
  {
    path: '/register',
    element: <RegisterPage />
  },
  {
    path: '/setup',
    element: <SocialSetupPage />
  },
  {
    path: '/oauth/callback',
    element: <OAuthCallbackPage />
  },
  {
    path: '/',
    element: (
      <ProtectedRoute>
        <AppShell />
      </ProtectedRoute>
    ),
    children: [
      {
        path: '',
        element: <Navigate to="/dashboard" replace />
      },
      {
        path: 'dashboard',
        element: <DashboardPage />
      },
      {
        path: 'inbox',
        element: <InboxPage />
      },
      {
        path: 'scheduler',
        element: <SchedulerPage />
      },
      {
        path: 'campaigns',
        element: <CampaignDashboardPage />
      },
      {
        path: 'analytics',
        element: <AnalyticsPage />
      },
      {
        path: 'ai-tools',
        element: <AIToolsPage />
      },
      {
        path: 'settings',
        element: <SettingsPage />
      }
    ]
  },
  {
    path: '*',
    element: <Navigate to="/dashboard" replace />
  }
]);
