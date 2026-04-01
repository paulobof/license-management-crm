import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { ArrowLeft } from 'lucide-react';
import type { ConfiguracaoAlerta } from '../../types';
import * as alertasApi from '../../api/alertas';
import Button from '../../components/ui/Button';
import Input from '../../components/ui/Input';

interface FormData {
  diasAntecedencia: string;
  horarioExecucao: string;
  emailAtivo: boolean;
  whatsappAtivo: boolean;
  templateEmail: string;
  templateWhatsapp: string;
}

const emptyForm = (): FormData => ({
  diasAntecedencia: '30,15,7,1',
  horarioExecucao: '08:00',
  emailAtivo: true,
  whatsappAtivo: false,
  templateEmail: '',
  templateWhatsapp: '',
});

const AlertaConfig: React.FC = () => {
  const navigate = useNavigate();
  const [form, setForm] = useState<FormData>(emptyForm());
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [errors, setErrors] = useState<Record<string, string>>({});
  const [successMsg, setSuccessMsg] = useState('');

  useEffect(() => {
    alertasApi
      .getConfig()
      .then((config: ConfiguracaoAlerta) => {
        setForm({
          diasAntecedencia: config.diasAntecedencia ?? '30,15,7,1',
          horarioExecucao: config.horarioExecucao ?? '08:00',
          emailAtivo: config.emailAtivo ?? true,
          whatsappAtivo: config.whatsappAtivo ?? false,
          templateEmail: config.templateEmail ?? '',
          templateWhatsapp: config.templateWhatsapp ?? '',
        });
      })
      .catch(() => {
        // use default values if config not found
      })
      .finally(() => setLoading(false));
  }, []);

  const setField = <K extends keyof FormData>(key: K, value: FormData[K]) => {
    setForm((prev) => ({ ...prev, [key]: value }));
    setErrors((prev) => ({ ...prev, [key]: '' }));
    setSuccessMsg('');
  };

  const validate = (): boolean => {
    const errs: Record<string, string> = {};
    if (!form.diasAntecedencia.trim()) {
      errs.diasAntecedencia = 'Informe os dias de antecedencia separados por virgula.';
    }
    if (!form.horarioExecucao) {
      errs.horarioExecucao = 'Horario de execucao e obrigatorio.';
    }
    setErrors(errs);
    return Object.keys(errs).length === 0;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (saving) return;
    if (!validate()) return;
    setSaving(true);
    setSuccessMsg('');
    try {
      await alertasApi.updateConfig({
        diasAntecedencia: form.diasAntecedencia,
        horarioExecucao: form.horarioExecucao,
        emailAtivo: form.emailAtivo,
        whatsappAtivo: form.whatsappAtivo,
        templateEmail: form.templateEmail,
        templateWhatsapp: form.templateWhatsapp,
      });
      setSuccessMsg('Configuracoes salvas com sucesso.');
    } catch (err: unknown) {
      const axiosError = err as { response?: { data?: { message?: string } } };
      setErrors({
        submit: axiosError?.response?.data?.message ?? 'Erro ao salvar configuracoes.',
      });
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
      <div className="flex items-center gap-3">
        <button
          onClick={() => navigate('/alertas')}
          className="text-gray-500 hover:text-gray-900 p-1.5 rounded-lg hover:bg-white transition-colors cursor-pointer"
          aria-label="Voltar"
        >
          <ArrowLeft size={18} />
        </button>
        <div>
          <h1 className="text-xl font-bold text-gray-900">Configuracao de Alertas</h1>
          <p className="text-sm text-gray-500 mt-0.5">
            Defina como e quando os alertas serao enviados
          </p>
        </div>
      </div>

      <div className="bg-white border border-gray-200 rounded-xl overflow-hidden">
        <form onSubmit={handleSubmit} noValidate>
          <div className="p-6 space-y-6">
            <div className="space-y-4">
              <h2 className="text-sm font-semibold text-gray-500 uppercase tracking-wider">
                Agendamento
              </h2>
              <Input
                label="Dias de Antecedencia"
                type="text"
                placeholder="30,15,7,1"
                value={form.diasAntecedencia}
                onChange={(e) => setField('diasAntecedencia', e.target.value)}
                error={errors.diasAntecedencia}
              />
              <p className="text-xs text-gray-400 -mt-2">
                Informe os dias separados por virgula. Ex: 30,15,7,1
              </p>
              <Input
                label="Horario de Execucao"
                type="time"
                value={form.horarioExecucao}
                onChange={(e) => setField('horarioExecucao', e.target.value)}
                error={errors.horarioExecucao}
              />
            </div>

            <div className="space-y-4 pt-2 border-t border-gray-100">
              <h2 className="text-sm font-semibold text-gray-500 uppercase tracking-wider">
                Canais de Notificacao
              </h2>
              <label className="flex items-center gap-3 cursor-pointer select-none">
                <input
                  type="checkbox"
                  checked={form.emailAtivo}
                  onChange={(e) => setField('emailAtivo', e.target.checked)}
                  className="w-4 h-4 rounded accent-red-600 cursor-pointer"
                />
                <span className="text-sm text-gray-700 font-medium">E-mail Ativo</span>
              </label>
              <label className="flex items-center gap-3 cursor-pointer select-none">
                <input
                  type="checkbox"
                  checked={form.whatsappAtivo}
                  onChange={(e) => setField('whatsappAtivo', e.target.checked)}
                  className="w-4 h-4 rounded accent-red-600 cursor-pointer"
                />
                <span className="text-sm text-gray-700 font-medium">WhatsApp Ativo</span>
              </label>
            </div>

            <div className="space-y-4 pt-2 border-t border-gray-100">
              <h2 className="text-sm font-semibold text-gray-500 uppercase tracking-wider">
                Templates
              </h2>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Template E-mail
                </label>
                <textarea
                  rows={4}
                  placeholder="Ex: Prezado {{cliente}}, o documento {{documento}} vence em {{dias}} dias."
                  value={form.templateEmail}
                  onChange={(e) => setField('templateEmail', e.target.value)}
                  className="w-full bg-white border border-gray-300 rounded-md px-3 py-2 text-sm text-gray-900 placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-red-500 focus:border-red-500 resize-none"
                />
                <p className="mt-1 text-xs text-gray-400">
                  Variaveis disponiveis: <code className="bg-gray-100 px-1 rounded">{'{{cliente}}'}</code>{' '}
                  <code className="bg-gray-100 px-1 rounded">{'{{documento}}'}</code>{' '}
                  <code className="bg-gray-100 px-1 rounded">{'{{dias}}'}</code>
                </p>
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Template WhatsApp
                </label>
                <textarea
                  rows={4}
                  placeholder="Ex: Ola {{cliente}}, lembrete: {{documento}} vence em {{dias}} dias."
                  value={form.templateWhatsapp}
                  onChange={(e) => setField('templateWhatsapp', e.target.value)}
                  className="w-full bg-white border border-gray-300 rounded-md px-3 py-2 text-sm text-gray-900 placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-red-500 focus:border-red-500 resize-none"
                />
                <p className="mt-1 text-xs text-gray-400">
                  Variaveis disponiveis: <code className="bg-gray-100 px-1 rounded">{'{{cliente}}'}</code>{' '}
                  <code className="bg-gray-100 px-1 rounded">{'{{documento}}'}</code>{' '}
                  <code className="bg-gray-100 px-1 rounded">{'{{dias}}'}</code>
                </p>
              </div>
            </div>
          </div>

          {errors.submit && (
            <div className="mx-6 mb-4 bg-red-50 border border-red-200 rounded-lg px-4 py-3">
              <p className="text-sm text-red-600">{errors.submit}</p>
            </div>
          )}

          {successMsg && (
            <div className="mx-6 mb-4 bg-green-50 border border-green-200 rounded-lg px-4 py-3">
              <p className="text-sm text-green-700">{successMsg}</p>
            </div>
          )}

          <div className="flex items-center justify-end gap-3 px-6 py-4 border-t border-gray-200 bg-white/50">
            <Button type="button" variant="ghost" onClick={() => navigate('/alertas')}>
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

export default AlertaConfig;
