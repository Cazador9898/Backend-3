package cl.duoc.bancoxyz.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.stereotype.Component;

@Component
public class StepMetricsListener implements StepExecutionListener {
    private static final Logger log = LoggerFactory.getLogger(StepMetricsListener.class);
    private final ThreadLocal<Long> inicio = new ThreadLocal<>();

    @Override
    public void beforeStep(StepExecution stepExecution) {
        inicio.set(System.currentTimeMillis());
        log.info("Iniciando Step {}", stepExecution.getStepName());
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        long duracion = System.currentTimeMillis() - inicio.get();
        inicio.remove();
        log.info("Step {} finalizado en {} ms | read={} write={} filter={} skip={} commits={} rollbacks={}",
                stepExecution.getStepName(),
                duracion,
                stepExecution.getReadCount(),
                stepExecution.getWriteCount(),
                stepExecution.getFilterCount(),
                stepExecution.getSkipCount(),
                stepExecution.getCommitCount(),
                stepExecution.getRollbackCount());
        return stepExecution.getExitStatus();
    }
}
