import React, { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import type { Periodicidade } from '../../types';
import * as contratosApi from '../../api/contratos';
import * as clientesApi from '../../api/clientes';
import type { Cliente } from '../../types';
import Button from '../../components/ui/Button';
import Input from '../../components/ui/Input';

interface FormData {
  clienteId: string;
  descricao: string;
  valor: string;
  periodicidade: Periodicidade;
  dataInicio: string;
  dataFim: string;
  observacoes: string;
}

const emptyForm = (): FormData => ({
  clienteId: '',
  descricao: '',
  valor: '',
  periodicidade: 'MENSAL',
  dataInicio: '',
  dataFim: '',
  observacoes: '',
});

const ContratoForm: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const isEdit = !!id;

  const [form, setForm] = useState<FormData>(emptyForm());
  const [clientes, setClientes] = useState<Cliente[]>([]);
  const [loading, setLoading] = useState(false);
  const [saving, setSaving] = useState(false);
  const [errors, setErrors] = useState<Record<string, string>>({});

  useEffect(() => {
    clientesApi
      .getAll({ status: 'ATIVO', size: 200 })
      .then((page) => setClientes(page.content))
      .catch(() => setClientes([]));
  }, []);

  useEffect(() => {
    if (!isEdit) return;
    setLoading(true);
    contratosApi
      .getById(Number(id))
      .then((contrato) => {
        setForm({
          clienteId: String(contrato.clienteId),
          descricao: contrato.descricao ?? '',
          valor: String(contrato.valor ?? ''),
          periodicidade: contrato.periodicidade ?? 'MENSAL',
          dataInicio: contrato.dataInicio ?? '',
          dataFim: contrato.dataFim ?? '',
          observacoes: contrato.observacoes ?? '',
        });
      })
      .catch(() => navigate('/contratos'))
      .finally(() => setLoading(false));
  }, [id, isEdit, navigate]);

  const setField = <K extends keyof FormData>(key: K, value: FormData[K]) => {
    setForm((prev) => ({ ...prev, [key]: value }));
    setErrors((prev) => ({ ...prev, [key]: '' }));
  };

  const validate = (): boolean => {
    const errs: Record<string, string> = {};
    if (!form.clienteId) errs.clienteId = 'Cliente e obrigatorio.';
    if (!form.descricao.trim()) errs.descricao = 'Descricao e obrigatoria.';
    if (!form.valor || isNaN(Number(form.valor)) || Number(form.valor) <= 0) {
      errs.valor = 'Valor deve ser maior que zero.';
    }
    if (!form.dataInicio) errs.dataInicio = 'Data de inicio e obrigatoria.';
    setErrors(errs);
    return Object.keys(errs).length === 0;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!validate()) return;
    setSaving(true);
    try {
      const payload = {
        clienteId: Number(form.clienteId),
        descricao: form.descricao,
        valor: Number(form.valor),
        periodicidade: form.periodicidade,
        dataInicio: form.dataInicio,
        dataFim: form.dataFim || null,
        observacoes: form.observacoes,
      };
      if (isEdit) {
        await contratosApi.update(Number(id), payload);
      } else {
        await contratosApi.create(payload);
      }
      navigate('/contratos');
    } catch (err: unknown) {
      const axiosError = err as { response?: { data?: { message?: string } } };
      const msg = axiosError?.response?.data?.message ?? 'Erro ao salvar contrato.';
      setErrors({ submit: msg });
    } finally {
      setSaving(false);
    }
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center py-24">
        <div className="animate-spin rounded-full h-10 w-10 border-2 border-red-600 border-t-transparent" />
      </div>
    );
  }

  return (
    <div className="max-w-2xl mx-auto space-y-6">
      <div>
        <h1 className="text-xl font-bold text-gray-900">
          {isEdit ? 'Editar Contrato' : 'Novo Contrato'}
        </h1>
        <p className="text-sm text-gray-500 mt-0.5">
          {isEdit
            ? 'Atualize os dados do contrato'
            : 'Preencha os dados para cadastrar um novo contrato'}
        </p>
      </div>

      <div className="bg-white border border-gray-200 rounded-xl overflow-hidden">
        <form onSubmit={handleSubmit} noValidate>
          <div className="p-6 space-y-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Cliente *
              </label>
              <select
                value={form.clienteId}
                onChange={(e) => setField('clienteId', e.target.value)}
                className={[
                  'w-full bg-white border rounded-md px-3 py-2 text-sm text-gray-900 focus:outline-none focus:ring-2 focus:ring-red-500 focus:border-red-500 cursor-pointer',
                  errors.clienteId ? 'border-red-500' : 'border-gray-300',
                ].join(' ')}
              >
                <option value="">Selecione um cliente</option>
                {clientes.map((c) => (
                  <option key={c.id} value={c.id}>
                    {c.razaoSocial}
                  </option>
                ))}
              </select>
              {errors.clienteId && (
                <p className="mt-1 text-xs text-red-600">{errors.clienteId}</p>
              )}
            </div>

            <Input
              label="Descricao *"
              type="text"
              placeholder="Descricao do contrato"
              value={form.descricao}
              onChange={(e) => setField('descricao', e.target.value)}
              error={errors.descricao}
            />

            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <Input
                label="Valor (R$) *"
                type="number"
                placeholder="0.00"
                value={form.valor}
                onChange={(e) => setField('valor', e.target.value)}
                error={errors.valor}
              />
              <Input
                as="select"
                label="Periodicidade"
                value={form.periodicidade}
                onChange={(e) => setField('periodicidade', e.target.value as Periodicidade)}
              >
                <option value="MENSAL">Mensal</option>
                <option value="TRIMESTRAL">Trimestral</option>
                <option value="SEMESTRAL">Semestral</option>
                <option value="ANUAL">Anual</option>
                <option value="AVULSO">Avulso</option>
              </Input>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <Input
                label="Data de Inicio *"
                type="date"
                value={form.dataInicio}
                onChange={(e) => setField('dataInicio', e.target.value)}
                error={errors.dataInicio}
              />
              <Input
                label="Data de Fim"
                type="date"
                value={form.dataFim}
                onChange={(e) => setField('dataFim', e.target.value)}
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Observacoes
              </label>
              <textarea
                rows={3}
                placeholder="Observacoes adicionais..."
                value={form.observacoes}
                onChange={(e) => setField('observacoes', e.target.value)}
                className="w-full bg-white border border-gray-300 rounded-md px-3 py-2 text-sm text-gray-900 placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-red-500 focus:border-red-500 resize-none"
              />
            </div>
          </div>

          {errors.submit && (
            <div className="mx-6 mb-4 bg-red-50 border border-red-200 rounded-lg px-4 py-3">
              <p className="text-sm text-red-600">{errors.submit}</p>
            </div>
          )}

          <div className="flex items-center justify-end gap-3 px-6 py-4 border-t border-gray-200 bg-white/50">
            <Button type="button" variant="ghost" onClick={() => navigate('/contratos')}>
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

export default ContratoForm;
