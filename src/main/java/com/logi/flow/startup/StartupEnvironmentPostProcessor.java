package com.logi.flow.startup;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.time.Instant;
import java.util.Map;

/**
 * Startup Layer — EnvironmentPostProcessor
 *
 * Runs BEFORE the ApplicationContext is created / refreshed — much earlier than
 * ApplicationRunner or CommandLineRunner. At this point:
 *   - No Spring beans exist yet (this class is NOT a @Component)
 *   - The logging system may not be fully initialised → use System.out
 *   - Registered via META-INF/spring.factories (not component scan)
 *
 * Use case: inject or override environment properties before any bean sees them.
 * Here we inject app.startup.timestamp so every bean can @Value-inject it.
 */
public class StartupEnvironmentPostProcessor implements EnvironmentPostProcessor {

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {

        String timestamp = Instant.now().toString();

        // addLast → lowest priority (does not override application.yaml values)
        environment.getPropertySources().addLast(
                new MapPropertySource("startupProperties", Map.of(
                        "app.startup.timestamp", timestamp
                ))
        );

        // Regular loggers are not ready this early — System.out is intentional here
        System.out.println("🌱 STARTUP LAYER - EnvironmentPostProcessor.postProcessEnvironment() — BEFORE context refresh");
        System.out.println("🌱 STARTUP LAYER - Injected property: app.startup.timestamp=" + timestamp);
        System.out.println("🌱 STARTUP LAYER - Active profiles: " + String.join(", ",
                environment.getActiveProfiles().length > 0
                        ? environment.getActiveProfiles()
                        : new String[]{"(default)"}));
    }
}
