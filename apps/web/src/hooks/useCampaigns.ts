import { useQuery } from '@tanstack/react-query';

export interface Campaign {
  id: string;
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
  const { data: campaigns = [], isLoading } = useQuery({
    queryKey: ['campaigns'],
    queryFn: async () => {
      // Mock data for Milestone 3 presentation of high fidelity ad metrics
      return [
        {
          id: 'c1',
          name: 'Summer Apparel Promo - US',
          platform: 'META',
          status: 'ACTIVE',
          spend: 12450.50,
          cpa: 4.25,
          impressions: 485000,
          clicks: 24250,
          dailyBudget: 500.00
        },
        {
          id: 'c2',
          name: 'Flash Sale Video - GenZ Focus',
          platform: 'TIKTOK',
          status: 'ACTIVE',
          spend: 8900.00,
          cpa: 3.10,
          impressions: 890000,
          clicks: 44500,
          dailyBudget: 400.00
        },
        {
          id: 'c3',
          name: 'Brand Awareness Retargeting',
          platform: 'META',
          status: 'PAUSED',
          spend: 3450.00,
          cpa: 6.80,
          impressions: 120000,
          clicks: 5800,
          dailyBudget: 150.00
        }
      ] as Campaign[];
    }
  });

  return {
    campaigns,
    isLoadingCampaigns: isLoading
  };
};
