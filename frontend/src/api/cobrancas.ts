import api from './axios';
import type { Cobranca, FinanceiroSummary, Page } from '../types';

/**
 * A resposta do backend inclui a URL de visualizacao do comprovante derivada do
 * comprovanteDriveId. O tipo compartilhado ainda nao expoe esse campo, entao ele
 * e declarado localmente.
 */
export interface CobrancaComComprovante extends Cobranca {
  comprovanteUrl?: string | null;
}

interface GetAllParams {
  contratoId?: number;
  status?: string;
  month?: number;
  year?: number;
  page?: number;
  size?: number;
}

export interface RegistrarPagamentoData {
  valorRecebido: number;
  dataPagamento: string;
  formaPagamento: string;
  comprovanteDriveId?: string | null;
}

export const getAll = async (
  params: GetAllParams = {}
): Promise<Page<CobrancaComComprovante>> => {
  const response = await api.get<Page<CobrancaComComprovante>>('/api/v1/cobrancas', { params });
  return response.data;
};

export const getById = async (id: number): Promise<CobrancaComComprovante> => {
  const response = await api.get<CobrancaComComprovante>(`/api/v1/cobrancas/${id}`);
  return response.data;
};

export const getByContratoId = async (contratoId: number): Promise<CobrancaComComprovante[]> => {
  const response = await api.get<CobrancaComComprovante[]>(
    `/api/v1/contratos/${contratoId}/cobrancas`
  );
  return response.data;
};

export const create = async (data: Partial<Cobranca>): Promise<CobrancaComComprovante> => {
  const response = await api.post<CobrancaComComprovante>('/api/v1/cobrancas', data);
  return response.data;
};

export const update = async (
  id: number,
  data: Partial<Cobranca>
): Promise<CobrancaComComprovante> => {
  const response = await api.put<CobrancaComComprovante>(`/api/v1/cobrancas/${id}`, data);
  return response.data;
};

/**
 * Registra o pagamento de uma cobranca. Quando um arquivo de comprovante e
 * informado, o envio e feito em multipart/form-data (parte "data" com o JSON e
 * parte "file" com o arquivo); caso contrario, mantem o envio JSON tradicional.
 */
export const registrarPagamento = async (
  id: number,
  data: RegistrarPagamentoData,
  file?: File | null
): Promise<CobrancaComComprovante> => {
  if (!file) {
    const response = await api.patch<CobrancaComComprovante>(
      `/api/v1/cobrancas/${id}/pagar`,
      data
    );
    return response.data;
  }

  const formData = new FormData();
  formData.append('file', file);
  formData.append('data', new Blob([JSON.stringify(data)], { type: 'application/json' }));

  const response = await api.patch<CobrancaComComprovante>(
    `/api/v1/cobrancas/${id}/pagar`,
    formData,
    { headers: { 'Content-Type': 'multipart/form-data' } }
  );
  return response.data;
};

export const remove = async (id: number): Promise<void> => {
  await api.delete(`/api/v1/cobrancas/${id}`);
};

export const getFinanceiroSummary = async (): Promise<FinanceiroSummary> => {
  const response = await api.get<FinanceiroSummary>('/api/v1/financeiro/summary');
  return response.data;
};
