import React, { useState, useEffect, useCallback } from 'react';
import { Link } from 'react-router-dom';
import { Settings, FileText, DollarSign, CheckCircle, BellOff, Send } from 'lucide-react';
import type { AlertaPendente } from '../../types';
import * as alertasApi from '../../api/alertas';
import { useAuth } from '../../contexts/AuthContext';
import Button from '../../components/ui/Button';

import { formatDate } from '../../utils/formatDate';

const AlertaCard: React.FC<{
  alerta: AlertaPendente;
  onSnooze: (id: number) => void;
  onEnviar: (id: number) => void;
  loadingSnooze: boolean;
  loadingEnviar: boolean;
}> = ({ alerta, onSnooze, onEnviar, loadingSnooze, loadingEnviar }) => {
  const isVencido = alerta.status === 'VENCIDO';

  return (
    <div
      className={[
        'bg-white border rounded-xl p-4 flex items-start gap-4 shadow-sm',
        isVencido ? 'border-red-200' : 'border-yellow-200',
      ].join(' ')}
    >
      <div
        className={[
          'w-10 h-10 rounded-xl flex items-center justify-center shrink-0',
          isVencido ? 'bg-red-100' : 'bg-yellow-100',
        ].join(' ')}
      >
        {alerta.tipo === 'DOCUMENTO' ? (
          <FileText size={18} className={isVencido ? 'text-red-600' : 'text-yellow-600'} />
        ) : (
          <DollarSign size={18} className={isVencido ? 'text-red-600' : 'text-yellow-600'} />
        )}
      </div>

      <div className="flex-1 min-w-0">
        <p className="text-sm font-semibold text-gray-900 truncate">{alerta.nome}</p>
        <p className="text-xs text-gray-500 mt-0.5">{alerta.clienteNome}</p>
        <div className="flex items-center gap-3 mt-1.5 flex-wrap">
          <span className="text-xs text-gray-500">
            Vencimento: {formatDate(alerta.dataVencimento)}
          </span>
          <span
            className={[
              'text-xs font-medium px-2 py-0.5 rounded-full',
              isVencido
                ? 'bg-red-100 text-red-700'
                : 'bg-yellow-100 text-yellow-700',
            ].join(' ')}
          >
            {isVencido
              ? `Vencido ha ${Math.abs(alerta.diasRestantes)} dia${Math.abs(alerta.diasRestantes) !== 1 ? 's' : ''}`
              : `Vence em ${alerta.diasRestantes} dia${alerta.diasRestantes !== 1 ? 's' : ''}`}
          </span>
        </div>
      </div>

      <div className="flex items-center gap-2 shrink-0">
        <Button
          variant="secondary"
          size="sm"
          loading={loadingSnooze}
          onClick={() => onSnooze(alerta.id)}
          title="Adiar 7 dias"
        >
          <BellOff size={14} />
          Snooze 7 dias
        </Button>
        <Button
          variant="primary"
          size="sm"
          loading={loadingEnviar}
          onClick={() => onEnviar(alerta.id)}
          title="Enviar alerta"
        >
          <Send size={14} />
          Enviar Alerta
        </Button>
      </div>
    </div>
  );
};

const AlertaList: React.FC = () => {
  const { isAdmin } = useAuth();
  const [alertas, setAlertas] = useState<AlertaPendente[]>([]);
  const [loading, setLoading] = useState(true);
  const [loadingSnooze, setLoadingSnooze] = useState<Record<number, boolean>>({});
  const [loadingEnviar, setLoadingEnviar] = useState<Record<number, boolean>>({});

  const fetchAlertas = useCallback(async () => {
    setLoading(true);
    try {
      const result = await alertasApi.getPendentes();
      setAlertas(result);
    } catch {
      setAlertas([]);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchAlertas();
  }, [fetchAlertas]);

  const handleSnooze = async (id: number) => {
    setLoadingSnooze((prev) => ({ ...prev, [id]: true }));
    try {
      await alertasApi.snooze(id, 7);
      await fetchAlertas();
    } catch {
      // silently ignore
    } finally {
      setLoadingSnooze((prev) => ({ ...prev, [id]: false }));
    }
  };

  const handleEnviar = async (id: number) => {
    setLoadingEnviar((prev) => ({ ...prev, [id]: true }));
    try {
      await alertasApi.enviarManual(id);
      await fetchAlertas();
    } catch {
      // silently ignore
    } finally {
      setLoadingEnviar((prev) => ({ ...prev, [id]: false }));
    }
  };

  return (
    <div className="space-y-5">
      <div className="flex items-center justify-between gap-4 flex-wrap">
        <div>
          <h1 className="text-xl font-bold text-gray-900">Alertas</h1>
          <p className="text-sm text-gray-500 mt-0.5">
            {loading
              ? 'Carregando...'
              : `${alertas.length} alerta${alertas.length !== 1 ? 's' : ''} pendente${alertas.length !== 1 ? 's' : ''}`}
          </p>
        </div>
        {isAdmin && (
          <Link to="/alertas/config">
            <Button variant="secondary">
              <Settings size={16} />
              Configuracoes
            </Button>
          </Link>
        )}
      </div>

      {loading && (
        <div className="flex items-center justify-center py-20">
          <div className="animate-spin rounded-full h-10 w-10 border-2 border-red-600 border-t-transparent" />
        </div>
      )}

      {!loading && alertas.length === 0 && (
        <div className="bg-white border border-gray-200 rounded-xl p-12 flex flex-col items-center gap-3 shadow-sm">
          <div className="w-14 h-14 bg-green-100 rounded-full flex items-center justify-center">
            <CheckCircle size={28} className="text-green-600" />
          </div>
          <p className="text-base font-semibold text-gray-800">Nenhum alerta pendente</p>
          <p className="text-sm text-gray-500">
            Todos os documentos e cobrancas estao em dia.
          </p>
        </div>
      )}

      {!loading && alertas.length > 0 && (
        <div className="space-y-3">
          {alertas.map((alerta) => (
            <AlertaCard
              key={alerta.id}
              alerta={alerta}
              onSnooze={handleSnooze}
              onEnviar={handleEnviar}
              loadingSnooze={!!loadingSnooze[alerta.id]}
              loadingEnviar={!!loadingEnviar[alerta.id]}
            />
          ))}
        </div>
      )}
    </div>
  );
};

export default AlertaList;
