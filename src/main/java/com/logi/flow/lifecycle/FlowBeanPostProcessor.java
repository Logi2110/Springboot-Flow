package com.logi.flow.lifecycle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

/**
 * Bean Lifecycle Layer — BeanPostProcessor
 *
 * Spring calls these two methods for EVERY bean it creates:
 *
 *   postProcessBeforeInitialization()  — called BEFORE @PostConstruct / afterPropertiesSet()
 *   postProcessAfterInitialization()   — called AFTER  @PostConstruct / afterPropertiesSet()
 *
 * This is the hook Spring itself uses to implement:
 *   - @Autowired injection          (AutowiredAnnotationBeanPostProcessor)
 *   - AOP proxy creation            (AnnotationAwareAspectJAutoProxyCreator)
 *   - @Async proxy creation         (AsyncAnnotationBeanPostProcessor)
 *   - @Scheduled method scanning    (ScheduledAnnotationBeanPostProcessor)
 *
 * IMPORTANT: Return the original bean (or a wrapper/proxy), NEVER return null.
 * Returning null would remove the bean from the context entirely.
 *
 * This demo implementation only logs — it never modifies the bean.
 */
@Component
public class FlowBeanPostProcessor implements BeanPostProcessor {

    private static final Logger logger = LoggerFactory.getLogger(FlowBeanPostProcessor.class);

    /**
     * Called BEFORE the bean's init method (@PostConstruct / afterPropertiesSet).
     * At this point, dependency injection has already completed.
     */
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (isFlowBean(beanName)) {
            logger.info("🔬 BEAN POST-PROCESSOR - [3] beforeInit : '{}' ({})",
                    beanName, bean.getClass().getSimpleName());
        }
        return bean; // MUST return the bean (or a proxy) — never null
    }

    /**
     * Called AFTER the bean's init method (@PostConstruct / afterPropertiesSet).
     * This is where Spring creates AOP proxies — the returned object may be a proxy,
     * not the original bean instance.
     */
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (isFlowBean(beanName)) {
            logger.info("🔬 BEAN POST-PROCESSOR - [5] afterInit  : '{}' — ready in context ({})",
                    beanName, bean.getClass().getSimpleName());
        }
        return bean; // MUST return the bean (or a proxy) — never null
    }

    // Limit log noise — only log beans from this project's packages
    private boolean isFlowBean(String beanName) {
        return beanName.startsWith("logging")
                || beanName.startsWith("user")
                || beanName.startsWith("startup")
                || beanName.startsWith("beanLifecycle")
                || beanName.startsWith("flow")
                || beanName.startsWith("executionFlow")
                || beanName.startsWith("method")
                || beanName.startsWith("global")
                || beanName.startsWith("request")
                || beanName.startsWith("department");
    }
}
