import React, { useState, useEffect, useCallback } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import { ArrowLeft, Pencil, RefreshCw } from 'lucide-react';
import type { Contrato, Cobranca, StatusContrato, StatusCobranca } from '../../types';
import * as contratosApi from '../../api/contratos';
import * as cobrancasApi from '../../api/cobrancas';
import Button from '../../components/ui/Button';
import Table from '../../components/ui/Table';
import Modal from '../../components/ui/Modal';
import Input from '../../components/ui/Input';

const formatBRL = (value: number | null | undefined): string => {
  if (value == null) return 'R$ 0,00';
  return value.toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' });
};

const formatDate = (value: string | null): string => {
  if (!value) return '—';
  const [year, month, day] = value.split('-');
  return `${day}/${month}/${year}`;
};

const statusContratoConfig: Record<StatusContrato, { label: string; classes: string }> = {
  ATIVO: { label: 'Ativo', classes: 'bg-green-50 text-green-700 border border-green-200' },
  ENCERRADO: { label: 'Encerrado', classes: 'bg-gray-100 text-gray-600 border border-gray-200' },
  CANCELADO: { label: 'Cancelado', classes: 'bg-red-50 text-red-700 border border-red-200' },
};

const statusCobrancaConfig: Record<StatusCobranca, { label: string; classes: string }> = {
  PENDENTE: { label: 'Pendente', classes: 'bg-yellow-50 text-yellow-700 border border-yellow-200' },
  PAGO: { label: 'Pago', classes: 'bg-green-50 text-green-700 border border-green-200' },
  VENCIDO: { label: 'Vencido', classes: 'bg-red-50 text-red-700 border border-red-200' },
  CANCELADO: { label: 'Cancelado', classes: 'bg-gray-100 text-gray-600 border border-gray-200' },
};

