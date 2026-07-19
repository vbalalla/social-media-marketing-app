import { useQuery, useMutation } from '@tanstack/react-query';
import { api } from '../lib/api';
import { useWorkspaceStore, Workspace } from '../stores/useWorkspaceStore';
import { useToastStore } from '../stores/useToastStore';
import { queryClient } from '../lib/queryClient';
import { useEffect } from 'react';

export const useWorkspaces = () => {
  const currentWorkspace = useWorkspaceStore((state) => state.currentWorkspace);
  const setWorkspace = useWorkspaceStore((state) => state.setWorkspace);
  const addToast = useToastStore((state) => state.addToast);

  const { data: workspaces = [], isLoading } = useQuery({
    queryKey: ['workspaces'],
    queryFn: async () => {
      const res = await api.get('/workspaces');
      return res.data as Workspace[];
    }
  });

  useEffect(() => {
    if (workspaces.length > 0 && !currentWorkspace) {
      setWorkspace(workspaces[0]);
    }
  }, [workspaces, currentWorkspace, setWorkspace]);

  const createWorkspaceMutation = useMutation({
    mutationFn: async (name: string) => {
      const res = await api.post('/workspaces', { name });
      return res.data as Workspace;
    },
    onSuccess: (newWorkspace) => {
      queryClient.invalidateQueries({ queryKey: ['workspaces'] });
      setWorkspace(newWorkspace);
      addToast('Workspace "' + newWorkspace.name + '" created successfully', 'success');
    },
    onError: () => {
      addToast('Failed to create workspace', 'error');
    }
  });

  return {
    workspaces,
    currentWorkspace,
    selectWorkspace: setWorkspace,
    isLoadingWorkspaces: isLoading,
    createWorkspace: createWorkspaceMutation.mutateAsync,
    isCreatingWorkspace: createWorkspaceMutation.isPending
  };
};
