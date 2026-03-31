ALTER TABLE clientes ADD COLUMN tipo_pessoa VARCHAR(10) NOT NULL DEFAULT 'JURIDICA';
ALTER TABLE clientes ADD COLUMN cpf VARCHAR(14) UNIQUE;

CREATE INDEX idx_clientes_cpf ON clientes(cpf);
CREATE INDEX idx_clientes_tipo_pessoa ON clientes(tipo_pessoa);
