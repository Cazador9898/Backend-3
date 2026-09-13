package cl.duoc.bancoxyz.policy;

import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.retry.RetryContext;
import org.springframework.retry.RetryPolicy;
import org.springframework.retry.context.RetryContextSupport;

public class BancoRetryPolicy implements RetryPolicy {
    private static final int MAX_ATTEMPTS = 3;

    @Override
    public boolean canRetry(RetryContext context) {
        if (context.getLastThrowable() == null) {
            return true;
        }
        return context.getRetryCount() < MAX_ATTEMPTS
                && isRetryable(context.getLastThrowable());
    }

    @Override
    public RetryContext open(RetryContext parent) {
        return new RetryContextSupport(parent);
    }

    @Override
    public void close(RetryContext context) {
        // No se requieren recursos adicionales al cerrar el reintento.
    }

    @Override
    public void registerThrowable(RetryContext context, Throwable throwable) {
        ((RetryContextSupport) context).registerThrowable(throwable);
    }

    private boolean isRetryable(Throwable throwable) {
        Throwable current = throwable;
        while (current != null) {
            if (current instanceof TransientDataAccessException
                    || current instanceof ConcurrencyFailureException) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }
}
