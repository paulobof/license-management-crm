import React, { useState, useEffect, useCallback } from 'react';
import { Link } from 'react-router-dom';
import { Plus, Search, Eye, Pencil } from 'lucide-react';
import type { Contrato, Page, StatusContrato, Periodicidade } from '../../types';
import * as contratosApi from '../../api/contratos';
import Button from '../../components/ui/Button';
import Table from '../../components/ui/Table';

const PAGE_SIZE = 10;

const formatBRL = (value: number | null | undefined): string => {
  if (value == null) return 'R$ 0,00';
  return value.toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' });
};

const formatDate = (value: string | null): string => {
  if (!value) return '—';
  const [year, month, day] = value.split('-');
  return `${day}/${month}/${year}`;
};

const statusConfig: Record<StatusContrato, { label: string; classes: string }> = {
  ATIVO: { label: 'Ativo', classes: 'bg-green-50 text-green-700 border border-green-200' },
  ENCERRADO: { label: 'Encerrado', classes: 'bg-gray-100 text-gray-600 border border-gray-200' },
  CANCELADO: { label: 'Cancelado', classes: 'bg-red-50 text-red-700 border border-red-200' },
};

const periodicidadeLabel: Record<Periodicidade, string> = {
  MENSAL: 'Mensal',
  TRIMESTRAL: 'Trimestral',
  SEMESTRAL: 'Semestral',
  ANUAL: 'Anual',
  AVULSO: 'Avulso',
};

const StatusBadge: React.FC<{ status: StatusContrato }> = ({ status }) => {
  const config = statusConfig[status] ?? statusConfig.CANCELADO;
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

const ContratoList: React.FC = () => {
  const [data, setData] = useState<Page<Contrato> | null>(null);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');
  const [debouncedSearch, setDebouncedSearch] = useState('');
  const [statusFilter, setStatusFilter] = useState('');
  const [periodicidadeFilter, setPeriodicidadeFilter] = useState('');
  const [page, setPage] = useState(0);

  useEffect(() => {
    const timer = setTimeout(() => {
      setDebouncedSearch(search);
      setPage(0);
    }, 300);
    return () => clearTimeout(timer);
  }, [search]);

  const fetchContratos = useCallback(async () => {
    setLoading(true);
    try {
      const result = await contratosApi.getAll({
        search: debouncedSearch || undefined,
        status: statusFilter || undefined,
        periodicidade: periodicidadeFilter || undefined,
        page,
        size: PAGE_SIZE,
      });
      setData(result);
    } catch {
      setData(null);
    } finally {
      setLoading(false);
    }
  }, [debouncedSearch, statusFilter, periodicidadeFilter, page]);

  useEffect(() => {
    fetchContratos();
  }, [fetchContratos]);

  const handleStatusChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
    setStatusFilter(e.target.value);
    setPage(0);
  };

  const handlePeriodicidadeChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
    setPeriodicidadeFilter(e.target.value);
    setPage(0);
  };

  const totalPages = data?.totalPages ?? 0;

  const columns = [
    {
      key: 'descricao',
      header: 'Descricao',
      render: (row: Contrato) => (
        <span className="text-gray-900 font-medium">{row.descricao}</span>
      ),
    },
    {
      key: 'clienteNome',
      header: 'Cliente',
      render: (row: Contrato) => (
        <Link
          to={`/clientes/${row.clienteId}`}
          className="text-red-600 hover:text-red-700 hover:underline"
        >
          {row.clienteNome}
        </Link>
      ),
    },
    {
      key: 'valor',
      header: 'Valor',
      render: (row: Contrato) => (
        <span className="font-mono text-gray-800">{formatBRL(row.valor)}</span>
      ),
    },
    {
      key: 'periodicidade',
      header: 'Periodicidade',
      render: (row: Contrato) => (
        <span>{periodicidadeLabel[row.periodicidade] ?? row.periodicidade}</span>
      ),
    },
    {
      key: 'dataInicio',
      header: 'Inicio',
      render: (row: Contrato) => <span>{formatDate(row.dataInicio)}</span>,
    },
    {
      key: 'status',
      header: 'Status',
      render: (row: Contrato) => <StatusBadge status={row.status} />,
    },
    {
      key: 'acoes',
      header: 'Acoes',
      className: 'text-right',
      render: (row: Contrato) => (
        <div className="flex items-center justify-end gap-2">
          <Link to={`/contratos/${row.id}`}>
            <Button variant="ghost" size="sm" title="Visualizar">
              <Eye size={15} />
            </Button>
          </Link>
          <Link to={`/contratos/${row.id}/editar`}>
            <Button variant="ghost" size="sm" title="Editar">
              <Pencil size={15} />
            </Button>
          </Link>
        </div>
      ),
    },
  ];

  return (
    <div className="space-y-5">
      <div className="flex items-center justify-between gap-4 flex-wrap">
        <div>
          <h1 className="text-xl font-bold text-gray-900">Contratos</h1>
          <p className="text-sm text-gray-500 mt-0.5">
            {data ? `${data.totalElements} contratos cadastrados` : 'Carregando...'}
          </p>
        </div>
        <Link to="/contratos/novo">
          <Button variant="primary">
            <Plus size={16} />
            Novo Contrato
          </Button>
        </Link>
      </div>

      <div className="flex gap-3 flex-wrap">
        <div className="relative flex-1 min-w-48">
          <Search
            size={16}
            className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-500 pointer-events-none"
          />
          <input
            type="text"
            placeholder="Buscar por descricao ou cliente..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            className="w-full bg-white border border-gray-300 rounded-md pl-9 pr-3 py-2 text-sm text-gray-900 placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-red-500 focus:border-red-500"
          />
        </div>
        <select
          value={statusFilter}
          onChange={handleStatusChange}
          className="bg-white border border-gray-300 rounded-md px-3 py-2 text-sm text-gray-900 focus:outline-none focus:ring-2 focus:ring-red-500 focus:border-red-500 cursor-pointer"
        >
          <option value="">Todos os status</option>
          <option value="ATIVO">Ativo</option>
          <option value="ENCERRADO">Encerrado</option>
          <option value="CANCELADO">Cancelado</option>
        </select>
        <select
          value={periodicidadeFilter}
          onChange={handlePeriodicidadeChange}
          className="bg-white border border-gray-300 rounded-md px-3 py-2 text-sm text-gray-900 focus:outline-none focus:ring-2 focus:ring-red-500 focus:border-red-500 cursor-pointer"
        >
          <option value="">Todas as periodicidades</option>
          <option value="MENSAL">Mensal</option>
          <option value="TRIMESTRAL">Trimestral</option>
          <option value="SEMESTRAL">Semestral</option>
          <option value="ANUAL">Anual</option>
          <option value="AVULSO">Avulso</option>
        </select>
      </div>

      <Table
        columns={columns}
        data={data?.content ?? []}
        keyExtractor={(row) => row.id}
        loading={loading}
        emptyMessage="Nenhum contrato encontrado."
      />

      {totalPages > 1 && (
        <div className="flex items-center justify-between pt-2">
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
  );
};

export default ContratoList;
