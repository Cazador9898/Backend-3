package cl.duoc.bancoxyz.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Parámetros simples para probar distintas configuraciones de rendimiento
 * sin modificar el código fuente.
 */
@Component
@ConfigurationProperties(prefix = "app.batch")
public class BatchTuningProperties {

    private int chunkSize = 5;
    private int threads = 3;

    public int getChunkSize() {
        return chunkSize;
    }

    public void setChunkSize(int chunkSize) {
        if (chunkSize <= 0) {
            throw new IllegalArgumentException("app.batch.chunk-size debe ser mayor que 0");
        }
        this.chunkSize = chunkSize;
    }

    public int getThreads() {
        return threads;
    }

    public void setThreads(int threads) {
        if (threads <= 0) {
            throw new IllegalArgumentException("app.batch.threads debe ser mayor que 0");
        }
        this.threads = threads;
    }
}
