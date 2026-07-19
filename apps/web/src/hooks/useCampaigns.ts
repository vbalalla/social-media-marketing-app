import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { api } from '../lib/api';
import { useWorkspaceStore } from '../stores/useWorkspaceStore';
import { useToastStore } from '../stores/useToastStore';

export interface Campaign {
  id: string; // config id or campaignId_platform
  campaignId: string;
  name: string;
  platform: 'META' | 'TIKTOK';
  status: 'ACTIVE' | 'PAUSED' | 'COMPLETED';
  spend: number;
  cpa: number;
  impressions: number;
  clicks: number;
  dailyBudget: number;
}

export const useCampaigns = () => {
  const currentWorkspace = useWorkspaceStore((state) => state.currentWorkspace);
  const workspaceId = currentWorkspace?.id;

  const { data: campaigns = [], isLoading, refetch } = useQuery({
    queryKey: ['campaigns', workspaceId],
    queryFn: async () => {
      if (!workspaceId) return [];
      const res = await api.get(`/ad/workspaces/${workspaceId}/campaigns`);
      const list: Campaign[] = [];
      res.data.forEach((camp: any) => {
        camp.platformConfigs.forEach((conf: any) => {
          list.push({
            id: conf.id,
            campaignId: camp.id,
            name: camp.name,
            platform: conf.platform,
            status: camp.status,
            spend: Number(conf.spendUsd),
            cpa: Number(conf.cpaUsd),
            impressions: Number(conf.impressions),
            clicks: Number(conf.clicks),
            dailyBudget: Number(conf.dailyBudget),
          });
        });
      });
      return list;
    },
    enabled: !!workspaceId,
  });

  return {
    campaigns,
    isLoadingCampaigns: isLoading,
    refetchCampaigns: refetch,
  };
};

export const useToggleCampaignStatus = () => {
  const queryClient = useQueryClient();
  const addToast = useToastStore((state) => state.addToast);

  return useMutation({
    mutationFn: async ({ campaignId, currentStatus }: { campaignId: string; currentStatus: string }) => {
      const nextStatus = currentStatus === 'ACTIVE' ? 'PAUSED' : 'ACTIVE';
      const res = await api.patch(`/ad/campaigns/${campaignId}/status`, { status: nextStatus });
      return res.data;
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['campaigns'] });
      addToast('Campaign status toggled successfully!', 'success');
    },
    onError: (err: any) => {
      const msg = err.response?.data?.message || 'Failed to toggle campaign status';
      addToast(msg, 'error');
    }
  });
};

export const useCreateCampaign = () => {
  const queryClient = useQueryClient();
  const currentWorkspace = useWorkspaceStore((state) => state.currentWorkspace);
  const addToast = useToastStore((state) => state.addToast);

  return useMutation({
    mutationFn: async (payload: { name: string; dailyBudget: number; platforms: string[] }) => {
      if (!currentWorkspace) throw new Error('No active workspace selected');
      const res = await api.post(`/ad/workspaces/${currentWorkspace.id}/campaigns`, payload);
      return res.data;
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['campaigns'] });
      addToast('Campaign created successfully!', 'success');
    },
    onError: (err: any) => {
      const msg = err.response?.data?.message || 'Failed to create campaign';
      addToast(msg, 'error');
    }
  });
};
