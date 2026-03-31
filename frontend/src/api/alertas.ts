import api from './axios';
import type { AlertaPendente, ConfiguracaoAlerta, NotificacaoSummary } from '../types';

export const getConfig = async (): Promise<ConfiguracaoAlerta> => {
  const response = await api.get<ConfiguracaoAlerta>('/api/alertas/config');
  return response.data;
};

export const updateConfig = async (
  data: Partial<ConfiguracaoAlerta>
): Promise<ConfiguracaoAlerta> => {
  const response = await api.put<ConfiguracaoAlerta>('/api/alertas/config', data);
  return response.data;
};

export const getPendentes = async (): Promise<AlertaPendente[]> => {
  const response = await api.get<AlertaPendente[]>('/api/alertas/pendentes');
  return response.data;
};

export const getSummary = async (): Promise<NotificacaoSummary> => {
  const response = await api.get<NotificacaoSummary>('/api/alertas/summary');
  return response.data;
};

export const snooze = async (documentoId: number, dias: number): Promise<void> => {
  await api.post(`/api/alertas/${documentoId}/snooze`, { dias });
};

export const enviarManual = async (documentoId: number): Promise<void> => {
  await api.post(`/api/alertas/enviar-manual/${documentoId}`);
};
