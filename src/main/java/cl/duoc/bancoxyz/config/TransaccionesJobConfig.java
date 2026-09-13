package cl.duoc.bancoxyz.config;

import cl.duoc.bancoxyz.listener.JobLoggingListener;
import cl.duoc.bancoxyz.listener.SkipLoggingListener;
import cl.duoc.bancoxyz.listener.StepMetricsListener;
import cl.duoc.bancoxyz.model.Transaccion;
import cl.duoc.bancoxyz.processor.TransaccionProcessor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.skip.SkipPolicy;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.support.SynchronizedItemStreamReader;
import org.springframework.batch.item.support.builder.SynchronizedItemStreamReaderBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.task.TaskExecutor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.retry.RetryPolicy;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

@Configuration
public class TransaccionesJobConfig {

    @Bean
    public SynchronizedItemStreamReader<Transaccion> transaccionReader() {
        FlatFileItemReader<Transaccion> delegate = new FlatFileItemReaderBuilder<Transaccion>()
                .name("transaccionReaderDelegate")
                .resource(new ClassPathResource("data/transacciones.csv"))
                .linesToSkip(1)
                .delimited()
                .names("id", "fecha", "monto", "tipo")
                .targetType(Transaccion.class)
                .build();

        return new SynchronizedItemStreamReaderBuilder<Transaccion>()
                .delegate(delegate)
                .build();
    }

    @Bean
    public TransaccionProcessor transaccionProcessor() {
        return new TransaccionProcessor();
    }

    @Bean
    public JdbcBatchItemWriter<Transaccion> transaccionWriter(DataSource dataSource) {
        return new JdbcBatchItemWriterBuilder<Transaccion>()
                .dataSource(dataSource)
                .sql("INSERT INTO transacciones_procesadas " +
                     "(id, fecha, monto, tipo, anomalia, observacion) " +
                     "VALUES (:id, :fechaNormalizada, :monto, :tipo, :anomalia, :observacion) " +
                     "ON DUPLICATE KEY UPDATE fecha=VALUES(fecha), monto=VALUES(monto), " +
                     "tipo=VALUES(tipo), anomalia=VALUES(anomalia), observacion=VALUES(observacion)")
                .itemSqlParameterSourceProvider(BeanPropertySqlParameterSource::new)
                .build();
    }

    @Bean
    public Step limpiarTransaccionesStep(JobRepository jobRepository,
                                          PlatformTransactionManager transactionManager,
                                          JdbcTemplate jdbcTemplate,
                                          StepMetricsListener stepMetricsListener) {
        return new StepBuilder("limpiarTransaccionesStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    jdbcTemplate.update("DELETE FROM resumen_transacciones_diarias");
                    jdbcTemplate.update("DELETE FROM transacciones_procesadas");
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .listener(stepMetricsListener)
                .build();
    }

    @Bean
    public Step transaccionesStep(JobRepository jobRepository,
                                  PlatformTransactionManager transactionManager,
                                  SynchronizedItemStreamReader<Transaccion> transaccionReader,
                                  TransaccionProcessor transaccionProcessor,
                                  JdbcBatchItemWriter<Transaccion> transaccionWriter,
                                  SkipPolicy bancoSkipPolicy,
                                  RetryPolicy bancoRetryPolicy,
                                  TaskExecutor batchTaskExecutor,
                                  StepMetricsListener stepMetricsListener,
                                  SkipLoggingListener skipLoggingListener,
                                  BatchTuningProperties tuning) {
        return new StepBuilder("transaccionesStep", jobRepository)
                .<Transaccion, Transaccion>chunk(tuning.getChunkSize(), transactionManager)
                .reader(transaccionReader)
                .processor(transaccionProcessor)
                .writer(transaccionWriter)
                .faultTolerant()
                .skipPolicy(bancoSkipPolicy)
                .retryPolicy(bancoRetryPolicy)
                .listener(skipLoggingListener)
                .taskExecutor(batchTaskExecutor)
                .startLimit(3)
                .allowStartIfComplete(true)
                .listener(stepMetricsListener)
                .build();
    }

    @Bean
    public Step resumenTransaccionesStep(JobRepository jobRepository,
                                         PlatformTransactionManager transactionManager,
                                         JdbcTemplate jdbcTemplate,
                                         StepMetricsListener stepMetricsListener) {
        return new StepBuilder("resumenTransaccionesStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    jdbcTemplate.update("DELETE FROM resumen_transacciones_diarias");
                    jdbcTemplate.update(
                            "INSERT INTO resumen_transacciones_diarias " +
                            "(fecha, total_transacciones, cantidad_anomalias, monto_total) " +
                            "SELECT fecha, COUNT(*), SUM(CASE WHEN anomalia = TRUE THEN 1 ELSE 0 END), SUM(monto) " +
                            "FROM transacciones_procesadas GROUP BY fecha ORDER BY fecha"
                    );
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .listener(stepMetricsListener)
                .build();
    }

    @Bean
    public Job transaccionesJob(JobRepository jobRepository,
                                Step limpiarTransaccionesStep,
                                Step transaccionesStep,
                                Step resumenTransaccionesStep,
                                JobLoggingListener jobLoggingListener) {
        return new JobBuilder("transaccionesJob", jobRepository)
                .listener(jobLoggingListener)
                .start(limpiarTransaccionesStep)
                .next(transaccionesStep)
                .next(resumenTransaccionesStep)
                .build();
    }
}
