import React, { useState } from 'react';
import type { FormEvent } from 'react';
import { Link } from 'react-router-dom';
import { forgotPassword } from '../../api/auth';

/**
 * Mensagem genérica exibida sempre que a solicitação é aceita, exista ou não
 * o e-mail informado (evita revelar quais contas estão cadastradas).
 */
const MENSAGEM_GENERICA =
  'Se o e-mail informado estiver cadastrado, você receberá em instantes um link para redefinir sua senha. Verifique também a caixa de spam.';

const ForgotPassword: React.FC = () => {
  const [email, setEmail] = useState('');
  const [enviado, setEnviado] = useState(false);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    if (loading) return;
    setError('');

    if (!email.trim()) {
      setError('Informe o e-mail cadastrado.');
      return;
    }

    setLoading(true);
    try {
      await forgotPassword(email.trim());
      setEnviado(true);
    } catch (err: unknown) {
      const axiosError = err as { response?: { data?: { message?: string } } };
      setError(
        axiosError?.response?.data?.message ??
          'Não foi possível processar a solicitação. Tente novamente em alguns instantes.',
      );
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-gray-50 flex flex-col items-center justify-center p-8">
      <div className="w-full max-w-sm">
        <div className="text-center mb-8">
          <img src="/logo.jpg" alt="Prediman" className="h-14 mx-auto mb-4 object-contain" />
          <h1 className="text-2xl font-bold text-gray-900">Esqueci minha senha</h1>
          <p className="text-gray-500 text-sm mt-1">
            Informe o e-mail cadastrado e enviaremos um link para você criar uma nova senha.
          </p>
        </div>

        <div className="rounded-xl border border-gray-200 bg-white p-6 shadow-sm">
          {enviado ? (
            <div className="space-y-5">
              <div className="flex items-start gap-2 rounded-lg border border-green-200 bg-green-50 px-4 py-3">
                <svg className="mt-0.5 h-4 w-4 shrink-0 text-green-600" fill="currentColor" viewBox="0 0 20 20">
                  <path fillRule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.793a1 1 0 00-1.414-1.414L9 10.086 7.707 8.793a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clipRule="evenodd" />
                </svg>
                <p className="text-sm text-green-700">{MENSAGEM_GENERICA}</p>
              </div>
              <p className="text-xs text-gray-500">
                O link é válido por 2 horas e pode ser utilizado apenas uma vez.
              </p>
              <Link
                to="/login"
                className="block w-full rounded-lg bg-gradient-to-r from-red-600 to-red-700 px-4 py-2.5 text-center text-sm font-semibold text-white shadow-md shadow-red-600/25 transition-all hover:from-red-700 hover:to-red-800"
              >
                Voltar para o login
              </Link>
            </div>
          ) : (
            <form onSubmit={handleSubmit} noValidate className="space-y-5">
              <div className="flex flex-col gap-1.5">
                <label htmlFor="email" className="text-sm font-medium text-gray-700">
                  E-mail
                </label>
                <input
                  id="email"
                  type="email"
                  placeholder="seu@email.com"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  autoComplete="email"
                  autoFocus
                  className="w-full rounded-lg border border-gray-300 bg-white px-3.5 py-2.5 text-sm text-gray-900 placeholder-gray-400 outline-none transition focus:border-red-500 focus:ring-2 focus:ring-red-500/20 disabled:opacity-50"
                  disabled={loading}
                />
              </div>

              {error && (
                <div className="flex items-start gap-2 rounded-lg border border-red-200 bg-red-50 px-4 py-3">
                  <svg className="mt-0.5 h-4 w-4 shrink-0 text-red-600" fill="currentColor" viewBox="0 0 20 20">
                    <path fillRule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm-.75-9.25a.75.75 0 011.5 0v3.5a.75.75 0 01-1.5 0v-3.5zm.75 6a.75.75 0 100-1.5.75.75 0 000 1.5z" clipRule="evenodd" />
                  </svg>
                  <p className="text-sm text-red-600">{error}</p>
                </div>
              )}

              <button
                type="submit"
                disabled={loading}
                className="w-full rounded-lg bg-gradient-to-r from-red-600 to-red-700 px-4 py-2.5 text-sm font-semibold text-white shadow-md shadow-red-600/25 transition-all hover:from-red-700 hover:to-red-800 hover:shadow-lg hover:shadow-red-600/30 focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-red-600 active:scale-[0.98] disabled:cursor-not-allowed disabled:opacity-60"
              >
                {loading ? 'Enviando...' : 'Enviar link de redefinição'}
              </button>

              <Link
                to="/login"
                className="block text-center text-sm font-medium text-red-600 hover:text-red-700"
              >
                Voltar para o login
              </Link>
            </form>
          )}
        </div>

        <p className="mt-8 text-center text-xs text-gray-400">
          Prediman Engenharia &copy; {new Date().getFullYear()}
        </p>
      </div>
    </div>
  );
};

export default ForgotPassword;
