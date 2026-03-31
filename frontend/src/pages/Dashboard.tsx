import React from 'react';
import { Users, UserCheck, AlertTriangle, DollarSign } from 'lucide-react';
import { useAuth } from '../contexts/AuthContext';

interface SummaryCardProps {
  title: string;
  value: string | number;
  icon: React.ReactNode;
  iconBg: string;
  subtitle?: string;
}

const SummaryCard: React.FC<SummaryCardProps> = ({ title, value, icon, iconBg, subtitle }) => (
  <div className="bg-white border border-gray-200 rounded-xl p-5 flex items-start gap-4 shadow-sm">
    <div className={['w-11 h-11 rounded-lg flex items-center justify-center shrink-0', iconBg].join(' ')}>
      {icon}
    </div>
    <div className="min-w-0">
      <p className="text-sm text-gray-500">{title}</p>
      <p className="text-2xl font-bold text-gray-900 mt-0.5">{value}</p>
      {subtitle && <p className="text-xs text-gray-400 mt-0.5">{subtitle}</p>}
    </div>
  </div>
);

const Dashboard: React.FC = () => {
  const { user } = useAuth();

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-xl font-bold text-gray-900">
          Bem-vindo, {user?.nome ?? 'Usuario'}
        </h1>
        <p className="text-sm text-gray-500 mt-0.5">
          Visao geral do sistema de gestao de clientes
        </p>
      </div>

      <div className="grid grid-cols-1 sm:grid-cols-2 xl:grid-cols-4 gap-4">
        <SummaryCard
          title="Total de Clientes"
          value="—"
          icon={<Users size={20} className="text-red-600" />}
          iconBg="bg-red-50"
          subtitle="Carregando..."
        />
        <SummaryCard
          title="Clientes Ativos"
          value="—"
          icon={<UserCheck size={20} className="text-green-600" />}
          iconBg="bg-green-50"
          subtitle="Carregando..."
        />
        <SummaryCard
          title="Documentos a Vencer"
          value="Em breve"
          icon={<AlertTriangle size={20} className="text-amber-600" />}
          iconBg="bg-amber-50"
          subtitle="Funcionalidade em desenvolvimento"
        />
        <SummaryCard
          title="Financeiro"
          value="Em breve"
          icon={<DollarSign size={20} className="text-purple-600" />}
          iconBg="bg-purple-50"
          subtitle="Funcionalidade em desenvolvimento"
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
