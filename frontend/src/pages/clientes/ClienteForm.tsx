import React, { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { Plus, Trash2, Search } from 'lucide-react';
import type { Contato, Endereco } from '../../types';
import * as clientesApi from '../../api/clientes';
import Button from '../../components/ui/Button';
import Input from '../../components/ui/Input';

type Tab = 'cadastro' | 'contatos' | 'enderecos';
type TipoPessoa = 'JURIDICA' | 'FISICA';

const emptyContato = (): Contato => ({
  nome: '',
  cargo: '',
  email: '',
  telefone: '',
  whatsapp: '',
  principal: false,
});

const emptyEndereco = (): Endereco => ({
  tipo: 'COBRANCA',
  cep: '',
  logradouro: '',
  numero: '',
  complemento: '',
  bairro: '',
  cidade: '',
  estado: '',
});

interface FormData {
  tipoPessoa: TipoPessoa;
  razaoSocial: string;
  nomeFantasia: string;
  cnpj: string;
  cpf: string;
  ie: string;
  segmento: string;
  dataFundacao: string;
  dataInicioCliente: string;
  status: 'ATIVO' | 'INATIVO';
  googleDriveFolderId: string;
  contatos: Contato[];
  enderecos: Endereco[];
}

const emptyForm = (): FormData => ({
  tipoPessoa: 'JURIDICA',
  razaoSocial: '',
  nomeFantasia: '',
  cnpj: '',
  cpf: '',
  ie: '',
  segmento: '',
  dataFundacao: '',
  dataInicioCliente: '',
  status: 'ATIVO',
  googleDriveFolderId: '',
  contatos: [],
  enderecos: [],
});

const ClienteForm: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const isEdit = !!id;

  const [activeTab, setActiveTab] = useState<Tab>('cadastro');
  const [form, setForm] = useState<FormData>(emptyForm());
  const [loading, setLoading] = useState(false);
  const [saving, setSaving] = useState(false);
  const [errors, setErrors] = useState<Record<string, string>>({});
  const [cepLoading, setCepLoading] = useState<Record<number, boolean>>({});

  useEffect(() => {
    if (!isEdit) return;
    setLoading(true);
    clientesApi
      .getById(Number(id))
      .then((cliente) => {
        setForm({
          tipoPessoa: (cliente.tipoPessoa as TipoPessoa) ?? 'JURIDICA',
          razaoSocial: cliente.razaoSocial ?? '',
          nomeFantasia: cliente.nomeFantasia ?? '',
          cnpj: cliente.cnpj ?? '',
          cpf: cliente.cpf ?? '',
          ie: cliente.ie ?? '',
          segmento: cliente.segmento ?? '',
          dataFundacao: cliente.dataFundacao ?? '',
          dataInicioCliente: cliente.dataInicioCliente ?? '',
          status: cliente.status ?? 'ATIVO',
          googleDriveFolderId: cliente.googleDriveFolderId ?? '',
          contatos: cliente.contatos ?? [],
          enderecos: cliente.enderecos ?? [],
        });
      })
      .catch(() => navigate('/clientes'))
      .finally(() => setLoading(false));
  }, [id, isEdit, navigate]);

  const setField = <K extends keyof FormData>(key: K, value: FormData[K]) => {
    setForm((prev) => ({ ...prev, [key]: value }));
    setErrors((prev) => ({ ...prev, [key]: '' }));
  };

  const validate = (): boolean => {
    const errs: Record<string, string> = {};
    if (!form.razaoSocial.trim()) {
      errs.razaoSocial = form.tipoPessoa === 'FISICA'
        ? 'Nome completo e obrigatorio.'
        : 'Razao social e obrigatoria.';
    }
    if (form.tipoPessoa === 'JURIDICA' && !form.cnpj.trim()) {
      errs.cnpj = 'CNPJ e obrigatorio.';
    }
    if (form.tipoPessoa === 'FISICA' && !form.cpf.trim()) {
      errs.cpf = 'CPF e obrigatorio.';
    }
    setErrors(errs);
    if (Object.keys(errs).length > 0) {
      setActiveTab('cadastro');
    }
    return Object.keys(errs).length === 0;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!validate()) return;
    setSaving(true);
    try {
      const payload = { ...form };
      if (isEdit) {
        await clientesApi.update(Number(id), payload);
      } else {
        await clientesApi.create(payload);
      }
      navigate('/clientes');
    } catch (err: unknown) {
      const axiosError = err as { response?: { data?: { message?: string } } };
      const msg = axiosError?.response?.data?.message ?? 'Erro ao salvar cliente.';
      setErrors({ submit: msg });
    } finally {
      setSaving(false);
    }
  };

  const addContato = () => setField('contatos', [...form.contatos, emptyContato()]);

  const removeContato = (idx: number) => {
    setField('contatos', form.contatos.filter((_, i) => i !== idx));
  };

  const updateContato = (idx: number, key: keyof Contato, value: string | boolean) => {
    const updated = form.contatos.map((c, i) => {
      if (i !== idx) return c;
      if (key === 'principal' && value === true) {
        return { ...c, [key]: value };
      }
      return { ...c, [key]: value };
    });
    if (key === 'principal' && value === true) {
      const cleared = updated.map((c, i) => ({ ...c, principal: i === idx }));
      setField('contatos', cleared);
    } else {
      setField('contatos', updated);
    }
  };

  const addEndereco = () => setField('enderecos', [...form.enderecos, emptyEndereco()]);

  const removeEndereco = (idx: number) => {
    setField('enderecos', form.enderecos.filter((_, i) => i !== idx));
  };

  const updateEndereco = (idx: number, key: keyof Endereco, value: string) => {
    setField(
      'enderecos',
      form.enderecos.map((e, i) => (i === idx ? { ...e, [key]: value } : e))
    );
  };

  const handleCepSearch = async (idx: number, cep: string) => {
    const cleaned = cep.replace(/\D/g, '');
    if (cleaned.length !== 8) return;
    setCepLoading((prev) => ({ ...prev, [idx]: true }));
    try {
      const data = await clientesApi.searchCep(cleaned);
      if (!data.erro) {
        setField(
          'enderecos',
          form.enderecos.map((e, i) =>
            i === idx
              ? {
                  ...e,
                  logradouro: data.logradouro ?? e.logradouro,
                  bairro: data.bairro ?? e.bairro,
                  cidade: data.cidade ?? e.cidade,
                  estado: data.estado ?? e.estado,
                }
              : e
          )
        );
      }
    } catch {
      // silently ignore CEP lookup errors
    } finally {
      setCepLoading((prev) => ({ ...prev, [idx]: false }));
    }
  };

  const tabs: { key: Tab; label: string }[] = [
    { key: 'cadastro', label: 'Cadastro' },
    { key: 'contatos', label: `Contatos (${form.contatos.length})` },
    { key: 'enderecos', label: `Enderecos (${form.enderecos.length})` },
  ];

  if (loading) {
    return (
      <div className="flex items-center justify-center py-24">
        <div className="animate-spin rounded-full h-10 w-10 border-2 border-blue-500 border-t-transparent" />
      </div>
    );
  }

  return (
    <div className="max-w-4xl mx-auto space-y-6">
      <div>
        <h1 className="text-xl font-bold text-gray-900">
          {isEdit ? 'Editar Cliente' : 'Novo Cliente'}
        </h1>
        <p className="text-sm text-gray-500 mt-0.5">
          {isEdit ? 'Atualize os dados do cliente' : 'Preencha os dados para cadastrar um novo cliente'}
        </p>
      </div>

      <div className="bg-white border border-gray-200 rounded-xl overflow-hidden">
        <div className="flex border-b border-gray-200 overflow-x-auto">
          {tabs.map((tab) => (
            <button
              key={tab.key}
              type="button"
              onClick={() => setActiveTab(tab.key)}
              className={[
                'px-5 py-3.5 text-sm font-medium whitespace-nowrap transition-colors cursor-pointer',
                activeTab === tab.key
                  ? 'text-red-600 border-b-2 border-red-500 bg-red-600/10'
                  : 'text-gray-500 hover:text-gray-800 hover:bg-gray-100/50',
              ].join(' ')}
            >
              {tab.label}
            </button>
          ))}
        </div>

        <form onSubmit={handleSubmit} noValidate>
          <div className="p-6">
            {activeTab === 'cadastro' && (
              <div className="space-y-4">
                {/* Toggle PJ / PF */}
                <div className="flex rounded-lg border border-gray-300 overflow-hidden w-fit">
                  <button
                    type="button"
                    onClick={() => setField('tipoPessoa', 'JURIDICA')}
                    className={[
                      'px-5 py-2 text-sm font-medium transition-colors',
                      form.tipoPessoa === 'JURIDICA'
                        ? 'bg-red-600 text-white'
                        : 'bg-white text-gray-700 border-r border-gray-300 hover:bg-gray-50',
                    ].join(' ')}
                  >
                    Pessoa Juridica
                  </button>
                  <button
                    type="button"
                    onClick={() => setField('tipoPessoa', 'FISICA')}
                    className={[
                      'px-5 py-2 text-sm font-medium transition-colors',
                      form.tipoPessoa === 'FISICA'
                        ? 'bg-red-600 text-white'
                        : 'bg-white text-gray-700 hover:bg-gray-50',
                    ].join(' ')}
                  >
                    Pessoa Fisica
                  </button>
                </div>

                {form.tipoPessoa === 'JURIDICA' && (
                  <>
                    <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                      <Input
                        label="Razao Social *"
                        type="text"
                        placeholder="Razao Social Ltda"
                        value={form.razaoSocial}
                        onChange={(e) => setField('razaoSocial', e.target.value)}
                        error={errors.razaoSocial}
                      />
                      <Input
                        label="Nome Fantasia"
                        type="text"
                        placeholder="Nome Fantasia"
                        value={form.nomeFantasia}
                        onChange={(e) => setField('nomeFantasia', e.target.value)}
                      />
                    </div>
                    <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                      <Input
                        label="CNPJ *"
                        type="text"
                        placeholder="00.000.000/0000-00"
                        value={form.cnpj}
                        onChange={(e) => setField('cnpj', e.target.value)}
                        error={errors.cnpj}
                      />
                      <Input
                        label="Inscricao Estadual"
                        type="text"
                        placeholder="000.000.000.000"
                        value={form.ie}
                        onChange={(e) => setField('ie', e.target.value)}
                      />
                    </div>
                    <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                      <Input
                        label="Segmento"
                        type="text"
                        placeholder="Ex: Industria, Comercio"
                        value={form.segmento}
                        onChange={(e) => setField('segmento', e.target.value)}
                      />
                      <Input
                        label="Data de Fundacao"
                        type="date"
                        value={form.dataFundacao}
                        onChange={(e) => setField('dataFundacao', e.target.value)}
                      />
                      <Input
                        label="Inicio como Cliente"
                        type="date"
                        value={form.dataInicioCliente}
                        onChange={(e) => setField('dataInicioCliente', e.target.value)}
                      />
                    </div>
                  </>
                )}

                {form.tipoPessoa === 'FISICA' && (
                  <>
                    <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                      <Input
                        label="Nome Completo *"
                        type="text"
                        placeholder="Nome completo"
                        value={form.razaoSocial}
                        onChange={(e) => setField('razaoSocial', e.target.value)}
                        error={errors.razaoSocial}
                      />
                      <Input
                        label="CPF *"
                        type="text"
                        placeholder="000.000.000-00"
                        value={form.cpf}
                        onChange={(e) => setField('cpf', e.target.value)}
                        error={errors.cpf}
                      />
                    </div>
                    <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                      <Input
                        label="RG"
                        type="text"
                        placeholder="00.000.000-0"
                        value={form.ie}
                        onChange={(e) => setField('ie', e.target.value)}
                      />
                      <Input
                        label="Segmento"
                        type="text"
                        placeholder="Ex: Industria, Comercio"
                        value={form.segmento}
                        onChange={(e) => setField('segmento', e.target.value)}
                      />
                    </div>
                    <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                      <Input
                        label="Data de Nascimento"
                        type="date"
                        value={form.dataFundacao}
                        onChange={(e) => setField('dataFundacao', e.target.value)}
                      />
                      <Input
                        label="Inicio como Cliente"
                        type="date"
                        value={form.dataInicioCliente}
                        onChange={(e) => setField('dataInicioCliente', e.target.value)}
                      />
                    </div>
                  </>
                )}

                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                  <Input
                    as="select"
                    label="Status"
                    value={form.status}
                    onChange={(e) => setField('status', e.target.value as 'ATIVO' | 'INATIVO')}
                  >
                    <option value="ATIVO">Ativo</option>
                    <option value="INATIVO">Inativo</option>
                  </Input>
                  <Input
                    label="ID Pasta Google Drive"
                    type="text"
                    placeholder="ID da pasta no Google Drive"
                    value={form.googleDriveFolderId}
                    onChange={(e) => setField('googleDriveFolderId', e.target.value)}
                  />
                </div>
              </div>
            )}

            {activeTab === 'contatos' && (
              <div className="space-y-4">
                {form.contatos.length === 0 && (
                  <p className="text-sm text-gray-500 text-center py-6">
                    Nenhum contato adicionado. Clique em "+ Adicionar Contato" para comecar.
                  </p>
                )}
                {form.contatos.map((contato, idx) => (
                  <div
                    key={idx}
                    className="bg-white border border-gray-200 rounded-lg p-4 space-y-3"
                  >
                    <div className="flex items-center justify-between">
                      <span className="text-sm font-medium text-gray-700">
                        Contato {idx + 1}
                        {contato.principal && (
                          <span className="ml-2 text-xs text-red-600 bg-blue-900/40 border border-blue-700/50 px-2 py-0.5 rounded-full">
                            Principal
                          </span>
                        )}
                      </span>
                      <Button
                        type="button"
                        variant="danger"
                        size="sm"
                        onClick={() => removeContato(idx)}
                      >
                        <Trash2 size={14} />
                        Remover
                      </Button>
                    </div>
                    <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
                      <Input
                        label="Nome"
                        type="text"
                        placeholder="Nome completo"
                        value={contato.nome}
                        onChange={(e) => updateContato(idx, 'nome', e.target.value)}
                      />
                      <Input
                        label="Cargo"
                        type="text"
                        placeholder="Cargo ou funcao"
                        value={contato.cargo}
                        onChange={(e) => updateContato(idx, 'cargo', e.target.value)}
                      />
                    </div>
                    <div className="grid grid-cols-1 md:grid-cols-3 gap-3">
                      <Input
                        label="Email"
                        type="email"
                        placeholder="email@empresa.com"
                        value={contato.email}
                        onChange={(e) => updateContato(idx, 'email', e.target.value)}
                      />
                      <Input
                        label="Telefone"
                        type="tel"
                        placeholder="(00) 0000-0000"
                        value={contato.telefone}
                        onChange={(e) => updateContato(idx, 'telefone', e.target.value)}
                      />
                      <Input
                        label="WhatsApp"
                        type="tel"
                        placeholder="(00) 00000-0000"
                        value={contato.whatsapp}
                        onChange={(e) => updateContato(idx, 'whatsapp', e.target.value)}
                      />
                    </div>
                    <label className="flex items-center gap-2 cursor-pointer select-none">
                      <input
                        type="checkbox"
                        checked={contato.principal}
                        onChange={(e) => updateContato(idx, 'principal', e.target.checked)}
                        className="w-4 h-4 rounded accent-blue-500 cursor-pointer"
                      />
                      <span className="text-sm text-gray-700">Contato principal</span>
                    </label>
                  </div>
                ))}
                <Button type="button" variant="secondary" onClick={addContato}>
                  <Plus size={16} />
                  Adicionar Contato
                </Button>
              </div>
            )}

            {activeTab === 'enderecos' && (
              <div className="space-y-4">
                {form.enderecos.length === 0 && (
                  <p className="text-sm text-gray-500 text-center py-6">
                    Nenhum endereco adicionado. Clique em "+ Adicionar Endereco" para comecar.
                  </p>
                )}
                {form.enderecos.map((endereco, idx) => (
                  <div
                    key={idx}
                    className="bg-white border border-gray-200 rounded-lg p-4 space-y-3"
                  >
                    <div className="flex items-center justify-between">
                      <span className="text-sm font-medium text-gray-700">
                        Endereco {idx + 1}
                      </span>
                      <Button
                        type="button"
                        variant="danger"
                        size="sm"
                        onClick={() => removeEndereco(idx)}
                      >
                        <Trash2 size={14} />
                        Remover
                      </Button>
                    </div>
                    <div className="grid grid-cols-1 md:grid-cols-3 gap-3">
                      <Input
                        as="select"
                        label="Tipo"
                        value={endereco.tipo}
                        onChange={(e) =>
                          updateEndereco(idx, 'tipo', e.target.value as Endereco['tipo'])
                        }
                      >
                        <option value="COBRANCA">Cobranca</option>
                        <option value="ENTREGA">Entrega</option>
                        <option value="FILIAL">Filial</option>
                        <option value="OUTRO">Outro</option>
                      </Input>
                      <div className="md:col-span-2">
                        <label className="block text-sm font-medium text-gray-700 mb-1">CEP</label>
                        <div className="flex gap-2">
                          <input
                            type="text"
                            placeholder="00000-000"
                            value={endereco.cep}
                            onChange={(e) => updateEndereco(idx, 'cep', e.target.value)}
                            className="flex-1 bg-white border border-gray-200 rounded-md px-3 py-2 text-sm text-gray-900 placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-red-500"
                          />
                          <Button
                            type="button"
                            variant="secondary"
                            size="md"
                            loading={cepLoading[idx]}
                            onClick={() => handleCepSearch(idx, endereco.cep)}
                            title="Buscar CEP"
                          >
                            <Search size={15} />
                            Buscar
                          </Button>
                        </div>
                      </div>
                    </div>
                    <div className="grid grid-cols-1 md:grid-cols-3 gap-3">
                      <div className="md:col-span-2">
                        <Input
                          label="Logradouro"
                          type="text"
                          placeholder="Rua, Avenida..."
                          value={endereco.logradouro}
                          onChange={(e) => updateEndereco(idx, 'logradouro', e.target.value)}
                        />
                      </div>
                      <Input
                        label="Numero"
                        type="text"
                        placeholder="123"
                        value={endereco.numero}
                        onChange={(e) => updateEndereco(idx, 'numero', e.target.value)}
                      />
                    </div>
                    <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
                      <Input
                        label="Complemento"
                        type="text"
                        placeholder="Apto, Sala, Bloco..."
                        value={endereco.complemento}
                        onChange={(e) => updateEndereco(idx, 'complemento', e.target.value)}
                      />
                      <Input
                        label="Bairro"
                        type="text"
                        placeholder="Bairro"
                        value={endereco.bairro}
                        onChange={(e) => updateEndereco(idx, 'bairro', e.target.value)}
                      />
                    </div>
                    <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
                      <Input
                        label="Cidade"
                        type="text"
                        placeholder="Cidade"
                        value={endereco.cidade}
                        onChange={(e) => updateEndereco(idx, 'cidade', e.target.value)}
                      />
                      <Input
                        label="Estado"
                        type="text"
                        placeholder="UF"
                        value={endereco.estado}
                        onChange={(e) => updateEndereco(idx, 'estado', e.target.value)}
                      />
                    </div>
                  </div>
                ))}
                <Button type="button" variant="secondary" onClick={addEndereco}>
                  <Plus size={16} />
                  Adicionar Endereco
                </Button>
              </div>
            )}
          </div>

          {errors.submit && (
            <div className="mx-6 mb-4 bg-red-50 border border-red-200 rounded-lg px-4 py-3">
              <p className="text-sm text-red-600">{errors.submit}</p>
            </div>
          )}

          <div className="flex items-center justify-end gap-3 px-6 py-4 border-t border-gray-200 bg-white/50">
            <Button
              type="button"
              variant="ghost"
              onClick={() => navigate('/clientes')}
            >
              Cancelar
            </Button>
            <Button type="submit" variant="primary" loading={saving}>
              {saving ? 'Salvando...' : 'Salvar'}
            </Button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default ClienteForm;
