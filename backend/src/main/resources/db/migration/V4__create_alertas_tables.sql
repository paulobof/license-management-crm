CREATE TABLE configuracao_alerta (
    id BIGSERIAL PRIMARY KEY,
    dias_antecedencia VARCHAR(50) NOT NULL DEFAULT '30,15,7,1',
    horario_execucao TIME NOT NULL DEFAULT '08:00:00',
    email_ativo BOOLEAN NOT NULL DEFAULT TRUE,
    whatsapp_ativo BOOLEAN NOT NULL DEFAULT FALSE,
    template_email TEXT DEFAULT 'Prezado(a), o documento {{documento}} do cliente {{cliente}} vence em {{dias}} dia(s). Por favor, tome as providências necessárias.',
    template_whatsapp TEXT DEFAULT 'Alerta: O documento {{documento}} do cliente {{cliente}} vence em {{dias}} dia(s).',
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE alerta_logs (
    id BIGSERIAL PRIMARY KEY,
    documento_id BIGINT REFERENCES documentos(id) ON DELETE SET NULL,
    cobranca_id BIGINT,
    tipo VARCHAR(20) NOT NULL,
    canal VARCHAR(20) NOT NULL,
    destinatario VARCHAR(255),
    mensagem TEXT,
    status_envio VARCHAR(20) NOT NULL DEFAULT 'PENDENTE',
    data_envio TIMESTAMP,
    snoozed_ate DATE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_alerta_logs_documento ON alerta_logs(documento_id);
CREATE INDEX idx_alerta_logs_cobranca ON alerta_logs(cobranca_id);
CREATE INDEX idx_alerta_logs_status ON alerta_logs(status_envio);

-- Insert default config
INSERT INTO configuracao_alerta (dias_antecedencia, horario_execucao) VALUES ('30,15,7,1', '08:00:00');
