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
  const response = await api.get<Page<Contrato>>('/api/v1/contratos', { params });
  return response.data;
};

export const getById = async (id: number): Promise<Contrato> => {
  const response = await api.get<Contrato>(`/api/v1/contratos/${id}`);
  return response.data;
};

export const getByClienteId = async (clienteId: number): Promise<Contrato[]> => {
  const response = await api.get<Contrato[]>(`/api/v1/clientes/${clienteId}/contratos`);
  return response.data;
};

export const create = async (data: Partial<Contrato>): Promise<Contrato> => {
  const response = await api.post<Contrato>('/api/v1/contratos', data);
  return response.data;
};

export const update = async (id: number, data: Partial<Contrato>): Promise<Contrato> => {
  const response = await api.put<Contrato>(`/api/v1/contratos/${id}`, data);
  return response.data;
};

export const remove = async (id: number): Promise<void> => {
  await api.delete(`/api/v1/contratos/${id}`);
};

export const gerarCobrancas = async (id: number): Promise<void> => {
  await api.post(`/api/v1/contratos/${id}/gerar-cobrancas`);
};
