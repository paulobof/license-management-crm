import React, { useState } from 'react';
import { Download } from 'lucide-react';
import axios from 'axios';
import Button from './Button';
import { downloadArquivo } from '../../api/export';

interface ExportButtonProps {
  endpoint: string;
  params?: Record<string, unknown>;
  filename: string;
  label?: string;
}

const mensagemDeErro = (erro: unknown): string => {
  if (axios.isAxiosError(erro) && erro.response?.status === 403) {
    return 'Você não tem permissão para exportar estes dados.';
  }
  return 'Não foi possível gerar a exportação. Tente novamente.';
};

const ExportButton: React.FC<ExportButtonProps> = ({
  endpoint,
  params,
  filename,
  label = 'Exportar',
}) => {
  const [loading, setLoading] = useState(false);
  const [erro, setErro] = useState<string | null>(null);

  const handleExport = async () => {
    setLoading(true);
    setErro(null);
    try {
      await downloadArquivo(endpoint, filename, params);
    } catch (e) {
      console.error('Falha ao exportar arquivo', e);
      setErro(mensagemDeErro(e));
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="inline-flex flex-col items-start">
      <Button
        type="button"
        variant="secondary"
        loading={loading}
        onClick={handleExport}
        title="Exportar os dados filtrados para planilha"
      >
        {!loading && <Download className="h-4 w-4" />}
        {loading ? 'Exportando...' : label}
      </Button>
      {erro && (
        <span role="alert" className="mt-1 text-xs text-red-600">
          {erro}
        </span>
      )}
    </div>
  );
};

export default ExportButton;
