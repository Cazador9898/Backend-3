package cl.duoc.bancoxyz.config;

import cl.duoc.bancoxyz.policy.BancoRetryPolicy;
import cl.duoc.bancoxyz.policy.BancoSkipPolicy;
import org.springframework.batch.core.step.skip.SkipPolicy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.RetryPolicy;

@Configuration
public class BatchPolicyConfig {

    @Bean
    public SkipPolicy bancoSkipPolicy() {
        return new BancoSkipPolicy();
    }

    @Bean
    public RetryPolicy bancoRetryPolicy() {
        return new BancoRetryPolicy();
    }
}
