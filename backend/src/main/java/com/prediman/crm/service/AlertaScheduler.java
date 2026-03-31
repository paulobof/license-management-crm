package com.prediman.crm.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AlertaScheduler {

    private final AlertaService alertaService;

    /**
     * Executa diariamente às 08:00 verificando documentos próximos do vencimento
     * e criando entradas AlertaLog com status PENDENTE.
     * O envio efetivo por e-mail/WhatsApp será ativado quando as credenciais forem configuradas.
     */
    @Scheduled(cron = "0 0 8 * * ?")
    public void executarAlertasDiarios() {
        log.info("Iniciando processamento diário de alertas de vencimento...");
        try {
            alertaService.processarAlertasDiarios();
        } catch (Exception e) {
            log.error("Erro ao processar alertas diários: {}", e.getMessage(), e);
        }
    }
}
