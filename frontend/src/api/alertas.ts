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

/**
 * O backend le "dias" e "tipo" como query params (nao no corpo), e o mesmo
 * endpoint atende alertas de documento e de cobranca. O id refere-se ao
 * documento ou a cobranca, conforme o tipo informado.
 */
export const snooze = async (
  id: number,
  dias: number,
  tipo: AlertaPendente['tipo'] = 'DOCUMENTO'
): Promise<void> => {
  await api.post(`/api/v1/alertas/${id}/snooze`, null, { params: { dias, tipo } });
};

export const enviarManual = async (
  id: number,
  tipo: AlertaPendente['tipo'] = 'DOCUMENTO'
): Promise<void> => {
  await api.post(`/api/v1/alertas/enviar-manual/${id}`, null, { params: { tipo } });
};