const StatusBadge: React.FC<{ status: StatusCobranca }> = ({ status }) => {
  const config = statusCobrancaConfig[status] ?? statusCobrancaConfig.CANCELADO;
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

const Section: React.FC<{ title: string; children: React.ReactNode }> = ({ title, children }) => (
  <div className="bg-white border border-gray-200 rounded-xl p-5">
    <h2 className="text-sm font-semibold text-gray-500 uppercase tracking-wider mb-4">{title}</h2>
    {children}
  </div>
);

const Field: React.FC<{ label: string; value?: string | null }> = ({ label, value }) => (
  <div>
    <dt className="text-xs text-gray-500 mb-0.5">{label}</dt>
    <dd className="text-sm text-gray-800">{value || '—'}</dd>
  </div>
);

interface PagamentoForm {
  valorRecebido: string;
  dataPagamento: string;
  formaPagamento: string;
}

const ContratoDetail: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();

  const [contrato, setContrato] = useState<Contrato | null>(null);
  const [cobrancas, setCobrancas] = useState<Cobranca[]>([]);
  const [loading, setLoading] = useState(true);
  const [gerandoCobrancas, setGerandoCobrancas] = useState(false);

  const [pagamentoModal, setPagamentoModal] = useState(false);
  const [cobrancaSelecionada, setCobrancaSelecionada] = useState<Cobranca | null>(null);
  const [pagamentoForm, setPagamentoForm] = useState<PagamentoForm>({
    valorRecebido: '',
    dataPagamento: '',
    formaPagamento: 'PIX',
  });
  const [salvandoPagamento, setSalvandoPagamento] = useState(false);
  const [pagamentoError, setPagamentoError] = useState('');

  const fetchCobrancas = useCallback(async (contratoId: number) => {
    try {
      const result = await cobrancasApi.getByContratoId(contratoId);
      setCobrancas(result);
    } catch {
      setCobrancas([]);
    }
  }, []);

  useEffect(() => {
    if (!id) return;
    setLoading(true);
    contratosApi
      .getById(Number(id))
      .then((c) => {
        setContrato(c);
        return fetchCobrancas(c.id);
      })
      .catch(() => navigate('/contratos'))
      .finally(() => setLoading(false));
  }, [id, navigate, fetchCobrancas]);

  const handleGerarCobrancas = async () => {
    if (!contrato) return;
    setGerandoCobrancas(true);
    try {
      await contratosApi.gerarCobrancas(contrato.id);
      await fetchCobrancas(contrato.id);
    } catch {
      // silently ignore
    } finally {
      setGerandoCobrancas(false);
    }
  };

  const openPagamentoModal = (cobranca: Cobranca) => {
    setCobrancaSelecionada(cobranca);
    setPagamentoForm({
      valorRecebido: String(cobranca.valorEsperado),
      dataPagamento: new Date().toISOString().split('T')[0],
      formaPagamento: 'PIX',
    });
    setPagamentoError('');
    setPagamentoModal(true);
  };

  const handleRegistrarPagamento = async () => {
    if (!cobrancaSelecionada) return;
    if (!pagamentoForm.valorRecebido || Number(pagamentoForm.valorRecebido) <= 0) {
      setPagamentoError('Valor recebido deve ser maior que zero.');
      return;
    }
    if (!pagamentoForm.dataPagamento) {
      setPagamentoError('Data de pagamento e obrigatoria.');
      return;
    }
    setSalvandoPagamento(true);
    setPagamentoError('');
    try {
      await cobrancasApi.registrarPagamento(cobrancaSelecionada.id, {
        valorRecebido: Number(pagamentoForm.valorRecebido),
        dataPagamento: pagamentoForm.dataPagamento,
        formaPagamento: pagamentoForm.formaPagamento,
      });
      setPagamentoModal(false);
      if (contrato) await fetchCobrancas(contrato.id);
    } catch (err: unknown) {
      const axiosError = err as { response?: { data?: { message?: string } } };
      setPagamentoError(axiosError?.response?.data?.message ?? 'Erro ao registrar pagamento.');
    } finally {
      setSalvandoPagamento(false);
    }
  };

  const cobrancaColumns = [
    {
      key: 'dataVencimento',
      header: 'Vencimento',
      render: (row: Cobranca) => <span>{formatDate(row.dataVencimento)}</span>,
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
      key: 'statusCalculado',
      header: 'Status',
      render: (row: Cobranca) => <StatusBadge status={row.statusCalculado} />,
    },
    {
      key: 'dataPagamento',
      header: 'Pagamento',
      render: (row: Cobranca) => (
        <span className="text-gray-600">
          {row.dataPagamento ? formatDate(row.dataPagamento) : '—'}
          {row.formaPagamento ? ` — ${row.formaPagamento}` : ''}
        </span>
      ),
    },
    {
      key: 'acoes',
      header: 'Acoes',
      className: 'text-right',
      render: (row: Cobranca) => (
        <div className="flex items-center justify-end">
          {row.statusCalculado !== 'PAGO' && row.statusCalculado !== 'CANCELADO' && (
            <Button
              variant="secondary"
              size="sm"
              onClick={() => openPagamentoModal(row)}
            >
              Registrar Pagamento
            </Button>
          )}
        </div>
      ),
    },
  ];

  if (loading) {
    return (
      <div className="flex items-center justify-center py-24">
        <div className="animate-spin rounded-full h-10 w-10 border-2 border-red-600 border-t-transparent" />
      </div>
    );
  }

  if (!contrato) return null;

  const statusContratoInfo =
    statusContratoConfig[contrato.status] ?? statusContratoConfig.CANCELADO;
  const periodicidadeLabel: Record<string, string> = {
    MENSAL: 'Mensal',
    TRIMESTRAL: 'Trimestral',
    SEMESTRAL: 'Semestral',
    ANUAL: 'Anual',
    AVULSO: 'Avulso',
  };

  return (
    <div className="max-w-5xl mx-auto space-y-5">
      <div className="flex items-start justify-between gap-4 flex-wrap">
        <div className="flex items-center gap-3">
          <button
            onClick={() => navigate('/contratos')}
            className="text-gray-500 hover:text-gray-900 p-1.5 rounded-lg hover:bg-white transition-colors cursor-pointer"
            aria-label="Voltar"
          >
            <ArrowLeft size={18} />
          </button>
          <div>
            <div className="flex items-center gap-3">
              <h1 className="text-xl font-bold text-gray-900">{contrato.descricao}</h1>
              <span
                className={[
                  'inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium',
                  statusContratoInfo.classes,
                ].join(' ')}
              >
                {statusContratoInfo.label}
              </span>
            </div>
            <p className="text-sm text-gray-500 mt-0.5">{contrato.clienteNome}</p>
          </div>
        </div>
        <Link to={`/contratos/${contrato.id}/editar`}>
          <Button variant="primary" size="sm">
            <Pencil size={14} />
            Editar
          </Button>
        </Link>
      </div>

      <Section title="Dados do Contrato">
        <dl className="grid grid-cols-2 md:grid-cols-4 gap-4">
          <Field
            label="Cliente"
            value={contrato.clienteNome}
          />
          <Field
            label="Valor"
            value={formatBRL(contrato.valor)}
          />
          <Field
            label="Periodicidade"
            value={periodicidadeLabel[contrato.periodicidade] ?? contrato.periodicidade}
          />
          <Field label="Status" value={statusContratoInfo.label} />
          <Field label="Data de Inicio" value={formatDate(contrato.dataInicio)} />
          <Field label="Data de Fim" value={formatDate(contrato.dataFim)} />
          {contrato.observacoes && (
            <div className="col-span-2 md:col-span-4">
              <dt className="text-xs text-gray-500 mb-0.5">Observacoes</dt>
              <dd className="text-sm text-gray-800">{contrato.observacoes}</dd>
            </div>
          )}
        </dl>
      </Section>

      <div className="bg-white border border-gray-200 rounded-xl overflow-hidden">
        <div className="flex items-center justify-between px-5 py-4 border-b border-gray-200">
          <h2 className="text-sm font-semibold text-gray-500 uppercase tracking-wider">
            Cobrancas ({cobrancas.length})
          </h2>
          <Button
            variant="secondary"
            size="sm"
            loading={gerandoCobrancas}
            onClick={handleGerarCobrancas}
          >
            <RefreshCw size={14} />
            Gerar Cobranca Mensal
          </Button>
        </div>
        <div className="overflow-x-auto">
          <Table
            columns={cobrancaColumns}
            data={cobrancas}
            keyExtractor={(row) => row.id}
            loading={false}
            emptyMessage="Nenhuma cobranca encontrada para este contrato."
          />
        </div>
      </div>

      <div className="text-xs text-gray-500 space-y-0.5 pb-2">
        <p>Criado em: {contrato.createdAt || '—'}</p>
        <p>Atualizado em: {contrato.updatedAt || '—'}</p>
      </div>

      <Modal
        isOpen={pagamentoModal}
        onClose={() => setPagamentoModal(false)}
        title="Registrar Pagamento"
        size="sm"
        footer={
          <>
            <Button variant="ghost" onClick={() => setPagamentoModal(false)}>
              Cancelar
            </Button>
            <Button
              variant="primary"
              loading={salvandoPagamento}
              onClick={handleRegistrarPagamento}
            >
              {salvandoPagamento ? 'Salvando...' : 'Confirmar'}
            </Button>
          </>
        }
      >
        <div className="space-y-4">
          {cobrancaSelecionada && (
            <div className="bg-gray-50 rounded-lg px-4 py-3 text-sm text-gray-700">
              Vencimento: <strong>{formatDate(cobrancaSelecionada.dataVencimento)}</strong>
              {' — '}
              Esperado: <strong>{formatBRL(cobrancaSelecionada.valorEsperado)}</strong>
            </div>
          )}
          <Input
            label="Valor Recebido (R$) *"
            type="number"
            placeholder="0.00"
            value={pagamentoForm.valorRecebido}
            onChange={(e) =>
              setPagamentoForm((prev) => ({ ...prev, valorRecebido: e.target.value }))
            }
          />
          <Input
            label="Data de Pagamento *"
            type="date"
            value={pagamentoForm.dataPagamento}
            onChange={(e) =>
              setPagamentoForm((prev) => ({ ...prev, dataPagamento: e.target.value }))
            }
          />
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Forma de Pagamento
            </label>
            <select
              value={pagamentoForm.formaPagamento}
              onChange={(e) =>
                setPagamentoForm((prev) => ({ ...prev, formaPagamento: e.target.value }))
              }
              className="w-full bg-white border border-gray-300 rounded-md px-3 py-2 text-sm text-gray-900 focus:outline-none focus:ring-2 focus:ring-red-500 focus:border-red-500 cursor-pointer"
            >
              <option value="PIX">Pix</option>
              <option value="BOLETO">Boleto</option>
              <option value="TRANSFERENCIA">Transferencia</option>
              <option value="CARTAO">Cartao</option>
              <option value="DINHEIRO">Dinheiro</option>
              <option value="OUTRO">Outro</option>
            </select>
          </div>
          {pagamentoError && (
            <div className="bg-red-50 border border-red-200 rounded-lg px-4 py-3">
              <p className="text-sm text-red-600">{pagamentoError}</p>
            </div>
          )}
        </div>
      </Modal>
    </div>
  );
};

export default ContratoDetail;
