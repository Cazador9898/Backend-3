package cl.duoc.bancoxyz.config;

import cl.duoc.bancoxyz.listener.JobLoggingListener;
import cl.duoc.bancoxyz.listener.SkipLoggingListener;
import cl.duoc.bancoxyz.listener.StepMetricsListener;
import cl.duoc.bancoxyz.model.MovimientoAnual;
import cl.duoc.bancoxyz.processor.MovimientoAnualProcessor;
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
public class EstadosCuentaJobConfig {

    @Bean
    public SynchronizedItemStreamReader<MovimientoAnual> movimientoAnualReader() {
        FlatFileItemReader<MovimientoAnual> delegate = new FlatFileItemReaderBuilder<MovimientoAnual>()
                .name("movimientoAnualReaderDelegate")
                .resource(new ClassPathResource("data/cuentas_anuales.csv"))
                .linesToSkip(1)
                .delimited()
                .names("cuentaId", "fecha", "transaccion", "monto", "descripcion")
                .targetType(MovimientoAnual.class)
                .build();

        return new SynchronizedItemStreamReaderBuilder<MovimientoAnual>()
                .delegate(delegate)
                .build();
    }

    @Bean
    public MovimientoAnualProcessor movimientoAnualProcessor() {
        return new MovimientoAnualProcessor();
    }

    @Bean
    public JdbcBatchItemWriter<MovimientoAnual> movimientoAnualWriter(DataSource dataSource) {
        return new JdbcBatchItemWriterBuilder<MovimientoAnual>()
                .dataSource(dataSource)
                .sql("INSERT INTO movimientos_anuales " +
                     "(cuenta_id, fecha, transaccion, monto, descripcion) " +
                     "VALUES (:cuentaId, :fechaNormalizada, :transaccion, :monto, :descripcion)")
                .itemSqlParameterSourceProvider(BeanPropertySqlParameterSource::new)
                .build();
    }

    @Bean
    public Step limpiarMovimientosStep(JobRepository jobRepository,
                                       PlatformTransactionManager transactionManager,
                                       JdbcTemplate jdbcTemplate,
                                       StepMetricsListener stepMetricsListener) {
        return new StepBuilder("limpiarMovimientosStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    jdbcTemplate.update("DELETE FROM movimientos_anuales");
                    jdbcTemplate.update("DELETE FROM estados_cuenta_anuales");
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .listener(stepMetricsListener)
                .build();
    }

    @Bean
    public Step cargarMovimientosStep(JobRepository jobRepository,
                                      PlatformTransactionManager transactionManager,
                                      SynchronizedItemStreamReader<MovimientoAnual> movimientoAnualReader,
                                      MovimientoAnualProcessor movimientoAnualProcessor,
                                      JdbcBatchItemWriter<MovimientoAnual> movimientoAnualWriter,
                                      SkipPolicy bancoSkipPolicy,
                                      RetryPolicy bancoRetryPolicy,
                                      TaskExecutor batchTaskExecutor,
                                      StepMetricsListener stepMetricsListener,
                                      SkipLoggingListener skipLoggingListener,
                                      BatchTuningProperties tuning) {
        return new StepBuilder("cargarMovimientosStep", jobRepository)
                .<MovimientoAnual, MovimientoAnual>chunk(tuning.getChunkSize(), transactionManager)
                .reader(movimientoAnualReader)
                .processor(movimientoAnualProcessor)
                .writer(movimientoAnualWriter)
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
    public Step generarResumenAnualStep(JobRepository jobRepository,
                                        PlatformTransactionManager transactionManager,
                                        JdbcTemplate jdbcTemplate,
                                        StepMetricsListener stepMetricsListener) {
        return new StepBuilder("generarResumenAnualStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    jdbcTemplate.update(
                        "INSERT INTO estados_cuenta_anuales " +
                        "(cuenta_id, anio, total_depositos, total_retiros_compras, saldo_neto, cantidad_movimientos) " +
                        "SELECT cuenta_id, YEAR(fecha), " +
                        "SUM(CASE WHEN monto > 0 THEN monto ELSE 0 END), " +
                        "SUM(CASE WHEN monto < 0 THEN ABS(monto) ELSE 0 END), " +
                        "SUM(monto), COUNT(*) " +
                        "FROM movimientos_anuales GROUP BY cuenta_id, YEAR(fecha)"
                    );
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .listener(stepMetricsListener)
                .build();
    }

    @Bean
    public Job estadosCuentaJob(JobRepository jobRepository,
                                Step limpiarMovimientosStep,
                                Step cargarMovimientosStep,
                                Step generarResumenAnualStep,
                                JobLoggingListener jobLoggingListener) {
        return new JobBuilder("estadosCuentaJob", jobRepository)
                .listener(jobLoggingListener)
                .start(limpiarMovimientosStep)
                .next(cargarMovimientosStep)
                .next(generarResumenAnualStep)
                .build();
    }
}
