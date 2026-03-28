import React, { useState, useEffect, useCallback } from 'react';
import { Link } from 'react-router-dom';
import { Plus, Search, Pencil, Eye } from 'lucide-react';
import type { Cliente, Page } from '../../types';
import * as clientesApi from '../../api/clientes';
import Button from '../../components/ui/Button';
import Table from '../../components/ui/Table';
import Badge from '../../components/ui/Badge';

const PAGE_SIZE = 10;

const ClienteList: React.FC = () => {
  const [data, setData] = useState<Page<Cliente> | null>(null);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');
  const [debouncedSearch, setDebouncedSearch] = useState('');
  const [statusFilter, setStatusFilter] = useState('');
  const [page, setPage] = useState(0);

  useEffect(() => {
    const timer = setTimeout(() => {
      setDebouncedSearch(search);
      setPage(0);
    }, 300);
    return () => clearTimeout(timer);
  }, [search]);

  const fetchClientes = useCallback(async () => {
    setLoading(true);
    try {
      const result = await clientesApi.getAll({
        search: debouncedSearch || undefined,
        status: statusFilter || undefined,
        page,
        size: PAGE_SIZE,
      });
      setData(result);
    } catch {
      setData(null);
    } finally {
      setLoading(false);
    }
  }, [debouncedSearch, statusFilter, page]);

  useEffect(() => {
    fetchClientes();
  }, [fetchClientes]);

  const handleStatusChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
    setStatusFilter(e.target.value);
    setPage(0);
  };

  const totalPages = data?.totalPages ?? 0;

  const columns = [
    {
      key: 'razaoSocial',
      header: 'Razao Social',
      render: (row: Cliente) => (
        <span className="text-zinc-100 font-medium">{row.razaoSocial}</span>
      ),
    },
    {
      key: 'cnpj',
      header: 'CNPJ',
      render: (row: Cliente) => <span className="font-mono text-xs">{row.cnpj}</span>,
    },
    {
      key: 'segmento',
      header: 'Segmento',
    },
    {
      key: 'status',
      header: 'Status',
      render: (row: Cliente) => <Badge status={row.status} />,
    },
    {
      key: 'acoes',
      header: 'Acoes',
      className: 'text-right',
      render: (row: Cliente) => (
        <div className="flex items-center justify-end gap-2">
          <Link to={`/clientes/${row.id}`}>
            <Button variant="ghost" size="sm" title="Visualizar">
              <Eye size={15} />
            </Button>
          </Link>
          <Link to={`/clientes/${row.id}/editar`}>
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
          <h1 className="text-xl font-bold text-zinc-100">Clientes</h1>
          <p className="text-sm text-zinc-500 mt-0.5">
            {data ? `${data.totalElements} clientes cadastrados` : 'Carregando...'}
          </p>
        </div>
        <Link to="/clientes/novo">
          <Button variant="primary">
            <Plus size={16} />
            Novo Cliente
          </Button>
        </Link>
      </div>

      <div className="flex gap-3 flex-wrap">
        <div className="relative flex-1 min-w-48">
          <Search
            size={16}
            className="absolute left-3 top-1/2 -translate-y-1/2 text-zinc-500 pointer-events-none"
          />
          <input
            type="text"
            placeholder="Buscar por razao social, CNPJ..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            className="w-full bg-zinc-800 border border-zinc-700 rounded-md pl-9 pr-3 py-2 text-sm text-zinc-100 placeholder-zinc-500 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
          />
        </div>
        <select
          value={statusFilter}
          onChange={handleStatusChange}
          className="bg-zinc-800 border border-zinc-700 rounded-md px-3 py-2 text-sm text-zinc-100 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500 cursor-pointer"
        >
          <option value="">Todos os status</option>
          <option value="ATIVO">Ativos</option>
          <option value="INATIVO">Inativos</option>
        </select>
      </div>

      <Table
        columns={columns}
        data={data?.content ?? []}
        keyExtractor={(row) => row.id}
        loading={loading}
        emptyMessage="Nenhum cliente encontrado."
      />

      {totalPages > 1 && (
        <div className="flex items-center justify-between pt-2">
          <p className="text-sm text-zinc-500">
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

export default ClienteList;
