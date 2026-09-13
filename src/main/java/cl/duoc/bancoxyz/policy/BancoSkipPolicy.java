package cl.duoc.bancoxyz.policy;

import org.springframework.batch.core.step.skip.SkipPolicy;
import org.springframework.batch.item.file.FlatFileParseException;
import org.springframework.dao.DataIntegrityViolationException;

public class BancoSkipPolicy implements SkipPolicy {
    private static final long MAX_SKIPS = 20;

    @Override
    public boolean shouldSkip(Throwable throwable, long skipCount) {
        if (skipCount >= MAX_SKIPS) {
            return false;
        }

        return hasCause(throwable, FlatFileParseException.class)
                || hasCause(throwable, IllegalArgumentException.class)
                || hasCause(throwable, DataIntegrityViolationException.class);
    }

    private boolean hasCause(Throwable throwable, Class<? extends Throwable> type) {
        Throwable current = throwable;
        while (current != null) {
            if (type.isInstance(current)) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }
}
