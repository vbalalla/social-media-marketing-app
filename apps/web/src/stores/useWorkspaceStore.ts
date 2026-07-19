import { create } from 'zustand';

export interface Workspace {
  id: string;
  name: string;
  plan: string;
  ownerId: string;
}

interface WorkspaceState {
  currentWorkspace: Workspace | null;
  setWorkspace: (workspace: Workspace | null) => void;
}

const storedWorkspace = localStorage.getItem('serendia_current_workspace');

export const useWorkspaceStore = create<WorkspaceState>((set) => ({
  currentWorkspace: storedWorkspace ? JSON.parse(storedWorkspace) : null,
  setWorkspace: (workspace) => {
    if (workspace) {
      localStorage.setItem('serendia_current_workspace', JSON.stringify(workspace));
    } else {
      localStorage.removeItem('serendia_current_workspace');
    }
    set({ currentWorkspace: workspace });
  }
}));
