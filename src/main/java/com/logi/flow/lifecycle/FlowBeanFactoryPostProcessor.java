package com.logi.flow.lifecycle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * Bean Lifecycle Layer — BeanFactoryPostProcessor
 *
 * Runs BEFORE any bean instance is created — operates on BeanDefinitions,
 * not on live bean instances.
 *
 * Lifecycle position (relative to BeanPostProcessor):
 *
 *   [Context starts]
 *        ↓
 *   BeanFactoryPostProcessor.postProcessBeanFactory()   ← THIS CLASS — modifies definitions
 *        ↓
 *   Spring instantiates beans (constructor)
 *        ↓
 *   BeanPostProcessor.postProcessBeforeInitialization() ← FlowBeanPostProcessor
 *        ↓
 *   @PostConstruct                                       ← BeanLifecycleDemoBean
 *        ↓
 *   BeanPostProcessor.postProcessAfterInitialization()  ← FlowBeanPostProcessor
 *        ↓
 *   [Bean ready in context]
 *
 * Real-world use cases:
 *   - Override property placeholders at factory level
 *   - Change bean scope (singleton ↔ prototype) based on environment
 *   - Mark bean definitions as lazy / primary / abstract programmatically
 *   - Register additional bean definitions dynamically
 *
 * This demo inspects all flow-package bean definitions and logs their metadata;
 * it does NOT modify any definitions.
 */
@Component
public class FlowBeanFactoryPostProcessor implements BeanFactoryPostProcessor {

    private static final Logger logger = LoggerFactory.getLogger(FlowBeanFactoryPostProcessor.class);

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        logger.info("🏭 BEAN FACTORY POST-PROCESSOR - postProcessBeanFactory() called");
        logger.info("🏭 BEAN FACTORY POST-PROCESSOR - Total bean definitions: {}", beanFactory.getBeanDefinitionCount());

        // Inspect definitions for beans in our own package
        String[] allNames = beanFactory.getBeanDefinitionNames();
        long flowBeanCount = Arrays.stream(allNames)
                .filter(name -> {
                    BeanDefinition bd = beanFactory.getBeanDefinition(name);
                    String className = bd.getBeanClassName();
                    return className != null && className.startsWith("com.logi.flow");
                })
                .peek(name -> {
                    BeanDefinition bd = beanFactory.getBeanDefinition(name);
                    logger.info("🏭 BEAN FACTORY POST-PROCESSOR -   '{}' | scope={} | lazy={} | class={}",
                            name,
                            bd.getScope().isEmpty() ? "singleton" : bd.getScope(),
                            bd.isLazyInit(),
                            simpleName(bd.getBeanClassName()));
                })
                .count();

        logger.info("🏭 BEAN FACTORY POST-PROCESSOR - Flow-package bean definitions found: {}", flowBeanCount);
        logger.info("🏭 BEAN FACTORY POST-PROCESSOR - NOTE: at this point NO beans have been instantiated yet");
    }

    private String simpleName(String fullClassName) {
        if (fullClassName == null) return "?";
        int dot = fullClassName.lastIndexOf('.');
        return dot >= 0 ? fullClassName.substring(dot + 1) : fullClassName;
    }
}
