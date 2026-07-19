import React from 'react';
import { Badge } from './Badge';

interface SentimentBadgeProps {
  sentiment: string;
}

export const SentimentBadge: React.FC<SentimentBadgeProps> = ({ sentiment }) => {
  const s = sentiment.toUpperCase();
  let variant: 'neutral' | 'success' | 'warning' | 'error' = 'neutral';
  
  if (s === 'POSITIVE') variant = 'success';
  else if (s === 'NEGATIVE') variant = 'error';
  else if (s === 'NEUTRAL') variant = 'neutral';

  return <Badge variant={variant}>{sentiment}</Badge>;
};
