package cl.duoc.bancoxyz.runner;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class JobRunner implements ApplicationRunner {

    private final JobLauncher jobLauncher;
    private final Job transaccionesJob;
    private final Job interesesJob;
    private final Job estadosCuentaJob;

    @Value("${app.job.name:all}")
    private String jobName;

    @Value("${app.batch.run-on-startup:false}")
    private boolean runOnStartup;

    public JobRunner(JobLauncher jobLauncher,
                     @Qualifier("transaccionesJob") Job transaccionesJob,
                     @Qualifier("interesesJob") Job interesesJob,
                     @Qualifier("estadosCuentaJob") Job estadosCuentaJob) {
        this.jobLauncher = jobLauncher;
        this.transaccionesJob = transaccionesJob;
        this.interesesJob = interesesJob;
        this.estadosCuentaJob = estadosCuentaJob;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (!runOnStartup) {
            System.out.println("Banco XYZ BFF iniciado. Jobs batch desactivados al arranque.");
            return;
        }

        System.out.println("\n======================================");
        System.out.println(" BANCO XYZ - EJECUCIÓN SPRING BATCH");
        System.out.println(" Job solicitado: " + jobName);
        System.out.println("======================================\n");

        if ("all".equalsIgnoreCase(jobName)) {
            for (Job job : List.of(transaccionesJob, interesesJob, estadosCuentaJob)) {
                ejecutar(job);
            }
            return;
        }

        switch (jobName.toLowerCase()) {
            case "transacciones" -> ejecutar(transaccionesJob);
            case "intereses" -> ejecutar(interesesJob);
            case "estados" -> ejecutar(estadosCuentaJob);
            default -> throw new IllegalArgumentException(
                "Job no reconocido. Usa: all, transacciones, intereses o estados");
        }
    }

    private void ejecutar(Job job) throws Exception {
        JobParameters parameters = new JobParametersBuilder()
                .addLong("run.id", System.currentTimeMillis())
                .toJobParameters();

        var execution = jobLauncher.run(job, parameters);
        System.out.println("--------------------------------------");
        System.out.println("Job: " + job.getName());
        System.out.println("Estado: " + execution.getStatus());
        System.out.println("Inicio: " + execution.getStartTime());
        System.out.println("Fin: " + execution.getEndTime());
        System.out.println("--------------------------------------\n");
    }
}
