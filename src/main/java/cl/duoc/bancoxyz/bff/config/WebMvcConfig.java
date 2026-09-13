package cl.duoc.bancoxyz.bff.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final ChannelAuthInterceptor channelAuthInterceptor;

    public WebMvcConfig(ChannelAuthInterceptor channelAuthInterceptor) {
        this.channelAuthInterceptor = channelAuthInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(channelAuthInterceptor)
                .addPathPatterns("/api/bff/**");
    }
}
