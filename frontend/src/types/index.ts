export interface Usuario {
  id: number;
  nome: string;
  email: string;
  perfil: 'ADMIN' | 'USUARIO';
  ativo: boolean;
}

export interface LoginResponse {
  token: string;
  refreshToken: string;
  nome: string;
  perfil: 'ADMIN' | 'USUARIO';
}

export type TipoPessoa = 'FISICA' | 'JURIDICA';

export interface Cliente {
  id: number;
  tipoPessoa: TipoPessoa;
  razaoSocial: string;
  nomeFantasia: string;
  cnpj: string;
  cpf: string;
  ie: string;
  segmento: string;
  dataFundacao: string;
  dataInicioCliente: string;
  status: 'ATIVO' | 'INATIVO';
  googleDriveFolderId: string;
  contatos: Contato[];
  enderecos: Endereco[];
  createdAt: string;
  updatedAt: string;
}

export interface Contato {
  id?: number;
  nome: string;
  cargo: string;
  email: string;
  telefone: string;
  whatsapp: string;
  principal: boolean;
}

export interface Endereco {
  id?: number;
  tipo: 'COBRANCA' | 'ENTREGA' | 'FILIAL' | 'OUTRO';
  cep: string;
  logradouro: string;
  numero: string;
  complemento: string;
  bairro: string;
  cidade: string;
  estado: string;
}

export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}

export type CategoriaDocumento = 'CONTRATO' | 'ALVARA' | 'CERTIFICADO' | 'LICENCA' | 'NF' | 'OUTRO';
export type StatusDocumento = 'VALIDO' | 'A_VENCER' | 'VENCIDO' | 'SEM_VALIDADE';

export interface Documento {
  id: number;
  nome: string;
  categoria: CategoriaDocumento;
  dataEmissao: string;
  dataValidade: string | null;
  revisao: string;
  observacoes: string;
  googleDriveFileId: string;
  googleDriveUrl: string;
  tamanhoBytes: number;
  mimeType: string;
  statusCalculado: StatusDocumento;
  clienteId: number;
  clienteNome: string;
  createdAt: string;
  updatedAt: string;
}

export interface DashboardSummary {
  totalClientes: number;
  clientesAtivos: number;
  documentosAVencer: number;
  documentosVencidos: number;
}

// Financeiro types
export type Periodicidade = 'MENSAL' | 'TRIMESTRAL' | 'SEMESTRAL' | 'ANUAL' | 'AVULSO';
export type StatusContrato = 'ATIVO' | 'ENCERRADO' | 'CANCELADO';
export type StatusCobranca = 'PENDENTE' | 'PAGO' | 'VENCIDO' | 'CANCELADO';

export interface Contrato {
  id: number;
  descricao: string;
  valor: number;
  periodicidade: Periodicidade;
  dataInicio: string;
  dataFim: string | null;
  status: StatusContrato;
  observacoes: string;
  clienteId: number;
  clienteNome: string;
  cobrancas: Cobranca[];
  createdAt: string;
  updatedAt: string;
}

export interface Cobranca {
  id: number;
  valorEsperado: number;
  valorRecebido: number | null;
  dataVencimento: string;
  dataPagamento: string | null;
  formaPagamento: string | null;
  comprovanteDriveId: string | null;
  status: StatusCobranca;
  statusCalculado: StatusCobranca;
  contratoId: number;
  createdAt: string;
  updatedAt: string;
}

export interface FinanceiroSummary {
  aReceber: number;
  recebido: number;
  emAtraso: number;
  vencendo7dias: number;
}

// Alerta types
export interface AlertaPendente {
  id: number;
  tipo: 'DOCUMENTO' | 'COBRANCA';
  nome: string;
  clienteNome: string;
  dataVencimento: string;
  diasRestantes: number;
  status: 'A_VENCER' | 'VENCIDO';
}

export interface NotificacaoSummary {
  totalPendentes: number;
  documentosAVencer: number;
  documentosVencidos: number;
  cobrancasVencidas: number;
}

export interface ConfiguracaoAlerta {
  id: number;
  diasAntecedencia: string;
  horarioExecucao: string;
  emailAtivo: boolean;
  whatsappAtivo: boolean;
  templateEmail: string;
  templateWhatsapp: string;
}
