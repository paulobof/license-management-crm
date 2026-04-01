import api from './axios';
import type { AlertaPendente, ConfiguracaoAlerta, NotificacaoSummary } from '../types';

export const getConfig = async (): Promise<ConfiguracaoAlerta> => {
  const response = await api.get<ConfiguracaoAlerta>('/api/v1/alertas/config');
  return response.data;
};

export const updateConfig = async (
  data: Partial<ConfiguracaoAlerta>
): Promise<ConfiguracaoAlerta> => {
  const response = await api.put<ConfiguracaoAlerta>('/api/v1/alertas/config', data);
  return response.data;
};

export const getPendentes = async (): Promise<AlertaPendente[]> => {
  const response = await api.get<AlertaPendente[]>('/api/v1/alertas/pendentes');
  return response.data;
};

export const getSummary = async (): Promise<NotificacaoSummary> => {
  const response = await api.get<NotificacaoSummary>('/api/v1/alertas/summary');
  return response.data;
};

export const snooze = async (documentoId: number, dias: number): Promise<void> => {
  await api.post(`/api/v1/alertas/${documentoId}/snooze`, { dias });
};

export const enviarManual = async (documentoId: number): Promise<void> => {
  await api.post(`/api/v1/alertas/enviar-manual/${documentoId}`);
};
