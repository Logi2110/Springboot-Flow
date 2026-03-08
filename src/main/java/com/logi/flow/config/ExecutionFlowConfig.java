package com.logi.flow.config;

import com.logi.flow.filter.LoggingFilter;
import com.logi.flow.interceptor.LoggingInterceptor;
import com.logi.flow.resolver.RequestInfoArgumentResolver;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;
import java.util.ListIterator;

@Configuration
@EnableAspectJAutoProxy
@EnableAsync
@EnableScheduling
public class ExecutionFlowConfig implements WebMvcConfigurer {

    @Bean
    public FilterRegistrationBean<LoggingFilter> loggingFilter() {
        FilterRegistrationBean<LoggingFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new LoggingFilter());
        registrationBean.addUrlPatterns("/api/*");
        registrationBean.setOrder(1);
        registrationBean.setName("LoggingFilter");
        return registrationBean;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new LoggingInterceptor())
                .addPathPatterns("/api/**")
                .order(1);
    }

    /**
     * ARGUMENT RESOLVER — Layer 2 registration.
     * Registers RequestInfoArgumentResolver so Spring MVC knows to call it
     * when a controller method parameter is annotated with @InjectRequestInfo.
     */
    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new RequestInfoArgumentResolver());
    }

    /**
     * MESSAGE CONVERTER — Layer 2 registration.
     * Replaces the default MappingJackson2HttpMessageConverter with our
     * LoggingMessageConverter (which extends it) to add read/write logging.
     * Uses extendMessageConverters (not configureMessageConverters) so all
     * other Spring Boot auto-configured converters remain intact.
     */
    @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        LoggingMessageConverter loggingConverter = new LoggingMessageConverter();
        // Replace the first Jackson converter found with our logging subclass
        ListIterator<HttpMessageConverter<?>> it = converters.listIterator();
        while (it.hasNext()) {
            HttpMessageConverter<?> converter = it.next();
            if (converter instanceof org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
                    && !(converter instanceof LoggingMessageConverter)) {
                it.set(loggingConverter);
                break;
            }
        }
    }
}