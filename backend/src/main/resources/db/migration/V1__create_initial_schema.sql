CREATE TABLE usuarios (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    senha_hash VARCHAR(255) NOT NULL,
    perfil VARCHAR(20) NOT NULL DEFAULT 'USUARIO',
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    ultimo_login TIMESTAMP
);

CREATE TABLE clientes (
    id BIGSERIAL PRIMARY KEY,
    razao_social VARCHAR(255) NOT NULL,
    nome_fantasia VARCHAR(255),
    cnpj VARCHAR(20) UNIQUE,
    ie VARCHAR(30),
    segmento VARCHAR(100),
    data_fundacao DATE,
    data_inicio_cliente DATE,
    status VARCHAR(20) NOT NULL DEFAULT 'ATIVO',
    google_drive_folder_id VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE contatos (
    id BIGSERIAL PRIMARY KEY,
    cliente_id BIGINT NOT NULL REFERENCES clientes(id) ON DELETE CASCADE,
    nome VARCHAR(255) NOT NULL,
    cargo VARCHAR(100),
    email VARCHAR(255),
    telefone VARCHAR(30),
    whatsapp VARCHAR(30),
    principal BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE enderecos (
    id BIGSERIAL PRIMARY KEY,
    cliente_id BIGINT NOT NULL REFERENCES clientes(id) ON DELETE CASCADE,
    tipo VARCHAR(20) NOT NULL DEFAULT 'COBRANCA',
    cep VARCHAR(10),
    logradouro VARCHAR(255),
    numero VARCHAR(20),
    complemento VARCHAR(100),
    bairro VARCHAR(100),
    cidade VARCHAR(100),
    estado VARCHAR(2)
);

CREATE INDEX idx_clientes_cnpj ON clientes(cnpj);
CREATE INDEX idx_clientes_status ON clientes(status);
CREATE INDEX idx_contatos_cliente ON contatos(cliente_id);
CREATE INDEX idx_enderecos_cliente ON enderecos(cliente_id);
