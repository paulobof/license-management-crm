import React, { useState, useEffect } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import { Pencil, ArrowLeft, MapPin, Phone, Mail, User } from 'lucide-react';
import type { Cliente } from '../../types';
import * as clientesApi from '../../api/clientes';
import Button from '../../components/ui/Button';
import Badge from '../../components/ui/Badge';

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

const tipoEnderecoLabel: Record<string, string> = {
  COBRANCA: 'Cobranca',
  ENTREGA: 'Entrega',
  FILIAL: 'Filial',
  OUTRO: 'Outro',
};

const ClienteDetail: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [cliente, setCliente] = useState<Cliente | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!id) return;
    setLoading(true);
    clientesApi
      .getById(Number(id))
      .then(setCliente)
      .catch(() => navigate('/clientes'))
      .finally(() => setLoading(false));
  }, [id, navigate]);

  if (loading) {
    return (
      <div className="flex items-center justify-center py-24">
        <div className="animate-spin rounded-full h-10 w-10 border-2 border-blue-500 border-t-transparent" />
      </div>
    );
  }

  if (!cliente) return null;

  return (
    <div className="max-w-4xl mx-auto space-y-5">
      <div className="flex items-start justify-between gap-4 flex-wrap">
        <div className="flex items-center gap-3">
          <button
            onClick={() => navigate('/clientes')}
            className="text-gray-500 hover:text-gray-900 p-1.5 rounded-lg hover:bg-white transition-colors cursor-pointer"
            aria-label="Voltar"
          >
            <ArrowLeft size={18} />
          </button>
          <div>
            <div className="flex items-center gap-3">
              <h1 className="text-xl font-bold text-gray-900">{cliente.razaoSocial}</h1>
              <Badge status={cliente.status} />
            </div>
            {cliente.nomeFantasia && (
              <p className="text-sm text-gray-500 mt-0.5">{cliente.nomeFantasia}</p>
            )}
          </div>
        </div>
        <Link to={`/clientes/${cliente.id}/editar`}>
          <Button variant="primary" size="sm">
            <Pencil size={14} />
            Editar
          </Button>
        </Link>
      </div>

      <Section title="Dados da Empresa">
        <dl className="grid grid-cols-2 md:grid-cols-4 gap-4">
          <Field label="CNPJ" value={cliente.cnpj} />
          <Field label="Inscricao Estadual" value={cliente.ie} />
          <Field label="Segmento" value={cliente.segmento} />
          <Field label="Status" value={cliente.status === 'ATIVO' ? 'Ativo' : 'Inativo'} />
          <Field label="Data de Fundacao" value={cliente.dataFundacao} />
          <Field label="Inicio como Cliente" value={cliente.dataInicioCliente} />
          <Field label="ID Google Drive" value={cliente.googleDriveFolderId} />
        </dl>
      </Section>

      <Section title={`Contatos (${cliente.contatos?.length ?? 0})`}>
        {!cliente.contatos?.length ? (
          <p className="text-sm text-gray-500">Nenhum contato cadastrado.</p>
        ) : (
          <div className="space-y-4">
            {cliente.contatos.map((contato, idx) => (
              <div
                key={contato.id ?? idx}
                className="bg-white border border-gray-200 rounded-lg p-4"
              >
                <div className="flex items-center gap-2 mb-3">
                  <div className="w-8 h-8 rounded-full bg-red-600/20 border border-blue-600/40 flex items-center justify-center shrink-0">
                    <User size={14} className="text-red-600" />
                  </div>
                  <div>
                    <p className="text-sm font-medium text-gray-900">{contato.nome || '—'}</p>
                    <p className="text-xs text-gray-500">{contato.cargo || '—'}</p>
                  </div>
                  {contato.principal && (
                    <span className="ml-auto text-xs text-red-600 bg-blue-900/40 border border-blue-700/50 px-2 py-0.5 rounded-full">
                      Principal
                    </span>
                  )}
                </div>
                <div className="grid grid-cols-1 sm:grid-cols-3 gap-2 text-sm">
                  <div className="flex items-center gap-2 text-gray-500">
                    <Mail size={13} className="shrink-0" />
                    <span className="truncate">{contato.email || '—'}</span>
                  </div>
                  <div className="flex items-center gap-2 text-gray-500">
                    <Phone size={13} className="shrink-0" />
                    <span>{contato.telefone || '—'}</span>
                  </div>
                  <div className="flex items-center gap-2 text-gray-500">
                    <Phone size={13} className="shrink-0" />
                    <span>WhatsApp: {contato.whatsapp || '—'}</span>
                  </div>
                </div>
              </div>
            ))}
          </div>
        )}
      </Section>

      <Section title={`Enderecos (${cliente.enderecos?.length ?? 0})`}>
        {!cliente.enderecos?.length ? (
          <p className="text-sm text-gray-500">Nenhum endereco cadastrado.</p>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            {cliente.enderecos.map((endereco, idx) => (
              <div
                key={endereco.id ?? idx}
                className="bg-white border border-gray-200 rounded-lg p-4"
              >
                <div className="flex items-center gap-2 mb-2">
                  <MapPin size={14} className="text-red-600 shrink-0" />
                  <span className="text-xs font-semibold text-red-600 uppercase">
                    {tipoEnderecoLabel[endereco.tipo] ?? endereco.tipo}
                  </span>
                </div>
                <p className="text-sm text-gray-800">
                  {[endereco.logradouro, endereco.numero, endereco.complemento]
                    .filter(Boolean)
                    .join(', ') || '—'}
                </p>
                <p className="text-sm text-gray-500 mt-0.5">
                  {[endereco.bairro, endereco.cidade, endereco.estado]
                    .filter(Boolean)
                    .join(', ') || '—'}
                </p>
                {endereco.cep && (
                  <p className="text-xs text-gray-500 mt-1">CEP: {endereco.cep}</p>
                )}
              </div>
            ))}
          </div>
        )}
      </Section>

      <div className="text-xs text-gray-500 space-y-0.5 pb-2">
        <p>Criado em: {cliente.createdAt || '—'}</p>
        <p>Atualizado em: {cliente.updatedAt || '—'}</p>
      </div>
    </div>
  );
};

export default ClienteDetail;
