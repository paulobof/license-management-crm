import api from './axios';
import type { Documento, Page, DashboardSummary } from '../types';

interface GetAllParams {
  search?: string;
  categoria?: string;
  status?: string;
  clienteId?: number;
  page?: number;
  size?: number;
}

export const getAll = async (params: GetAllParams = {}): Promise<Page<Documento>> => {
  const response = await api.get<Page<Documento>>('/api/v1/documentos', { params });
  return response.data;
};

export const getById = async (id: number): Promise<Documento> => {
  const response = await api.get<Documento>(`/api/v1/documentos/${id}`);
  return response.data;
};

export const getByClienteId = async (clienteId: number): Promise<Documento[]> => {
  const response = await api.get<Documento[]>(`/api/v1/clientes/${clienteId}/documentos`);
  return response.data;
};

export const create = async (data: Partial<Documento> & { clienteId: number }): Promise<Documento> => {
  const response = await api.post<Documento>('/api/v1/documentos', data);
  return response.data;
};

/** Limite de tamanho de arquivo aceito no upload (30 MB, igual ao configurado no backend). */
export const MAX_UPLOAD_BYTES = 30 * 1024 * 1024;

export const uploadDocumento = async (
  data: Partial<Documento> & { clienteId: number },
  file: File
): Promise<Documento> => {
  const formData = new FormData();
  // A parte "data" precisa ser um Blob com type application/json para o @RequestPart do Spring desserializar.
  formData.append('data', new Blob([JSON.stringify(data)], { type: 'application/json' }));
  formData.append('file', file);

  const response = await api.post<Documento>('/api/v1/documentos/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  });
  return response.data;
};

export const update = async (id: number, data: Partial<Documento>): Promise<Documento> => {
  const response = await api.put<Documento>(`/api/v1/documentos/${id}`, data);
  return response.data;
};

export const remove = async (id: number): Promise<void> => {
  await api.delete(`/api/v1/documentos/${id}`);
};

export const getDashboardSummary = async (): Promise<DashboardSummary> => {
  const response = await api.get<DashboardSummary>('/api/v1/dashboard/summary');
  return response.data;
};
