import React, { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import type { Cliente, CategoriaDocumento } from '../../types';
import * as documentosApi from '../../api/documentos';
import * as clientesApi from '../../api/clientes';
import Button from '../../components/ui/Button';
import Input from '../../components/ui/Input';
import { MAX_UPLOAD_BYTES } from '../../api/documentos';
import { ExternalLink, Paperclip } from 'lucide-react';

const MAX_UPLOAD_MB = Math.round(MAX_UPLOAD_BYTES / (1024 * 1024));

interface FormData {
  clienteId: string;
  nome: string;
  categoria: CategoriaDocumento;
  dataEmissao: string;
  dataValidade: string;
  revisao: string;
  observacoes: string;
}

const emptyForm = (): FormData => ({
  clienteId: '',
  nome: '',
  categoria: 'CONTRATO',
  dataEmissao: '',
  dataValidade: '',
  revisao: '',
  observacoes: '',
});

const DocumentoForm: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const isEdit = !!id;

  const [form, setForm] = useState<FormData>(emptyForm());
  const [arquivo, setArquivo] = useState<File | null>(null);
  const [arquivoAtualUrl, setArquivoAtualUrl] = useState<string>('');
  const [clientes, setClientes] = useState<Cliente[]>([]);
  const [loading, setLoading] = useState(false);
  const [saving, setSaving] = useState(false);
  const [errors, setErrors] = useState<Record<string, string>>({});

  useEffect(() => {
    clientesApi
      .getAll({ size: 1000 })
      .then((page) => setClientes(page.content))
      .catch(() => setClientes([]));
  }, []);

  useEffect(() => {
    if (!isEdit) return;
    setLoading(true);
    documentosApi
      .getById(Number(id))
      .then((doc) => {
        setForm({
          clienteId: String(doc.clienteId),
          nome: doc.nome ?? '',
          categoria: doc.categoria ?? 'CONTRATO',
          dataEmissao: doc.dataEmissao ?? '',
          dataValidade: doc.dataValidade ?? '',
          revisao: doc.revisao ?? '',
          observacoes: doc.observacoes ?? '',
        });
        setArquivoAtualUrl(doc.googleDriveUrl ?? '');
      })
      .catch(() => navigate('/documentos'))
      .finally(() => setLoading(false));
  }, [id, isEdit, navigate]);

  const setField = <K extends keyof FormData>(key: K, value: FormData[K]) => {
    setForm((prev) => ({ ...prev, [key]: value }));
    setErrors((prev) => ({ ...prev, [key]: '' }));
  };

  const validate = (): boolean => {
    const errs: Record<string, string> = {};
    if (!form.clienteId) errs.clienteId = 'Cliente e obrigatorio.';
    if (!form.nome.trim()) errs.nome = 'Nome e obrigatorio.';
    setErrors(errs);
    return Object.keys(errs).length === 0;
  };

  const handleArquivoChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const selecionado = e.target.files?.[0] ?? null;

    if (selecionado && selecionado.size > MAX_UPLOAD_BYTES) {
      e.target.value = '';
      setArquivo(null);
      setErrors((prev) => ({
        ...prev,
        arquivo: `O arquivo excede o limite de ${MAX_UPLOAD_MB} MB.`,
      }));
      return;
    }

    setArquivo(selecionado);
    setErrors((prev) => ({ ...prev, arquivo: '' }));
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (saving) return;
    if (!validate()) return;
    setSaving(true);
    try {
      const payload = {
        clienteId: Number(form.clienteId),
        nome: form.nome,
        categoria: form.categoria,
        dataEmissao: form.dataEmissao || undefined,
        dataValidade: form.dataValidade || undefined,
        revisao: form.revisao,
        observacoes: form.observacoes,
      };
      if (isEdit) {
        await documentosApi.update(Number(id), payload);
      } else if (arquivo) {
        await documentosApi.uploadDocumento({ ...payload, clienteId: Number(form.clienteId) }, arquivo);
      } else {
        await documentosApi.create({ ...payload, clienteId: Number(form.clienteId) });
      }
      navigate('/documentos');
    } catch (err: unknown) {
      const axiosError = err as { response?: { data?: { message?: string } } };
      const msg = axiosError?.response?.data?.message ?? 'Erro ao salvar documento.';
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
          {isEdit ? 'Editar Documento' : 'Novo Documento'}
        </h1>
        <p className="text-sm text-gray-500 mt-0.5">
          {isEdit
            ? 'Atualize os dados do documento'
            : 'Preencha os dados para cadastrar um novo documento'}
        </p>
      </div>

      <div className="bg-white border border-gray-200 rounded-xl overflow-hidden">
        <form onSubmit={handleSubmit} noValidate>
          <div className="p-6 space-y-4">
            <Input
              as="select"
              label="Cliente *"
              value={form.clienteId}
              onChange={(e) => setField('clienteId', e.target.value)}
              error={errors.clienteId}
            >
              <option value="">Selecione um cliente</option>
              {clientes.map((c) => (
                <option key={c.id} value={c.id}>
                  {c.razaoSocial}
                </option>
              ))}
            </Input>

            <Input
              label="Nome *"
              type="text"
              placeholder="Nome do documento"
              value={form.nome}
              onChange={(e) => setField('nome', e.target.value)}
              error={errors.nome}
            />

            <Input
              as="select"
              label="Categoria"
              value={form.categoria}
              onChange={(e) => setField('categoria', e.target.value as CategoriaDocumento)}
            >
              <option value="CONTRATO">Contrato</option>
              <option value="ALVARA">Alvara</option>
              <option value="CERTIFICADO">Certificado</option>
              <option value="LICENCA">Licenca</option>
              <option value="NF">NF</option>
              <option value="OUTRO">Outro</option>
            </Input>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <Input
                label="Data de Emissao"
                type="date"
                value={form.dataEmissao}
                onChange={(e) => setField('dataEmissao', e.target.value)}
              />
              <Input
                label="Data de Validade"
                type="date"
                value={form.dataValidade}
                onChange={(e) => setField('dataValidade', e.target.value)}
              />
            </div>

            <Input
              label="Revisao"
              type="text"
              placeholder="Ex: A, Rev.01, FINAL"
              value={form.revisao}
              onChange={(e) => setField('revisao', e.target.value)}
            />

            {!isEdit ? (
              <div>
                <label
                  htmlFor="documento-arquivo"
                  className="block text-sm font-medium text-gray-700 mb-1"
                >
                  Arquivo
                </label>
                <input
                  id="documento-arquivo"
                  type="file"
                  onChange={handleArquivoChange}
                  aria-invalid={errors.arquivo ? true : undefined}
                  aria-describedby={errors.arquivo ? 'documento-arquivo-error' : undefined}
                  className="w-full bg-white border border-gray-300 rounded-md px-3 py-2 text-sm text-gray-900 file:mr-3 file:py-1 file:px-3 file:rounded-md file:border-0 file:text-sm file:font-medium file:bg-red-50 file:text-red-700 hover:file:bg-red-100 focus:outline-none focus:ring-2 focus:ring-red-500 focus:border-red-500 cursor-pointer"
                />
                {errors.arquivo ? (
                  <p id="documento-arquivo-error" className="mt-1 text-xs text-red-600">
                    {errors.arquivo}
                  </p>
                ) : (
                  <p className="mt-1 text-xs text-gray-500">
                    Opcional. Tamanho maximo de {MAX_UPLOAD_MB} MB. O arquivo e enviado para a pasta
                    do cliente no Google Drive.
                  </p>
                )}
                {arquivo && (
                  <p className="mt-1 text-xs text-gray-600 flex items-center gap-1">
                    <Paperclip size={12} />
                    {arquivo.name}
                  </p>
                )}
              </div>
            ) : (
              <div>
                <span className="block text-sm font-medium text-gray-700 mb-1">Arquivo</span>
                {arquivoAtualUrl ? (
                  <a
                    href={arquivoAtualUrl}
                    target="_blank"
                    rel="noopener noreferrer"
                    className="inline-flex items-center gap-1.5 text-sm text-red-600 hover:text-red-700 hover:underline"
                  >
                    <ExternalLink size={14} />
                    Abrir arquivo no Google Drive
                  </a>
                ) : (
                  <p className="text-sm text-gray-500">
                    Nenhum arquivo enviado. Para anexar um arquivo, cadastre um novo documento.
                  </p>
                )}
              </div>
            )}

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Observacoes
              </label>
              <textarea
                rows={4}
                placeholder="Observacoes sobre o documento..."
                value={form.observacoes}
                onChange={(e) => setField('observacoes', e.target.value)}
                className="w-full bg-white border border-gray-300 rounded-md px-3 py-2 text-gray-900 placeholder-gray-400 text-sm focus:outline-none focus:ring-2 focus:ring-red-500 focus:border-red-500 transition-colors duration-150 resize-none"
              />
            </div>
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
              onClick={() => navigate('/documentos')}
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

export default DocumentoForm;
