CREATE TABLE contratos (
    id BIGSERIAL PRIMARY KEY,
    cliente_id BIGINT NOT NULL REFERENCES clientes(id) ON DELETE CASCADE,
    descricao VARCHAR(255) NOT NULL,
    valor DECIMAL(12,2) NOT NULL,
    periodicidade VARCHAR(20) NOT NULL DEFAULT 'MENSAL',
    data_inicio DATE NOT NULL,
    data_fim DATE,
    status VARCHAR(20) NOT NULL DEFAULT 'ATIVO',
    observacoes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE cobrancas (
    id BIGSERIAL PRIMARY KEY,
    contrato_id BIGINT NOT NULL REFERENCES contratos(id) ON DELETE CASCADE,
    valor_esperado DECIMAL(12,2) NOT NULL,
    valor_recebido DECIMAL(12,2),
    data_vencimento DATE NOT NULL,
    data_pagamento DATE,
    forma_pagamento VARCHAR(50),
    comprovante_drive_id VARCHAR(255),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDENTE',
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_contratos_cliente ON contratos(cliente_id);
CREATE INDEX idx_contratos_status ON contratos(status);
CREATE INDEX idx_cobrancas_contrato ON cobrancas(contrato_id);
CREATE INDEX idx_cobrancas_data_vencimento ON cobrancas(data_vencimento);
CREATE INDEX idx_cobrancas_status ON cobrancas(status);
