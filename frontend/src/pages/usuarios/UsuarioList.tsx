import React, { useState, useEffect, useCallback } from 'react';
import { Plus, PowerOff, Power } from 'lucide-react';
import type { Usuario } from '../../types';
import * as usuariosApi from '../../api/usuarios';
import Button from '../../components/ui/Button';
import Table from '../../components/ui/Table';
import Badge from '../../components/ui/Badge';
import Modal from '../../components/ui/Modal';
import Input from '../../components/ui/Input';

interface NovoUsuarioForm {
  nome: string;
  email: string;
  senha: string;
  perfil: 'ADMIN' | 'USUARIO';
}

const emptyForm = (): NovoUsuarioForm => ({
  nome: '',
  email: '',
  senha: '',
  perfil: 'USUARIO',
});

const UsuarioList: React.FC = () => {
  const [usuarios, setUsuarios] = useState<Usuario[]>([]);
  const [loading, setLoading] = useState(true);
  const [modalOpen, setModalOpen] = useState(false);
  const [form, setForm] = useState<NovoUsuarioForm>(emptyForm());
  const [formErrors, setFormErrors] = useState<Partial<NovoUsuarioForm>>({});
  const [saving, setSaving] = useState(false);
  const [submitError, setSubmitError] = useState('');
  const [togglingId, setTogglingId] = useState<number | null>(null);

  const fetchUsuarios = useCallback(async () => {
    setLoading(true);
    try {
      const data = await usuariosApi.getAll();
      setUsuarios(data);
    } catch {
      setUsuarios([]);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchUsuarios();
  }, [fetchUsuarios]);

  const handleOpenModal = () => {
    setForm(emptyForm());
    setFormErrors({});
    setSubmitError('');
    setModalOpen(true);
  };

  const handleCloseModal = () => {
    setModalOpen(false);
  };

  const setField = <K extends keyof NovoUsuarioForm>(key: K, value: NovoUsuarioForm[K]) => {
    setForm((prev) => ({ ...prev, [key]: value }));
    setFormErrors((prev) => ({ ...prev, [key]: '' }));
  };

  const validate = (): boolean => {
    const errs: Partial<NovoUsuarioForm> = {};
    if (!form.nome.trim()) errs.nome = 'Nome e obrigatorio.';
    if (!form.email.trim()) errs.email = 'Email e obrigatorio.';
    if (!form.senha.trim()) errs.senha = 'Senha e obrigatoria.';
    else if (form.senha.length < 6) errs.senha = 'Senha deve ter pelo menos 6 caracteres.';
    setFormErrors(errs);
    return Object.keys(errs).length === 0;
  };

  const handleSave = async () => {
    if (saving) return;
    if (!validate()) return;
    setSaving(true);
    setSubmitError('');
    try {
      await usuariosApi.create(form);
      setModalOpen(false);
      fetchUsuarios();
    } catch (err: unknown) {
      const axiosError = err as { response?: { data?: { message?: string } } };
      setSubmitError(axiosError?.response?.data?.message ?? 'Erro ao criar usuario.');
    } finally {
      setSaving(false);
    }
  };

  const handleToggle = async (usuario: Usuario) => {
    setTogglingId(usuario.id);
    try {
      const updated = await usuariosApi.toggleStatus(usuario.id);
      setUsuarios((prev) => prev.map((u) => (u.id === updated.id ? updated : u)));
    } catch {
      // silently ignore toggle errors
    } finally {
      setTogglingId(null);
    }
  };

  const columns = [
    {
      key: 'nome',
      header: 'Nome',
      render: (row: Usuario) => (
        <span className="text-gray-900 font-medium">{row.nome}</span>
      ),
    },
    {
      key: 'email',
      header: 'Email',
      render: (row: Usuario) => <span className="text-gray-500">{row.email}</span>,
    },
    {
      key: 'perfil',
      header: 'Perfil',
      render: (row: Usuario) => (
        <span
          className={[
            'inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium',
            row.perfil === 'ADMIN'
              ? 'bg-purple-50 text-purple-700 border border-purple-200'
              : 'bg-gray-100 text-gray-600 border border-gray-200',
          ].join(' ')}
        >
          {row.perfil === 'ADMIN' ? 'Administrador' : 'Usuario'}
        </span>
      ),
    },
    {
      key: 'ativo',
      header: 'Status',
      render: (row: Usuario) => <Badge status={row.ativo ? 'ATIVO' : 'INATIVO'} />,
    },
    {
      key: 'acoes',
      header: 'Acoes',
      className: 'text-right',
      render: (row: Usuario) => (
        <div className="flex items-center justify-end gap-2">
          <Button
            variant={row.ativo ? 'danger' : 'secondary'}
            size="sm"
            loading={togglingId === row.id}
            onClick={() => handleToggle(row)}
            title={row.ativo ? 'Desativar usuario' : 'Ativar usuario'}
          >
            {row.ativo ? <PowerOff size={14} /> : <Power size={14} />}
            {row.ativo ? 'Desativar' : 'Ativar'}
          </Button>
        </div>
      ),
    },
  ];

  return (
    <div className="space-y-5">
      <div className="flex items-center justify-between gap-4 flex-wrap">
        <div>
          <h1 className="text-xl font-bold text-gray-900">Usuarios</h1>
          <p className="text-sm text-gray-500 mt-0.5">
            Gerenciamento de usuarios do sistema
          </p>
        </div>
        <Button variant="primary" onClick={handleOpenModal}>
          <Plus size={16} />
          Novo Usuario
        </Button>
      </div>

      <Table
        columns={columns}
        data={usuarios}
        keyExtractor={(row) => row.id}
        loading={loading}
        emptyMessage="Nenhum usuario encontrado."
      />

      <Modal
        isOpen={modalOpen}
        onClose={handleCloseModal}
        title="Novo Usuario"
        size="md"
        footer={
          <>
            <Button variant="ghost" onClick={handleCloseModal}>
              Cancelar
            </Button>
            <Button variant="primary" loading={saving} onClick={handleSave}>
              {saving ? 'Criando...' : 'Criar Usuario'}
            </Button>
          </>
        }
      >
        <div className="space-y-4">
          <Input
            label="Nome completo"
            type="text"
            placeholder="Nome do usuario"
            value={form.nome}
            onChange={(e) => setField('nome', e.target.value)}
            error={formErrors.nome}
          />
          <Input
            label="Email"
            type="email"
            placeholder="email@prediman.com.br"
            value={form.email}
            onChange={(e) => setField('email', e.target.value)}
            error={formErrors.email}
          />
          <Input
            label="Senha"
            type="password"
            placeholder="Minimo 6 caracteres"
            value={form.senha}
            onChange={(e) => setField('senha', e.target.value)}
            error={formErrors.senha}
          />
          <Input
            as="select"
            label="Perfil"
            value={form.perfil}
            onChange={(e) => setField('perfil', e.target.value as 'ADMIN' | 'USUARIO')}
          >
            <option value="USUARIO">Usuario</option>
            <option value="ADMIN">Administrador</option>
          </Input>
          {submitError && (
            <div className="bg-red-50 border border-red-200 rounded-lg px-4 py-3">
              <p className="text-sm text-red-600">{submitError}</p>
            </div>
          )}
        </div>
      </Modal>
    </div>
  );
};

export default UsuarioList;
