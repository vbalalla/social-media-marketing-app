import { create } from 'zustand';

export interface User {
  id: string;
  email: string;
  fullName: string;
  role: string;
}

interface AuthState {
  user: User | null;
  accessToken: string | null;
  setAuth: (user: User, accessToken: string) => void;
  clearAuth: () => void;
}

const storedUser = localStorage.getItem('serendia_user');
const storedToken = localStorage.getItem('serendia_token');

export const useAuthStore = create<AuthState>((set) => ({
  user: storedUser ? JSON.parse(storedUser) : null,
  accessToken: storedToken || null,
  setAuth: (user, accessToken) => {
    localStorage.setItem('serendia_user', JSON.stringify(user));
    localStorage.setItem('serendia_token', accessToken);
    set({ user, accessToken });
  },
  clearAuth: () => {
    localStorage.removeItem('serendia_user');
    localStorage.removeItem('serendia_token');
    set({ user: null, accessToken: null });
  }
}));
