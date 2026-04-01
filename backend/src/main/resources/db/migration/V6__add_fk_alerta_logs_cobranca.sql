-- Adiciona foreign key em alerta_logs.cobranca_id referenciando cobrancas.id
ALTER TABLE alerta_logs
    ADD CONSTRAINT fk_alerta_logs_cobranca
    FOREIGN KEY (cobranca_id) REFERENCES cobrancas(id)
    ON DELETE SET NULL;
