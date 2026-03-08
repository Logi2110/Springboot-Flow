package com.logi.flow.lifecycle;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Bean Lifecycle Layer — @PostConstruct / @PreDestroy
 *
 * Lifecycle order for a Spring-managed bean:
 *
 *   1. Constructor                   — Spring instantiates the bean
 *   2. Dependency injection          — @Autowired / constructor args injected
 *   3. BeanPostProcessor.before      — FlowBeanPostProcessor wraps it (see that class)
 *   4. @PostConstruct                — ← THIS METHOD — safe to use injected fields here
 *   5. BeanPostProcessor.after       — FlowBeanPostProcessor wraps it (after init)
 *   6. Bean is ready and in context  — serves requests
 *      ...
 *   7. @PreDestroy                   — ← THIS METHOD — called on context shutdown
 *   8. Bean is GC-eligible
 *
 * Compare with InitializingBean.afterPropertiesSet() and DisposableBean.destroy()
 * which do the same thing but couple your class to Spring APIs. @PostConstruct /
 * @PreDestroy are the preferred, JSR-250 (Jakarta) standard approach.
 */
@Component
public class BeanLifecycleDemoBean {

    private static final Logger logger = LoggerFactory.getLogger(BeanLifecycleDemoBean.class);

    @Value("${spring.application.name:flow}")
    private String applicationName;

    @Value("${app.startup.timestamp:N/A}")
    private String startupTimestamp;

    // Step 1 — constructor runs first. Injected fields are NOT yet available here.
    public BeanLifecycleDemoBean() {
        logger.info("🌱 BEAN LIFECYCLE - [1] Constructor: BeanLifecycleDemoBean() — fields not injected yet");
    }

    // Step 4 — all injected fields are populated; safe to do initialization work.
    @PostConstruct
    public void init() {
        logger.info("🌱 BEAN LIFECYCLE - [4] @PostConstruct: init() called");
        logger.info("🌱 BEAN LIFECYCLE -     Application : {}", applicationName);
        logger.info("🌱 BEAN LIFECYCLE -     Started at  : {}", startupTimestamp);
        logger.info("🌱 BEAN LIFECYCLE -     Use @PostConstruct to: open connections, warm caches, validate config");
    }

    // Step 7 — called when the application context is shutting down.
    @PreDestroy
    public void cleanup() {
        logger.info("🌱 BEAN LIFECYCLE - [7] @PreDestroy: cleanup() called");
        logger.info("🌱 BEAN LIFECYCLE -     Use @PreDestroy to: close connections, flush buffers, release resources");
    }
}
