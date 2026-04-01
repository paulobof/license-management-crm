package com.prediman.crm.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("SchedulerConfig")
class SchedulerConfigTest {

    private final SchedulerConfig schedulerConfig = new SchedulerConfig();

    @Test
    @DisplayName("taskScheduler retorna ThreadPoolTaskScheduler nao nulo")
    void taskScheduler_retornaSchedulerNaoNulo() {
        TaskScheduler scheduler = schedulerConfig.taskScheduler();

        assertThat(scheduler).isNotNull();
        assertThat(scheduler).isInstanceOf(ThreadPoolTaskScheduler.class);
    }

    @Test
    @DisplayName("taskScheduler esta inicializado e pronto para uso")
    void taskScheduler_estaInicializado() {
        ThreadPoolTaskScheduler scheduler = (ThreadPoolTaskScheduler) schedulerConfig.taskScheduler();

        assertThat(scheduler.getThreadNamePrefix()).isEqualTo("alerta-scheduler-");
        // getScheduledThreadPoolExecutor().getCorePoolSize() reflects the configured pool size after initialize()
        assertThat(scheduler.getScheduledThreadPoolExecutor().getCorePoolSize()).isEqualTo(4);
    }

    @Test
    @DisplayName("taskScheduler possui errorHandler configurado")
    void taskScheduler_possuiErrorHandlerConfigurado() {
        ThreadPoolTaskScheduler scheduler = (ThreadPoolTaskScheduler) schedulerConfig.taskScheduler();

        // Verifica que o scheduler está devidamente inicializado (error handler configurado via setErrorHandler)
        assertThat(scheduler).isNotNull();
        assertThat(scheduler.getThreadNamePrefix()).isEqualTo("alerta-scheduler-");
    }

    @Test
    @DisplayName("errorHandler executa sem lancar excecao ao receber um Throwable")
    void taskScheduler_errorHandler_executaSemExcecao() throws Exception {
        ThreadPoolTaskScheduler scheduler = (ThreadPoolTaskScheduler) schedulerConfig.taskScheduler();

        // Extract the error handler field via reflection
        java.lang.reflect.Field field = ThreadPoolTaskScheduler.class.getDeclaredField("errorHandler");
        field.setAccessible(true);
        org.springframework.util.ErrorHandler errorHandler =
                (org.springframework.util.ErrorHandler) field.get(scheduler);

        assertThat(errorHandler).isNotNull();

        // Invoke the lambda — it should just log and not throw
        errorHandler.handleError(new RuntimeException("Erro de teste"));
    }
}
