import React, { useState, useEffect, useCallback } from 'react';
import { TrendingUp, TrendingDown, AlertCircle, Clock } from 'lucide-react';
import type { Cobranca, FinanceiroSummary, Page, StatusCobranca } from '../../types';
import * as cobrancasApi from '../../api/cobrancas';
import Table from '../../components/ui/Table';
import Button from '../../components/ui/Button';
import SummaryCard from '../../components/ui/SummaryCard';
import { formatDate } from '../../utils/formatDate';

const PAGE_SIZE = 10;

const formatBRL = (value: number | null | undefined): string => {
  if (value == null) return 'R$ 0,00';
  return value.toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' });
};

const statusCobrancaConfig: Record<StatusCobranca, { label: string; classes: string }> = {
  PENDENTE: { label: 'Pendente', classes: 'bg-yellow-50 text-yellow-700 border border-yellow-200' },
  PAGO: { label: 'Pago', classes: 'bg-green-50 text-green-700 border border-green-200' },
  VENCIDO: { label: 'Vencido', classes: 'bg-red-50 text-red-700 border border-red-200' },
  CANCELADO: { label: 'Cancelado', classes: 'bg-gray-100 text-gray-600 border border-gray-200' },
};

const MESES = [
  { value: '1', label: 'Janeiro' },
  { value: '2', label: 'Fevereiro' },
  { value: '3', label: 'Marco' },
  { value: '4', label: 'Abril' },
  { value: '5', label: 'Maio' },
  { value: '6', label: 'Junho' },
  { value: '7', label: 'Julho' },
  { value: '8', label: 'Agosto' },
  { value: '9', label: 'Setembro' },
  { value: '10', label: 'Outubro' },
  { value: '11', label: 'Novembro' },
  { value: '12', label: 'Dezembro' },
];

