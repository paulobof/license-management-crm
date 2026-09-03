package com.prediman.crm.service;

import com.prediman.crm.model.Contrato;
import com.prediman.crm.model.enums.Periodicidade;
import com.prediman.crm.model.enums.StatusContrato;
import com.prediman.crm.repository.ContratoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

/**
 * Job mensal de geração automática de cobranças.
 *
 * <p>Executa todo dia 1 às 02:00, percorrendo os contratos MENSAL/ATIVO e gerando a
 * parcela do mês corrente. Cada contrato é processado em sua própria transação
 * (chamada ao {@link ContratoService} via proxy), de modo que a falha em um contrato
 * não aborta o processamento dos demais.</p>
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CobrancaScheduler {

    private final ContratoRepository contratoRepository;
    private final ContratoService contratoService;

    /**
     * Dia 1 de cada mês, às 02:00.
     */
    @Scheduled(cron = "0 0 2 1 * ?")
    public void executarGeracaoMensal() {
        executarGeracaoMensal(LocalDate.now());
    }

    /**
     * Executa a geração mensal para o mês de referência informado.
     * Visível para permitir testes determinísticos.
     */
    public void executarGeracaoMensal(LocalDate referencia) {
        log.info("Iniciando geração automática de cobranças mensais (referência: {})...", referencia);

        List<Contrato> contratos;
        try {
            contratos = contratoRepository.findByPeriodicidadeAndStatus(
                    Periodicidade.MENSAL, StatusContrato.ATIVO);
        } catch (Exception e) {
            log.error("Erro ao carregar contratos para a geração mensal de cobranças: {}", e.getMessage(), e);
            return;
        }

        int geradas = 0;
        int ignorados = 0;
        int erros = 0;

        for (Contrato contrato : contratos) {
            try {
                if (contratoService.gerarCobrancaMensalAutomatica(contrato.getId(), referencia)) {
                    geradas++;
                } else {
                    ignorados++;
                }
            } catch (Exception e) {
                erros++;
                log.error("Erro ao gerar cobrança mensal do contrato id={}: {}",
                        contrato.getId(), e.getMessage(), e);
            }
        }

        log.info("Geração automática de cobranças mensais concluída: {} gerada(s), {} ignorado(s), {} erro(s)",
                geradas, ignorados, erros);
    }
}
