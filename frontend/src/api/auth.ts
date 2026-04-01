import api from './axios';
import type { LoginResponse } from '../types';

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

export const logout = (): void => {
  localStorage.removeItem('token');
  localStorage.removeItem('refreshToken');
  localStorage.removeItem('user');
};
