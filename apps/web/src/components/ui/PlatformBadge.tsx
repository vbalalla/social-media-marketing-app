import React from 'react';

interface PlatformBadgeProps {
  platform: string;
}

export const PlatformBadge: React.FC<PlatformBadgeProps> = ({ platform }) => {
  const p = platform.toUpperCase();
  let style = { background: '#1f2833', color: '#c5c6c7' };
  
  if (p === 'FACEBOOK') style = { background: 'rgba(24, 119, 242, 0.15)', color: '#1877F2' };
  else if (p === 'INSTAGRAM') style = { background: 'rgba(225, 48, 108, 0.15)', color: '#E1306C' };
  else if (p === 'TIKTOK') style = { background: 'rgba(255, 255, 255, 0.08)', color: '#ffffff' };
  else if (p === 'LINKEDIN') style = { background: 'rgba(10, 102, 194, 0.15)', color: '#0A66C2' };
  else if (p === 'X' || p === 'TWITTER') style = { background: 'rgba(29, 161, 242, 0.15)', color: '#1DA1F2' };

  return (
    <span
      className="badge"
      style={{
        ...style,
        border: `1px solid ${style.color}33`,
        textTransform: 'uppercase',
        fontSize: '0.7rem'
      }}
    >
      {platform}
    </span>
  );
};
