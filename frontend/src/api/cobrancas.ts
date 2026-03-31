import api from './axios';
import type { Cobranca, FinanceiroSummary, Page } from '../types';

interface GetAllParams {
  contratoId?: number;
  status?: string;
  month?: number;
  year?: number;
  page?: number;
  size?: number;
}

interface RegistrarPagamentoData {
  valorRecebido: number;
  dataPagamento: string;
  formaPagamento: string;
}

export const getAll = async (params: GetAllParams = {}): Promise<Page<Cobranca>> => {
  const response = await api.get<Page<Cobranca>>('/api/cobrancas', { params });
  return response.data;
};

export const getById = async (id: number): Promise<Cobranca> => {
  const response = await api.get<Cobranca>(`/api/cobrancas/${id}`);
  return response.data;
};

export const getByContratoId = async (contratoId: number): Promise<Cobranca[]> => {
  const response = await api.get<Cobranca[]>(`/api/contratos/${contratoId}/cobrancas`);
  return response.data;
};

export const create = async (data: Partial<Cobranca>): Promise<Cobranca> => {
  const response = await api.post<Cobranca>('/api/cobrancas', data);
  return response.data;
};

export const update = async (id: number, data: Partial<Cobranca>): Promise<Cobranca> => {
  const response = await api.put<Cobranca>(`/api/cobrancas/${id}`, data);
  return response.data;
};

export const registrarPagamento = async (
  id: number,
  data: RegistrarPagamentoData
): Promise<Cobranca> => {
  const response = await api.patch<Cobranca>(`/api/cobrancas/${id}/pagar`, data);
  return response.data;
};

export const remove = async (id: number): Promise<void> => {
  await api.delete(`/api/cobrancas/${id}`);
};

export const getFinanceiroSummary = async (): Promise<FinanceiroSummary> => {
  const response = await api.get<FinanceiroSummary>('/api/financeiro/summary');
  return response.data;
};
