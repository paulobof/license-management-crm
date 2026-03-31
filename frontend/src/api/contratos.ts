import api from './axios';
import type { Contrato, Page } from '../types';

interface GetAllParams {
  search?: string;
  clienteId?: number;
  status?: string;
  periodicidade?: string;
  page?: number;
  size?: number;
}

export const getAll = async (params: GetAllParams = {}): Promise<Page<Contrato>> => {
  const response = await api.get<Page<Contrato>>('/api/contratos', { params });
  return response.data;
};

export const getById = async (id: number): Promise<Contrato> => {
  const response = await api.get<Contrato>(`/api/contratos/${id}`);
  return response.data;
};

export const getByClienteId = async (clienteId: number): Promise<Contrato[]> => {
  const response = await api.get<Contrato[]>(`/api/clientes/${clienteId}/contratos`);
  return response.data;
};

export const create = async (data: Partial<Contrato>): Promise<Contrato> => {
  const response = await api.post<Contrato>('/api/contratos', data);
  return response.data;
};

export const update = async (id: number, data: Partial<Contrato>): Promise<Contrato> => {
  const response = await api.put<Contrato>(`/api/contratos/${id}`, data);
  return response.data;
};

export const remove = async (id: number): Promise<void> => {
  await api.delete(`/api/contratos/${id}`);
};

export const gerarCobrancas = async (id: number): Promise<void> => {
  await api.post(`/api/contratos/${id}/gerar-cobrancas`);
};
