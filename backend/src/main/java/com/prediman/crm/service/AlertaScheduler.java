package com.prediman.crm.service;

import com.prediman.crm.model.ConfiguracaoAlerta;
import com.prediman.crm.repository.ConfiguracaoAlertaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;

import java.time.LocalTime;

/**
 * Scheduler dinâmico que lê o horário de execução da tabela configuracao_alerta.
 * A cada disparo, o trigger recalcula o próximo horário com base no valor atual do banco,
 * permitindo que alterações via UI entrem em vigor sem reiniciar a aplicação.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AlertaScheduler implements SchedulingConfigurer {

    private final AlertaService alertaService;
    private final ConfiguracaoAlertaRepository configuracaoAlertaRepository;
    private final TaskScheduler taskScheduler;

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        taskRegistrar.setScheduler(taskScheduler);
        taskRegistrar.addTriggerTask(
                this::executarAlertasDiarios,
                triggerContext -> {
                    String cron = buildCronExpression();
                    CronTrigger cronTrigger = new CronTrigger(cron);
                    return cronTrigger.nextExecution(triggerContext);
                }
        );
    }

    /**
     * Executa diariamente no horário configurado, verificando documentos próximos
     * do vencimento e criando entradas AlertaLog com status PENDENTE.
     */
    public void executarAlertasDiarios() {
        log.info("Iniciando processamento diário de alertas de vencimento...");
        try {
            alertaService.processarAlertasDiarios();
        } catch (Exception e) {
            log.error("Erro ao processar alertas diários: {}", e.getMessage(), e);
        }
    }

    /**
     * Constrói uma expressão cron a partir do horarioExecucao armazenado no banco.
     * Fallback para 08:00 caso a configuração não seja encontrada.
     */
    private String buildCronExpression() {
        try {
            LocalTime horario = configuracaoAlertaRepository.findFirstByOrderByIdAsc()
                    .map(ConfiguracaoAlerta::getHorarioExecucao)
                    .orElse(LocalTime.of(8, 0));

            String cron = String.format("0 %d %d * * ?", horario.getMinute(), horario.getHour());
            log.debug("Cron de alertas dinâmico: {}", cron);
            return cron;
        } catch (Exception e) {
            log.warn("Erro ao ler horarioExecucao do banco; usando 08:00 como fallback: {}", e.getMessage());
            return "0 0 8 * * ?";
        }
    }
}
