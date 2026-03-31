import React, { useState } from 'react';
import type { FormEvent } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';

const Login: React.FC = () => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const { login } = useAuth();
  const navigate = useNavigate();

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setError('');

    if (!email.trim() || !password.trim()) {
      setError('Preencha o email e a senha.');
      return;
    }

    setLoading(true);
    try {
      await login(email, password);
      navigate('/', { replace: true });
    } catch (err: unknown) {
      const axiosError = err as { response?: { data?: { message?: string } } };
      const msg = axiosError?.response?.data?.message;
      setError(msg ?? 'Email ou senha invalidos. Tente novamente.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-[#F9FAFB] flex flex-col items-center justify-center p-4">
      <div className="w-full max-w-sm">

        {/* Header / Logo */}
        <div className="text-center mb-8">
          <img
            src="/logo.jpg"
            alt="Prediman"
            className="h-16 mx-auto mb-4 object-contain"
          />
          <h1 className="text-2xl font-bold text-[#1F2937] tracking-tight">
            Prediman CRM
          </h1>
          <p className="text-sm text-gray-500 mt-1">
            Sistema de Gestão de Licenças e Clientes
          </p>
        </div>

        {/* Card */}
        <div className="bg-white border border-gray-200 rounded-2xl p-8 shadow-sm">
          <form onSubmit={handleSubmit} noValidate className="space-y-5">

            {/* Email */}
            <div className="flex flex-col gap-1.5">
              <label
                htmlFor="email"
                className="text-sm font-medium text-[#1F2937]"
              >
                Email
              </label>
              <input
                id="email"
                type="email"
                placeholder="seu@email.com"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                autoComplete="email"
                autoFocus
                className="w-full rounded-lg border border-gray-300 bg-white px-3.5 py-2.5 text-sm text-[#1F2937] placeholder-gray-400 outline-none transition focus:border-[#DC2626] focus:ring-2 focus:ring-[#DC2626]/20 disabled:opacity-50"
                disabled={loading}
              />
            </div>

            {/* Password */}
            <div className="flex flex-col gap-1.5">
              <label
                htmlFor="password"
                className="text-sm font-medium text-[#1F2937]"
              >
                Senha
              </label>
              <input
                id="password"
                type="password"
                placeholder="••••••••"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                autoComplete="current-password"
                className="w-full rounded-lg border border-gray-300 bg-white px-3.5 py-2.5 text-sm text-[#1F2937] placeholder-gray-400 outline-none transition focus:border-[#DC2626] focus:ring-2 focus:ring-[#DC2626]/20 disabled:opacity-50"
                disabled={loading}
              />
            </div>

            {/* Error message */}
            {error && (
              <div className="flex items-start gap-2 rounded-lg border border-red-200 bg-red-50 px-4 py-3">
                <svg
                  className="mt-0.5 h-4 w-4 shrink-0 text-[#DC2626]"
                  fill="currentColor"
                  viewBox="0 0 20 20"
                  aria-hidden="true"
                >
                  <path
                    fillRule="evenodd"
                    d="M10 18a8 8 0 100-16 8 8 0 000 16zm-.75-9.25a.75.75 0 011.5 0v3.5a.75.75 0 01-1.5 0v-3.5zm.75 6a.75.75 0 100-1.5.75.75 0 000 1.5z"
                    clipRule="evenodd"
                  />
                </svg>
                <p className="text-sm text-[#DC2626]">{error}</p>
              </div>
            )}

            {/* Submit button */}
            <button
              type="submit"
              disabled={loading}
              className="mt-1 w-full rounded-lg bg-[#DC2626] px-4 py-2.5 text-sm font-semibold text-white shadow-sm transition hover:bg-[#991B1B] focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-[#DC2626] active:scale-[0.98] disabled:cursor-not-allowed disabled:opacity-60"
            >
              {loading ? (
                <span className="flex items-center justify-center gap-2">
                  <svg
                    className="h-4 w-4 animate-spin text-white"
                    fill="none"
                    viewBox="0 0 24 24"
                    aria-hidden="true"
                  >
                    <circle
                      className="opacity-25"
                      cx="12"
                      cy="12"
                      r="10"
                      stroke="currentColor"
                      strokeWidth="4"
                    />
                    <path
                      className="opacity-75"
                      fill="currentColor"
                      d="M4 12a8 8 0 018-8v4a4 4 0 00-4 4H4z"
                    />
                  </svg>
                  Entrando...
                </span>
              ) : (
                'Entrar'
              )}
            </button>

          </form>
        </div>

        {/* Footer */}
        <p className="mt-6 text-center text-xs text-gray-400">
          Prediman Engenharia &copy; 2026
        </p>

      </div>
    </div>
  );
};

export default Login;
