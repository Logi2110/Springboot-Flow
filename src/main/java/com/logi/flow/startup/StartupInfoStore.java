package com.logi.flow.startup;

import com.logi.flow.dto.StartupInfoResponse;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Startup Layer — StartupInfoStore
 *
 * Central store for events captured during the application startup lifecycle.
 * Events are added by every lifecycle component as they execute:
 *
 *   [static]  StartupEnvironmentPostProcessor  → recordEvent() before context exists
 *   [static]  FlowBeanFactoryPostProcessor     → recordEvent() before beans instantiated
 *   [static]  FlowBeanPostProcessor            → recordEvent() once per BPP phase
 *   [instance] this.@PostConstruct             → addEvent() after DI is complete
 *   [instance] BeanLifecycleDemoBean           → addEvent() via injected store
 *   [instance] StartupApplicationRunner        → addEvent() via injected store
 *   [instance] ExecutionFlowDemoRunner         → addEvent() via injected store
 *
 * Exposed as JSON via GET /api/users/startup-info (Flow 5).
 *
 * Why static + instance?
 *   recordEvent() is static so it can be called from code that runs BEFORE this
 *   bean is instantiated (EnvironmentPostProcessor, BeanFactoryPostProcessor).
 *   addEvent() is an instance method for code that runs AFTER Spring DI.
 *   Both write to the SAME CopyOnWriteArrayList.
 */
@Component
public class StartupInfoStore {

    private static final Logger logger = LoggerFactory.getLogger(StartupInfoStore.class);

    // Static — populated before this bean exists (EnvironmentPostProcessor, BFP, BPP)
    private static final List<String> EVENTS = new CopyOnWriteArrayList<>();

    private final ApplicationContext applicationContext;
    private final Environment environment;

    // Populated in @PostConstruct
    private int totalBeanCount;
    private int flowBeanCount;
    private final List<String> flowBeanNames = new ArrayList<>();

    public StartupInfoStore(ApplicationContext applicationContext, Environment environment) {
        this.applicationContext = applicationContext;
        this.environment = environment;
    }

    /**
     * Static — safe to call from EnvironmentPostProcessor / BeanFactoryPostProcessor
     * that run before any Spring bean exists.
     */
    public static void recordEvent(String event) {
        EVENTS.add(event);
    }

    /**
     * Instance — called by beans that are initialized after this store.
     */
    public void addEvent(String event) {
        EVENTS.add(event);
    }

    @PostConstruct
    public void collect() {
        totalBeanCount = applicationContext.getBeanDefinitionCount();

        if (applicationContext instanceof ConfigurableApplicationContext cac) {
            ConfigurableListableBeanFactory beanFactory = cac.getBeanFactory();
            Arrays.stream(applicationContext.getBeanDefinitionNames())
                    .filter(name -> {
                        try {
                            BeanDefinition bd = beanFactory.getBeanDefinition(name);
                            String className = bd.getBeanClassName();
                            return className != null && className.startsWith("com.logi.flow");
                        } catch (Exception e) {
                            return false;
                        }
                    })
                    .forEach(flowBeanNames::add);
        }

        flowBeanCount = flowBeanNames.size();
        EVENTS.add("📦 StartupInfoStore.@PostConstruct — collected " + flowBeanCount
                + " flow beans / " + totalBeanCount + " total bean definitions");

        logger.info("📦 STARTUP INFO STORE - @PostConstruct: {} flow beans / {} total", flowBeanCount, totalBeanCount);
    }

    public StartupInfoResponse toResponse() {
        List<String> profiles = Arrays.asList(
                environment.getActiveProfiles().length > 0
                        ? environment.getActiveProfiles()
                        : new String[]{"default"});

        return new StartupInfoResponse(
                environment.getProperty("spring.application.name", "flow"),
                environment.getProperty("app.startup.timestamp", "N/A"),
                profiles,
                totalBeanCount,
                flowBeanCount,
                Collections.unmodifiableList(flowBeanNames),
                Collections.unmodifiableList(EVENTS)
        );
    }
}