const FinanceiroPage: React.FC = () => {
  const now = new Date();
  const [summary, setSummary] = useState<FinanceiroSummary | null>(null);
  const [loadingSummary, setLoadingSummary] = useState(true);
  const [data, setData] = useState<Page<Cobranca> | null>(null);
  const [loading, setLoading] = useState(true);
  const [statusFilter, setStatusFilter] = useState('');
  const [monthFilter, setMonthFilter] = useState(String(now.getMonth() + 1));
  const [yearFilter, setYearFilter] = useState(String(now.getFullYear()));
  const [page, setPage] = useState(0);

  useEffect(() => {
    let cancelled = false;
    cobrancasApi
      .getFinanceiroSummary()
      .then((data) => { if (!cancelled) setSummary(data); })
      .catch(() => { if (!cancelled) setSummary(null); })
      .finally(() => { if (!cancelled) setLoadingSummary(false); });
    return () => { cancelled = true; };
  }, []);

  const fetchCobrancas = useCallback(async (signal?: AbortSignal) => {
    setLoading(true);
    try {
      const result = await cobrancasApi.getAll({
        status: statusFilter || undefined,
        month: monthFilter ? Number(monthFilter) : undefined,
        year: yearFilter ? Number(yearFilter) : undefined,
        page,
        size: PAGE_SIZE,
      });
      if (!signal?.aborted) setData(result);
    } catch {
      if (!signal?.aborted) setData(null);
    } finally {
      if (!signal?.aborted) setLoading(false);
    }
  }, [statusFilter, monthFilter, yearFilter, page]);

  useEffect(() => {
    const controller = new AbortController();
    fetchCobrancas(controller.signal);
    return () => controller.abort();
  }, [fetchCobrancas]);

  const totalPages = data?.totalPages ?? 0;

  const displaySummary = (val: number | undefined): string => {
    if (loadingSummary) return '...';
    if (val === undefined) return 'R$ 0,00';
    return formatBRL(val);
  };

  const cobrancaColumns = [
    {
      key: 'dataVencimento',
      header: 'Vencimento',
      render: (row: Cobranca) => <span>{formatDate(row.dataVencimento)}</span>,
    },
    {
      key: 'contratoId',
      header: 'Contrato',
      render: (row: Cobranca) => (
        <span className="text-gray-700">#{row.contratoId}</span>
      ),
    },
    {
      key: 'valorEsperado',
      header: 'Valor Esperado',
      render: (row: Cobranca) => (
        <span className="font-mono text-gray-800">{formatBRL(row.valorEsperado)}</span>
      ),
    },
    {
      key: 'valorRecebido',
      header: 'Valor Recebido',
      render: (row: Cobranca) => (
        <span className="font-mono text-gray-800">{formatBRL(row.valorRecebido)}</span>
      ),
    },
    {
      key: 'dataPagamento',
      header: 'Data Pagamento',
      render: (row: Cobranca) => <span>{formatDate(row.dataPagamento)}</span>,
    },
    {
      key: 'statusCalculado',
      header: 'Status',
      render: (row: Cobranca) => {
        const config =
          statusCobrancaConfig[row.statusCalculado] ?? statusCobrancaConfig.CANCELADO;
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
      },
    },
  ];

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-xl font-bold text-gray-900">Financeiro</h1>
        <p className="text-sm text-gray-500 mt-0.5">Visao geral financeira</p>
      </div>

      <div className="grid grid-cols-1 sm:grid-cols-2 xl:grid-cols-4 gap-4">
        <SummaryCard
          title="A Receber"
          value={displaySummary(summary?.aReceber)}
          icon={<TrendingUp size={20} className="text-blue-600" />}
          iconBg="bg-blue-100"
          borderColor="border-blue-500"
          subtitle="mes atual — pendente"
        />
        <SummaryCard
          title="Recebido"
          value={displaySummary(summary?.recebido)}
          icon={<TrendingDown size={20} className="text-emerald-600" />}
          iconBg="bg-emerald-100"
          borderColor="border-emerald-500"
          subtitle="mes atual — pago"
        />
        <SummaryCard
          title="Em Atraso"
          value={displaySummary(summary?.emAtraso)}
          icon={<AlertCircle size={20} className="text-red-600" />}
          iconBg="bg-red-100"
          borderColor="border-red-500"
          subtitle="total em atraso"
        />
        <SummaryCard
          title="Vencendo em 7 dias"
          value={displaySummary(summary?.vencendo7dias)}
          icon={<Clock size={20} className="text-amber-600" />}
          iconBg="bg-amber-100"
          borderColor="border-amber-500"
          subtitle="proximo vencimento"
        />
      </div>

      <div className="bg-white border border-gray-200 rounded-xl overflow-hidden shadow-sm">
        <div className="px-5 py-4 border-b border-gray-200">
          <h2 className="text-sm font-semibold text-gray-700 mb-3">Cobrancas</h2>
          <div className="flex gap-3 flex-wrap">
            <select
              value={statusFilter}
              onChange={(e) => { setStatusFilter(e.target.value); setPage(0); }}
              className="bg-white border border-gray-300 rounded-md px-3 py-2 text-sm text-gray-900 focus:outline-none focus:ring-2 focus:ring-red-500 focus:border-red-500 cursor-pointer"
            >
              <option value="">Todos os status</option>
              <option value="PENDENTE">Pendente</option>
              <option value="PAGO">Pago</option>
              <option value="VENCIDO">Vencido</option>
              <option value="CANCELADO">Cancelado</option>
            </select>
            <select
              value={monthFilter}
              onChange={(e) => { setMonthFilter(e.target.value); setPage(0); }}
              className="bg-white border border-gray-300 rounded-md px-3 py-2 text-sm text-gray-900 focus:outline-none focus:ring-2 focus:ring-red-500 focus:border-red-500 cursor-pointer"
            >
              <option value="">Todos os meses</option>
              {MESES.map((m) => (
                <option key={m.value} value={m.value}>
                  {m.label}
                </option>
              ))}
            </select>
            <select
              value={yearFilter}
              onChange={(e) => { setYearFilter(e.target.value); setPage(0); }}
              className="bg-white border border-gray-300 rounded-md px-3 py-2 text-sm text-gray-900 focus:outline-none focus:ring-2 focus:ring-red-500 focus:border-red-500 cursor-pointer"
            >
              <option value="">Todos os anos</option>
              <option value={String(now.getFullYear() - 1)}>{now.getFullYear() - 1}</option>
              <option value={String(now.getFullYear())}>{now.getFullYear()}</option>
              <option value={String(now.getFullYear() + 1)}>{now.getFullYear() + 1}</option>
            </select>
          </div>
        </div>

        <Table
          columns={cobrancaColumns}
          data={data?.content ?? []}
          keyExtractor={(row) => row.id}
          loading={loading}
          emptyMessage="Nenhuma cobranca encontrada."
        />

        {totalPages > 1 && (
          <div className="flex items-center justify-between px-5 py-3 border-t border-gray-200">
            <p className="text-sm text-gray-500">
              Pagina {page + 1} de {totalPages}
            </p>
            <div className="flex gap-2">
              <Button
                variant="secondary"
                size="sm"
                disabled={page === 0}
                onClick={() => setPage((p) => Math.max(0, p - 1))}
              >
                Anterior
              </Button>
              <Button
                variant="secondary"
                size="sm"
                disabled={page >= totalPages - 1}
                onClick={() => setPage((p) => p + 1)}
              >
                Proximo
              </Button>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};

export default FinanceiroPage;
