import api from './axios';
import type { LoginResponse } from '../types';

/** Resposta genérica dos endpoints de recuperação de senha. */
export interface PasswordResetResponse {
  mensagem: string;
}

export const login = async (email: string, password: string): Promise<LoginResponse> => {
  const response = await api.post<LoginResponse>('/api/v1/auth/login', { email, password });
  return response.data;
};

export const refreshToken = async (): Promise<{ token: string }> => {
  const storedRefreshToken = localStorage.getItem('refreshToken');
  const response = await api.post<{ token: string }>('/api/v1/auth/refresh', {
    refreshToken: storedRefreshToken,
  });
  return response.data;
};

/**
 * Solicita o envio do link de redefinição de senha.
 * A API sempre responde 200 com a mesma mensagem, exista o e-mail ou não.
 */
export const forgotPassword = async (email: string): Promise<PasswordResetResponse> => {
  const response = await api.post<PasswordResetResponse>('/api/v1/auth/forgot-password', { email });
  return response.data;
};

/** Redefine a senha a partir do token recebido por e-mail. */
export const resetPassword = async (
  token: string,
  novaSenha: string,
): Promise<PasswordResetResponse> => {
  const response = await api.post<PasswordResetResponse>('/api/v1/auth/reset-password', {
    token,
    novaSenha,
  });
  return response.data;
};

export const logout = (): void => {
  localStorage.removeItem('token');
  localStorage.removeItem('refreshToken');
  localStorage.removeItem('user');
};
