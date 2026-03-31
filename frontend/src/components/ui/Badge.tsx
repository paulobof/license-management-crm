import React from 'react';

type BadgeStatus = 'ATIVO' | 'INATIVO';

interface BadgeProps {
  status: BadgeStatus;
}

const badgeConfig: Record<BadgeStatus, { label: string; classes: string }> = {
  ATIVO: {
    label: 'Ativo',
    classes: 'bg-green-50 text-green-700 border border-green-200',
  },
  INATIVO: {
    label: 'Inativo',
    classes: 'bg-gray-100 text-gray-600 border border-gray-200',
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
