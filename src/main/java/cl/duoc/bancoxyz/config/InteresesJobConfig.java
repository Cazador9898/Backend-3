package cl.duoc.bancoxyz.config;

import cl.duoc.bancoxyz.listener.JobLoggingListener;
import cl.duoc.bancoxyz.listener.SkipLoggingListener;
import cl.duoc.bancoxyz.listener.StepMetricsListener;
import cl.duoc.bancoxyz.model.InteresCuenta;
import cl.duoc.bancoxyz.processor.InteresProcessor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.skip.SkipPolicy;
import org.springframework.batch.core.configuration.annotation.StepScope;
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
public class InteresesJobConfig {

    @Bean
    public SynchronizedItemStreamReader<InteresCuenta> interesReader() {
        FlatFileItemReader<InteresCuenta> delegate = new FlatFileItemReaderBuilder<InteresCuenta>()
                .name("interesReaderDelegate")
                .resource(new ClassPathResource("data/intereses.csv"))
                .linesToSkip(1)
                .delimited()
                .names("cuentaId", "nombre", "saldo", "edad", "tipo")
                .targetType(InteresCuenta.class)
                .build();

        return new SynchronizedItemStreamReaderBuilder<InteresCuenta>()
                .delegate(delegate)
                .build();
    }

    @Bean
    @StepScope
    public InteresProcessor interesProcessor() {
        return new InteresProcessor();
    }

    @Bean
    public JdbcBatchItemWriter<InteresCuenta> interesWriter(DataSource dataSource) {
        return new JdbcBatchItemWriterBuilder<InteresCuenta>()
                .dataSource(dataSource)
                .sql("INSERT INTO intereses_procesados " +
                     "(cuenta_id, nombre, saldo_inicial, edad, tipo, tasa, interes_calculado, saldo_final) " +
                     "VALUES (:cuentaId, :nombre, :saldo, :edad, :tipo, :tasa, :interesCalculado, :saldoFinal) " +
                     "ON DUPLICATE KEY UPDATE nombre=VALUES(nombre), saldo_inicial=VALUES(saldo_inicial), " +
                     "edad=VALUES(edad), tipo=VALUES(tipo), tasa=VALUES(tasa), " +
                     "interes_calculado=VALUES(interes_calculado), saldo_final=VALUES(saldo_final)")
                .itemSqlParameterSourceProvider(BeanPropertySqlParameterSource::new)
                .build();
    }

    @Bean
    public Step limpiarInteresesStep(JobRepository jobRepository,
                                     PlatformTransactionManager transactionManager,
                                     JdbcTemplate jdbcTemplate,
                                     StepMetricsListener stepMetricsListener) {
        return new StepBuilder("limpiarInteresesStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    jdbcTemplate.update("DELETE FROM intereses_procesados");
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .listener(stepMetricsListener)
                .build();
    }

    @Bean
    public Step interesesStep(JobRepository jobRepository,
                              PlatformTransactionManager transactionManager,
                              SynchronizedItemStreamReader<InteresCuenta> interesReader,
                              InteresProcessor interesProcessor,
                              JdbcBatchItemWriter<InteresCuenta> interesWriter,
                              SkipPolicy bancoSkipPolicy,
                              RetryPolicy bancoRetryPolicy,
                              TaskExecutor batchTaskExecutor,
                              StepMetricsListener stepMetricsListener,
                              SkipLoggingListener skipLoggingListener,
                              BatchTuningProperties tuning) {
        return new StepBuilder("interesesStep", jobRepository)
                .<InteresCuenta, InteresCuenta>chunk(tuning.getChunkSize(), transactionManager)
                .reader(interesReader)
                .processor(interesProcessor)
                .writer(interesWriter)
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
    public Job interesesJob(JobRepository jobRepository,
                            Step limpiarInteresesStep,
                            Step interesesStep,
                            JobLoggingListener jobLoggingListener) {
        return new JobBuilder("interesesJob", jobRepository)
                .listener(jobLoggingListener)
                .start(limpiarInteresesStep)
                .next(interesesStep)
                .build();
    }
}
