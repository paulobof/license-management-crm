package com.prediman.crm.service;

import com.prediman.crm.model.ConfiguracaoAlerta;
import com.prediman.crm.repository.ConfiguracaoAlertaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.scheduling.config.TriggerTask;
import org.springframework.test.util.ReflectionTestUtils;

import org.springframework.scheduling.TriggerContext;
import org.springframework.scheduling.support.SimpleTriggerContext;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AlertaScheduler")
class AlertaSchedulerTest {

    @Mock
    private AlertaService alertaService;

    @Mock
    private ConfiguracaoAlertaRepository configuracaoAlertaRepository;

    @Mock
    private TaskScheduler taskScheduler;

    private AlertaScheduler alertaScheduler;

    @BeforeEach
    void setUp() {
        alertaScheduler = new AlertaScheduler(alertaService, configuracaoAlertaRepository, taskScheduler);
    }

    @Test
    @DisplayName("configureTasks registra o taskScheduler e adiciona uma TriggerTask")
    void configureTasks_registraSchedulerEAdicionaTriggerTask() {
        ScheduledTaskRegistrar registrar = mock(ScheduledTaskRegistrar.class);

        alertaScheduler.configureTasks(registrar);

        verify(registrar).setScheduler(taskScheduler);
        verify(registrar).addTriggerTask(any(Runnable.class), any());
    }

    @Test
    @DisplayName("executarAlertasDiarios delega para AlertaService.processarAlertasDiarios")
    void executarAlertasDiarios_delegaParaAlertaService() {
        alertaScheduler.executarAlertasDiarios();

        verify(alertaService).processarAlertasDiarios();
    }

    @Test
    @DisplayName("executarAlertasDiarios captura excecao sem propagar")
    void executarAlertasDiarios_capturaExcecao() {
        doThrow(new RuntimeException("Erro simulado")).when(alertaService).processarAlertasDiarios();

        // Nao deve lancar excecao
        alertaScheduler.executarAlertasDiarios();
    }

    @Test
    @DisplayName("buildCronExpression usa horario da base de dados quando disponivel")
    void buildCronExpression_usaHorarioDoBanco() {
        ConfiguracaoAlerta config = ConfiguracaoAlerta.builder()
                .horarioExecucao(LocalTime.of(14, 30))
                .build();
        when(configuracaoAlertaRepository.findFirstByOrderByIdAsc())
                .thenReturn(Optional.of(config));

        String cron = (String) ReflectionTestUtils.invokeMethod(alertaScheduler, "buildCronExpression");

        assertThat(cron).isEqualTo("0 30 14 * * ?");
    }

    @Test
    @DisplayName("buildCronExpression usa fallback 08:00 quando configuracao nao encontrada")
    void buildCronExpression_usaFallbackQuandoSemConfiguracao() {
        when(configuracaoAlertaRepository.findFirstByOrderByIdAsc())
                .thenReturn(Optional.empty());

        String cron = (String) ReflectionTestUtils.invokeMethod(alertaScheduler, "buildCronExpression");

        assertThat(cron).isEqualTo("0 0 8 * * ?");
    }

    @Test
    @DisplayName("buildCronExpression usa fallback quando ocorre excecao no repositorio")
    void buildCronExpression_usaFallbackQuandoExcecao() {
        when(configuracaoAlertaRepository.findFirstByOrderByIdAsc())
                .thenThrow(new RuntimeException("DB indisponivel"));

        String cron = (String) ReflectionTestUtils.invokeMethod(alertaScheduler, "buildCronExpression");

        assertThat(cron).isEqualTo("0 0 8 * * ?");
    }

    @Test
    @DisplayName("buildCronExpression formata corretamente hora zero e minuto zero")
    void buildCronExpression_horaMinutoZero() {
        ConfiguracaoAlerta config = ConfiguracaoAlerta.builder()
                .horarioExecucao(LocalTime.of(0, 0))
                .build();
        when(configuracaoAlertaRepository.findFirstByOrderByIdAsc())
                .thenReturn(Optional.of(config));

        String cron = (String) ReflectionTestUtils.invokeMethod(alertaScheduler, "buildCronExpression");

        assertThat(cron).isEqualTo("0 0 0 * * ?");
    }

    @Test
    @DisplayName("configureTasks — trigger lambda calcula proximo horario com base no banco")
    void configureTasks_triggerLambda_calculaProximoHorario() {
        ConfiguracaoAlerta config = ConfiguracaoAlerta.builder()
                .horarioExecucao(LocalTime.of(9, 0))
                .build();
        when(configuracaoAlertaRepository.findFirstByOrderByIdAsc())
                .thenReturn(Optional.of(config));

        // Capture the trigger task via a real ScheduledTaskRegistrar
        org.springframework.scheduling.config.ScheduledTaskRegistrar registrar =
                new org.springframework.scheduling.config.ScheduledTaskRegistrar();
        registrar.setScheduler(taskScheduler);

        alertaScheduler.configureTasks(registrar);

        // Extract the trigger tasks and invoke the trigger directly
        List<org.springframework.scheduling.config.TriggerTask> triggerTasks = registrar.getTriggerTaskList();
        assertThat(triggerTasks).isNotEmpty();

        org.springframework.scheduling.config.TriggerTask triggerTask = triggerTasks.get(0);

        // Use SimpleTriggerContext with a real Clock so CronTrigger can compute next execution
        SimpleTriggerContext context = new SimpleTriggerContext(Clock.system(ZoneId.systemDefault()));

        // nextExecution should return a non-null instant given a valid cron expression
        Instant next = triggerTask.getTrigger().nextExecution(context);
        assertThat(next).isNotNull();
    }
}
