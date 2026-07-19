import { useMutation } from '@tanstack/react-query';
import { api } from '../lib/api';
import { useAuthStore, User } from '../stores/useAuthStore';
import { useToastStore } from '../stores/useToastStore';

export const useAuth = () => {
  const setAuth = useAuthStore((state) => state.setAuth);
  const clearAuth = useAuthStore((state) => state.clearAuth);
  const addToast = useToastStore((state) => state.addToast);

  const loginMutation = useMutation({
    mutationFn: async (credentials: { email: string; password: string }) => {
      const res = await api.post('/auth/login', credentials);
      return res.data;
    },
    onSuccess: (data) => {
      const user: User = {
        id: data.userId,
        email: data.email,
        fullName: data.fullName,
        role: data.role,
      };
      setAuth(user, data.accessToken);
      addToast('Welcome back, ' + user.fullName + '!', 'success');
    },
    onError: (err: any) => {
      const msg = err.response?.data?.message || 'Login failed';
      addToast(msg, 'error');
    },
  });

  const registerMutation = useMutation({
    mutationFn: async (userData: { email: string; password: string; fullName: string }) => {
      const res = await api.post('/auth/register', userData);
      return res.data;
    },
    onSuccess: () => {
      addToast('Registration successful! You can now log in.', 'success');
    },
    onError: (err: any) => {
      const msg = err.response?.data?.message || 'Registration failed';
      addToast(msg, 'error');
    },
  });

  const logoutMutation = useMutation({
    mutationFn: async () => {
      await api.post('/auth/logout');
    },
    onSuccess: () => {
      clearAuth();
      addToast('Logged out successfully', 'success');
    },
    onError: () => {
      clearAuth();
    },
  });

  return {
    login: loginMutation.mutateAsync,
    isLoggingIn: loginMutation.isPending,
    register: registerMutation.mutateAsync,
    isRegistering: registerMutation.isPending,
    logout: logoutMutation.mutate,
  };
};
