import api from './axios';
import type { Cliente, Page } from '../types';

interface GetAllParams {
  search?: string;
  status?: string;
  page?: number;
  size?: number;
}

export const getAll = async (params: GetAllParams = {}): Promise<Page<Cliente>> => {
  const response = await api.get<Page<Cliente>>('/api/v1/clientes', { params });
  return response.data;
};

export const getById = async (id: number): Promise<Cliente> => {
  const response = await api.get<Cliente>(`/api/v1/clientes/${id}`);
  return response.data;
};

export const create = async (data: Partial<Cliente>): Promise<Cliente> => {
  const response = await api.post<Cliente>('/api/v1/clientes', data);
  return response.data;
};

export const update = async (id: number, data: Partial<Cliente>): Promise<Cliente> => {
  const response = await api.put<Cliente>(`/api/v1/clientes/${id}`, data);
  return response.data;
};

export const remove = async (id: number): Promise<void> => {
  await api.delete(`/api/v1/clientes/${id}`);
};

export const toggleStatus = async (id: number): Promise<Cliente> => {
  const response = await api.patch<Cliente>(`/api/v1/clientes/${id}/toggle-status`);
  return response.data;
};

interface CepResponse {
  cep: string;
  logradouro: string;
  bairro: string;
  cidade: string;
  estado: string;
  erro?: boolean;
}

export const searchCep = async (cep: string): Promise<CepResponse> => {
  const response = await api.get<CepResponse>(`/api/v1/cep/${cep}`);
  return response.data;
};
