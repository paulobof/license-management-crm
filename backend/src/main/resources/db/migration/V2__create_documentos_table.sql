CREATE TABLE documentos (
    id BIGSERIAL PRIMARY KEY,
    cliente_id BIGINT NOT NULL REFERENCES clientes(id) ON DELETE CASCADE,
    nome VARCHAR(255) NOT NULL,
    categoria VARCHAR(30) NOT NULL DEFAULT 'OUTRO',
    data_emissao DATE,
    data_validade DATE,
    revisao VARCHAR(50),
    observacoes TEXT,
    google_drive_file_id VARCHAR(255),
    google_drive_url VARCHAR(500),
    tamanho_bytes BIGINT,
    mime_type VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_documentos_cliente ON documentos(cliente_id);
CREATE INDEX idx_documentos_data_validade ON documentos(data_validade);
CREATE INDEX idx_documentos_categoria ON documentos(categoria);
