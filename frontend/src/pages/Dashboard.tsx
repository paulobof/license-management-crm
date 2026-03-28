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
  <div className="bg-zinc-800 border border-zinc-700 rounded-xl p-5 flex items-start gap-4">
    <div className={['w-11 h-11 rounded-lg flex items-center justify-center shrink-0', iconBg].join(' ')}>
      {icon}
    </div>
    <div className="min-w-0">
      <p className="text-sm text-zinc-400">{title}</p>
      <p className="text-2xl font-bold text-zinc-100 mt-0.5">{value}</p>
      {subtitle && <p className="text-xs text-zinc-500 mt-0.5">{subtitle}</p>}
    </div>
  </div>
);

const Dashboard: React.FC = () => {
  const { user } = useAuth();

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-xl font-bold text-zinc-100">
          Bem-vindo, {user?.nome ?? 'Usuario'}
        </h1>
        <p className="text-sm text-zinc-500 mt-0.5">
          Visao geral do sistema de gestao de clientes
        </p>
      </div>

      <div className="grid grid-cols-1 sm:grid-cols-2 xl:grid-cols-4 gap-4">
        <SummaryCard
          title="Total de Clientes"
          value="—"
          icon={<Users size={20} className="text-blue-400" />}
          iconBg="bg-blue-600/20"
          subtitle="Carregando..."
        />
        <SummaryCard
          title="Clientes Ativos"
          value="—"
          icon={<UserCheck size={20} className="text-green-400" />}
          iconBg="bg-green-600/20"
          subtitle="Carregando..."
        />
        <SummaryCard
          title="Documentos a Vencer"
          value="Em breve"
          icon={<AlertTriangle size={20} className="text-yellow-400" />}
          iconBg="bg-yellow-600/20"
          subtitle="Funcionalidade em desenvolvimento"
        />
        <SummaryCard
          title="Financeiro"
          value="Em breve"
          icon={<DollarSign size={20} className="text-purple-400" />}
          iconBg="bg-purple-600/20"
          subtitle="Funcionalidade em desenvolvimento"
        />
      </div>

      <div className="bg-zinc-800 border border-zinc-700 rounded-xl p-6">
        <h2 className="text-base font-semibold text-zinc-100 mb-1">Atividade Recente</h2>
        <p className="text-sm text-zinc-500">
          Nenhuma atividade recente para exibir.
        </p>
      </div>
    </div>
  );
};

export default Dashboard;
