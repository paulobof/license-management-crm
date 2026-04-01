package com.prediman.crm.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

/**
 * Configura um ThreadPoolTaskScheduler com pool de 4 threads
 * para evitar que tarefas agendadas bloqueiem umas às outras.
 */
@Configuration
public class SchedulerConfig {

    @Bean
    public TaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(4);
        scheduler.setThreadNamePrefix("alerta-scheduler-");
        scheduler.setErrorHandler(t ->
                org.slf4j.LoggerFactory.getLogger(SchedulerConfig.class)
                        .error("Erro não tratado no scheduler: {}", t.getMessage(), t));
        scheduler.initialize();
        return scheduler;
    }
}
