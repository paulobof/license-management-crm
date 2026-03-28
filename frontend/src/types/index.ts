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

export interface Cliente {
  id: number;
  razaoSocial: string;
  nomeFantasia: string;
  cnpj: string;
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
