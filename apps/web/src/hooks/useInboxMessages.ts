import { useQuery, useMutation } from '@tanstack/react-query';
import { api } from '../lib/api';
import { useInboxStore } from '../stores/useInboxStore';
import { useWorkspaceStore } from '../stores/useWorkspaceStore';
import { useToastStore } from '../stores/useToastStore';
import { queryClient } from '../lib/queryClient';

export interface InboxMessage {
  id: string;
  workspaceId: string;
  platform: string;
  platformMessageId: string;
  senderId: string;
  senderName: string;
  content: string;
  sentiment: string;
  status: string;
  assignedTo: string | null;
  labels: string[];
  receivedAt: string;
}

export const useInboxMessages = () => {
  const currentWorkspace = useWorkspaceStore((state) => state.currentWorkspace);
  const { platform, status } = useInboxStore((state) => state.filters);
  const addToast = useToastStore((state) => state.addToast);

  const workspaceId = currentWorkspace?.id;

  const { data: messages = [], isLoading, refetch } = useQuery({
    queryKey: ['inbox', workspaceId, platform, status],
    queryFn: async () => {
      if (!workspaceId) return [];
      const res = await api.get(`/workspaces/${workspaceId}/inbox`, {
        params: { platform, status, size: 50 }
      });
      return (res.data.content || []) as InboxMessage[];
    },
    enabled: !!workspaceId,
    refetchInterval: 30000
  });

  const markReadMutation = useMutation({
    mutationFn: async (id: string) => {
      const res = await api.patch(`/inbox/${id}/read`);
      return res.data;
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['inbox'] });
    }
  });

  const assignMutation = useMutation({
    mutationFn: async ({ id, userId }: { id: string; userId: string }) => {
      const res = await api.patch(`/inbox/${id}/assign`, { userId });
      return res.data;
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['inbox'] });
      addToast('Message assigned successfully', 'success');
    }
  });

  const replyMutation = useMutation({
    mutationFn: async ({ id, replyText }: { id: string; replyText: string }) => {
      await api.post(`/inbox/${id}/reply`, { replyText });
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['inbox'] });
      addToast('Reply sent successfully', 'success');
    },
    onError: () => {
      addToast('Failed to send reply', 'error');
    }
  });

  return {
    messages,
    isLoadingMessages: isLoading,
    refetchMessages: refetch,
    markAsRead: markReadMutation.mutate,
    assignUser: assignMutation.mutate,
    reply: replyMutation.mutateAsync,
    isReplying: replyMutation.isPending
  };
};
