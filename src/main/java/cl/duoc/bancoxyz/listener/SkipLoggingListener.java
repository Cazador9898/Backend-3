package cl.duoc.bancoxyz.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.SkipListener;
import org.springframework.stereotype.Component;

@Component
public class SkipLoggingListener implements SkipListener<Object, Object> {
    private static final Logger log = LoggerFactory.getLogger(SkipLoggingListener.class);

    @Override
    public void onSkipInRead(Throwable t) {
        log.warn("Registro omitido durante lectura: {}", t.getMessage());
    }

    @Override
    public void onSkipInProcess(Object item, Throwable t) {
        log.warn("Registro omitido durante procesamiento: item={} error={}", item, t.getMessage());
    }

    @Override
    public void onSkipInWrite(Object item, Throwable t) {
        log.warn("Registro omitido durante escritura: item={} error={}", item, t.getMessage());
    }
}
