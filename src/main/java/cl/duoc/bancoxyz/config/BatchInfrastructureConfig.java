package cl.duoc.bancoxyz.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class BatchInfrastructureConfig {

    @Bean(name = "batchTaskExecutor")
    public TaskExecutor batchTaskExecutor(BatchTuningProperties tuning) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setThreadNamePrefix("banco-batch-");
        executor.setCorePoolSize(tuning.getThreads());
        executor.setMaxPoolSize(tuning.getThreads());
        executor.setQueueCapacity(100);
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.initialize();
        return executor;
    }
}
