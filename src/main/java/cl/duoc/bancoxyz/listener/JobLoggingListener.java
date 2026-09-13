package cl.duoc.bancoxyz.listener;

import cl.duoc.bancoxyz.config.BatchTuningProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.stereotype.Component;

@Component
public class JobLoggingListener implements JobExecutionListener {
    private static final Logger log = LoggerFactory.getLogger(JobLoggingListener.class);
    private final BatchTuningProperties tuning;

    public JobLoggingListener(BatchTuningProperties tuning) {
        this.tuning = tuning;
    }

    @Override
    public void beforeJob(JobExecution jobExecution) {
        log.info("Iniciando Job {} con parámetros {} | chunkSize={} | threads={}",
                jobExecution.getJobInstance().getJobName(),
                jobExecution.getJobParameters(),
                tuning.getChunkSize(),
                tuning.getThreads());
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        log.info("Finalizó Job {} - estado={} - inicio={} - fin={}",
                jobExecution.getJobInstance().getJobName(),
                jobExecution.getStatus(),
                jobExecution.getStartTime(),
                jobExecution.getEndTime());
    }
}
