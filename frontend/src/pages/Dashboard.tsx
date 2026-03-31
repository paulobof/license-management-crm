import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { Users, UserCheck, AlertTriangle, FileX } from 'lucide-react';
import { useAuth } from '../contexts/AuthContext';
import { getDashboardSummary } from '../api/documentos';
import type { DashboardSummary } from '../types';

interface SummaryCardProps {
  title: string;
  value: string | number;
  icon: React.ReactNode;
  iconBg: string;
  borderColor: string;
  subtitle?: string;
  onClick?: () => void;
}

const SummaryCard: React.FC<SummaryCardProps> = ({ title, value, icon, iconBg, borderColor, subtitle, onClick }) => (
  <div
    className={[
      'bg-white rounded-xl p-5 flex items-start gap-4 shadow-sm border-l-4',
      borderColor,
      onClick ? 'cursor-pointer hover:shadow-md transition-shadow duration-150' : '',
    ].join(' ')}
    onClick={onClick}
  >
    <div className={['w-11 h-11 rounded-xl flex items-center justify-center shrink-0', iconBg].join(' ')}>
      {icon}
    </div>
    <div className="min-w-0">
      <p className="text-sm text-gray-500 font-medium">{title}</p>
      <p className="text-2xl font-bold text-gray-900 mt-0.5">{value}</p>
      {subtitle && <p className="text-xs text-gray-400 mt-0.5">{subtitle}</p>}
    </div>
  </div>
);

const Dashboard: React.FC = () => {
  const { user } = useAuth();
  const navigate = useNavigate();
  const [summary, setSummary] = useState<DashboardSummary | null>(null);
  const [loadingSummary, setLoadingSummary] = useState(true);

  useEffect(() => {
    getDashboardSummary()
      .then((data) => setSummary(data))
      .catch(() => setSummary(null))
      .finally(() => setLoadingSummary(false));
  }, []);

  const displayValue = (val: number | undefined): string | number => {
    if (loadingSummary) return '...';
    if (val === undefined) return '—';
    return val;
  };

  return (
    <div className="space-y-6">
      <div className="bg-gradient-to-r from-red-600 to-orange-500 rounded-2xl p-6 text-white shadow-lg shadow-red-600/10">
        <h1 className="text-xl font-bold">
          Bem-vindo, {user?.nome ?? 'Usuario'}
        </h1>
        <p className="text-red-100 text-sm mt-1">
          Visão geral do sistema de gestão de clientes e licenças
        </p>
      </div>

      <div className="grid grid-cols-1 sm:grid-cols-2 xl:grid-cols-4 gap-4">
        <SummaryCard
          title="Total de Clientes"
          value={displayValue(summary?.totalClientes)}
          icon={<Users size={20} className="text-red-600" />}
          iconBg="bg-red-100"
          borderColor="border-red-500"
          subtitle="cadastrados no sistema"
        />
        <SummaryCard
          title="Clientes Ativos"
          value={displayValue(summary?.clientesAtivos)}
          icon={<UserCheck size={20} className="text-emerald-600" />}
          iconBg="bg-emerald-100"
          borderColor="border-emerald-500"
          subtitle="com status ativo"
        />
        <SummaryCard
          title="Docs a Vencer"
          value={displayValue(summary?.documentosAVencer)}
          icon={<AlertTriangle size={20} className="text-amber-600" />}
          iconBg="bg-amber-100"
          borderColor="border-amber-500"
          subtitle="nos proximos 30 dias"
          onClick={() => navigate('/documentos?status=A_VENCER')}
        />
        <SummaryCard
          title="Docs Vencidos"
          value={displayValue(summary?.documentosVencidos)}
          icon={<FileX size={20} className="text-rose-600" />}
          iconBg="bg-rose-100"
          borderColor="border-rose-500"
          subtitle="com validade expirada"
          onClick={() => navigate('/documentos?status=VENCIDO')}
        />
      </div>

      <div className="bg-white border border-gray-200 rounded-xl p-6 shadow-sm">
        <h2 className="text-base font-semibold text-gray-900 mb-1">Atividade Recente</h2>
        <p className="text-sm text-gray-500">
          Nenhuma atividade recente para exibir.
        </p>
      </div>
    </div>
  );
};

export default Dashboard;
