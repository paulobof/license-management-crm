import React from 'react';

interface SummaryCardProps {
  title: string;
  value: string | number;
  icon: React.ReactNode;
  iconBg: string;
  borderColor: string;
  subtitle?: string;
  onClick?: () => void;
}

const SummaryCard: React.FC<SummaryCardProps> = ({
  title,
  value,
  icon,
  iconBg,
  borderColor,
  subtitle,
  onClick,
}) => (
  <div
    className={[
      'bg-white rounded-xl p-5 flex items-start gap-4 shadow-sm border-l-4',
      borderColor,
      onClick ? 'cursor-pointer hover:shadow-md transition-shadow duration-150' : '',
    ].join(' ')}
    onClick={onClick}
    role={onClick ? 'button' : undefined}
    tabIndex={onClick ? 0 : undefined}
    onKeyDown={
      onClick
        ? (e) => {
            if (e.key === 'Enter' || e.key === ' ') {
              e.preventDefault();
              onClick();
            }
          }
        : undefined
    }
  >
    <div
      className={['w-11 h-11 rounded-xl flex items-center justify-center shrink-0', iconBg].join(
        ' '
      )}
    >
      {icon}
    </div>
    <div className="min-w-0">
      <p className="text-sm text-gray-500 font-medium">{title}</p>
      <p className="text-xl font-bold text-gray-900 mt-0.5">{value}</p>
      {subtitle && <p className="text-xs text-gray-400 mt-0.5">{subtitle}</p>}
    </div>
  </div>
);

export default SummaryCard;
