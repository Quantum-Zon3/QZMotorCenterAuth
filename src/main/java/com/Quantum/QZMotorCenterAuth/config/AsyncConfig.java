package com.Quantum.QZMotorCenterAuth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Configuración de asincronizidad para la aplicación.
 *
 * @EnableAsync - Habilita el procesamiento asíncrono en Spring
 *
 * Se configura un ThreadPoolTaskExecutor personalizado con:
 * - CorePoolSize: Hilos mínimos activos
 * - MaxPoolSize: Hilos máximos permitidos
 * - QueueCapacity: Cola de tareas pendientes
 * - ThreadNamePrefix: Prefijo para identificar hilos async en los logs
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean(name = "asyncExecutor")
    public Executor asyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(8);
        executor.setQueueCapacity(50);
        executor.setThreadNamePrefix("QZAuth-Async-");
        executor.initialize();
        return executor;
    }
}
