import { create } from 'zustand';

interface InboxFilters {
  platform: string;
  status: string;
}

interface InboxState {
  filters: InboxFilters;
  selectedMessageId: string | null;
  setFilter: (key: keyof InboxFilters, value: string) => void;
  selectMessage: (id: string | null) => void;
}

export const useInboxStore = create<InboxState>((set) => ({
  filters: {
    platform: '',
    status: ''
  },
  selectedMessageId: null,
  setFilter: (key, value) =>
    set((state) => ({
      filters: { ...state.filters, [key]: value }
    })),
  selectMessage: (id) => set({ selectedMessageId: id })
}));
