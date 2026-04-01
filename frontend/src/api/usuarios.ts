import api from './axios';
import type { Usuario } from '../types';

export const getAll = async (): Promise<Usuario[]> => {
  const response = await api.get<Usuario[]>('/api/v1/users');
  return response.data;
};

interface CreateUsuarioData {
  nome: string;
  email: string;
  senha: string;
  perfil: 'ADMIN' | 'USUARIO';
}

export const create = async (data: CreateUsuarioData): Promise<Usuario> => {
  const response = await api.post<Usuario>('/api/v1/users', data);
  return response.data;
};

interface UpdateUsuarioData {
  nome?: string;
  email?: string;
  perfil?: 'ADMIN' | 'USUARIO';
}

export const update = async (id: number, data: UpdateUsuarioData): Promise<Usuario> => {
  const response = await api.put<Usuario>(`/api/v1/users/${id}`, data);
  return response.data;
};

export const toggleStatus = async (id: number): Promise<Usuario> => {
  const response = await api.patch<Usuario>(`/api/v1/users/${id}/toggle-status`);
  return response.data;
};
