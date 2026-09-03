import api from './axios';

export type ExportParams = Record<string, unknown>;

/**
 * Remove filtros vazios para não enviar parâmetros inúteis ao backend.
 */
export const limparParams = (params: ExportParams = {}): ExportParams =>
  Object.fromEntries(
    Object.entries(params).filter(
      ([, valor]) => valor !== undefined && valor !== null && valor !== ''
    )
  );

/**
 * Salva o conteúdo recebido como arquivo, usando um link temporário.
 * A URL do objeto é sempre revogada ao final para liberar memória.
 */
export const salvarBlob = (blob: Blob, filename: string): void => {
  const url = URL.createObjectURL(blob);
  try {
    const link = document.createElement('a');
    link.href = url;
    link.download = filename;
    link.style.display = 'none';
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  } finally {
    URL.revokeObjectURL(url);
  }
};

/**
 * Baixa um arquivo gerado pelo backend (ex.: CSV de documentos ou de cobranças),
 * repassando os filtros ativos da tela.
 */
export const downloadArquivo = async (
  endpoint: string,
  filename: string,
  params: ExportParams = {}
): Promise<void> => {
  const response = await api.get<Blob>(endpoint, {
    params: limparParams(params),
    responseType: 'blob',
  });
  salvarBlob(response.data, filename);
};
