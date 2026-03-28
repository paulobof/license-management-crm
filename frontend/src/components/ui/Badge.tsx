import React from 'react';

type BadgeStatus = 'ATIVO' | 'INATIVO';

interface BadgeProps {
  status: BadgeStatus;
}

const badgeConfig: Record<BadgeStatus, { label: string; classes: string }> = {
  ATIVO: {
    label: 'Ativo',
    classes: 'bg-green-900/50 text-green-400 border border-green-700/50',
  },
  INATIVO: {
    label: 'Inativo',
    classes: 'bg-red-900/50 text-red-400 border border-red-700/50',
  },
};

const Badge: React.FC<BadgeProps> = ({ status }) => {
  const config = badgeConfig[status] ?? badgeConfig.INATIVO;
  return (
    <span
      className={[
        'inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium',
        config.classes,
      ].join(' ')}
    >
      {config.label}
    </span>
  );
};

export default Badge;
