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
