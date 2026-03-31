import React, { useState, useEffect, useCallback } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import { Plus, Search, Pencil } from 'lucide-react';
import type { Documento, Page, CategoriaDocumento, StatusDocumento } from '../../types';
import * as documentosApi from '../../api/documentos';
import Button from '../../components/ui/Button';
import Table from '../../components/ui/Table';

const PAGE_SIZE = 10;

const categoriaLabels: Record<CategoriaDocumento, string> = {
  CONTRATO: 'Contrato',
  ALVARA: 'Alvara',
  CERTIFICADO: 'Certificado',
  LICENCA: 'Licenca',
  NF: 'NF',
  OUTRO: 'Outro',
};

const statusConfig: Record<StatusDocumento, { label: string; classes: string }> = {
  VALIDO: {
    label: 'Valido',
    classes: 'bg-green-100 text-green-700',
  },
  A_VENCER: {
    label: 'A Vencer',
    classes: 'bg-yellow-100 text-yellow-700',
  },
  VENCIDO: {
    label: 'Vencido',
    classes: 'bg-red-100 text-red-700',
  },
  SEM_VALIDADE: {
    label: 'Sem Validade',
    classes: 'bg-gray-100 text-gray-600',
  },
};

const formatDate = (value: string | null): string => {
  if (!value) return '—';
  const [year, month, day] = value.split('-');
  if (!year || !month || !day) return value;
  return `${day}/${month}/${year}`;
};

const StatusBadge: React.FC<{ status: StatusDocumento }> = ({ status }) => {
  const config = statusConfig[status] ?? statusConfig.SEM_VALIDADE;
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

const CategoriaBadge: React.FC<{ categoria: CategoriaDocumento }> = ({ categoria }) => (
  <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-blue-50 text-blue-700 border border-blue-100">
    {categoriaLabels[categoria] ?? categoria}
  </span>
);

const DocumentoList: React.FC = () => {
  const [searchParams, setSearchParams] = useSearchParams();

  const [data, setData] = useState<Page<Documento> | null>(null);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');
  const [debouncedSearch, setDebouncedSearch] = useState('');
  const [categoriaFilter, setCategoriaFilter] = useState('');
  const [statusFilter, setStatusFilter] = useState(() => searchParams.get('status') ?? '');
  const [page, setPage] = useState(0);

  useEffect(() => {
    const timer = setTimeout(() => {
      setDebouncedSearch(search);
      setPage(0);
    }, 300);
    return () => clearTimeout(timer);
  }, [search]);

  const fetchDocumentos = useCallback(async () => {
    setLoading(true);
    try {
      const result = await documentosApi.getAll({
        search: debouncedSearch || undefined,
        categoria: categoriaFilter || undefined,
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
  }, [debouncedSearch, categoriaFilter, statusFilter, page]);

  useEffect(() => {
    fetchDocumentos();
  }, [fetchDocumentos]);

  const handleCategoriaChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
    setCategoriaFilter(e.target.value);
    setPage(0);
  };

  const handleStatusChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
    const value = e.target.value;
    setStatusFilter(value);
    setPage(0);
    if (value) {
      setSearchParams({ status: value });
    } else {
      setSearchParams({});
    }
  };

  const totalPages = data?.totalPages ?? 0;

  const columns = [
    {
      key: 'nome',
      header: 'Nome',
      render: (row: Documento) => (
        <span className="text-gray-900 font-medium">{row.nome}</span>
      ),
    },
    {
      key: 'clienteNome',
      header: 'Cliente',
      render: (row: Documento) => (
        <Link
          to={`/clientes/${row.clienteId}`}
          className="text-red-600 hover:text-red-700 hover:underline text-sm"
        >
          {row.clienteNome}
        </Link>
      ),
    },
    {
      key: 'categoria',
      header: 'Categoria',
      render: (row: Documento) => <CategoriaBadge categoria={row.categoria} />,
    },
    {
      key: 'dataEmissao',
      header: 'Emissao',
      render: (row: Documento) => (
        <span className="text-sm text-gray-600">{formatDate(row.dataEmissao)}</span>
      ),
    },
    {
      key: 'dataValidade',
      header: 'Validade',
      render: (row: Documento) => (
        <span className="text-sm text-gray-600">{formatDate(row.dataValidade)}</span>
      ),
    },
    {
      key: 'statusCalculado',
      header: 'Status',
      render: (row: Documento) => <StatusBadge status={row.statusCalculado} />,
    },
    {
      key: 'acoes',
      header: 'Acoes',
      className: 'text-right',
      render: (row: Documento) => (
        <div className="flex items-center justify-end gap-2">
          <Link to={`/documentos/${row.id}/editar`}>
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
          <h1 className="text-xl font-bold text-gray-900">Documentos</h1>
          <p className="text-sm text-gray-500 mt-0.5">
            Relatorio geral de documentos de todos os clientes
          </p>
        </div>
        <Link to="/documentos/novo">
          <Button variant="primary">
            <Plus size={16} />
            Novo Documento
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
            placeholder="Buscar por nome do documento..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            className="w-full bg-white border border-gray-300 rounded-md pl-9 pr-3 py-2 text-sm text-gray-900 placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-red-500 focus:border-red-500"
          />
        </div>
        <select
          value={categoriaFilter}
          onChange={handleCategoriaChange}
          className="bg-white border border-gray-300 rounded-md px-3 py-2 text-sm text-gray-900 focus:outline-none focus:ring-2 focus:ring-red-500 focus:border-red-500 cursor-pointer"
        >
          <option value="">Todas as categorias</option>
          <option value="CONTRATO">Contrato</option>
          <option value="ALVARA">Alvara</option>
          <option value="CERTIFICADO">Certificado</option>
          <option value="LICENCA">Licenca</option>
          <option value="NF">NF</option>
          <option value="OUTRO">Outro</option>
        </select>
        <select
          value={statusFilter}
          onChange={handleStatusChange}
          className="bg-white border border-gray-300 rounded-md px-3 py-2 text-sm text-gray-900 focus:outline-none focus:ring-2 focus:ring-red-500 focus:border-red-500 cursor-pointer"
        >
          <option value="">Todos os status</option>
          <option value="VALIDO">Valido</option>
          <option value="A_VENCER">A Vencer</option>
          <option value="VENCIDO">Vencido</option>
          <option value="SEM_VALIDADE">Sem Validade</option>
        </select>
      </div>

      <Table
        columns={columns}
        data={data?.content ?? []}
        keyExtractor={(row) => row.id}
        loading={loading}
        emptyMessage="Nenhum documento encontrado."
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

export default DocumentoList;
