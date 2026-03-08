package com.logi.flow.startup;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

/**
 * Startup Layer — ApplicationRunner
 *
 * Runs AFTER the application context is fully refreshed, just like CommandLineRunner.
 * Key difference from CommandLineRunner:
 *   - ApplicationRunner   receives ApplicationArguments (structured, supports --key=value)
 *   - CommandLineRunner   receives raw String[] args
 *
 * Both run in the same startup phase; use @Order to control relative order.
 */
@Component
public class StartupApplicationRunner implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(StartupApplicationRunner.class);

    private final Environment environment;

    public StartupApplicationRunner(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        logger.info("🚀 ======================================================");
        logger.info("🚀 STARTUP LAYER - ApplicationRunner.run() called");
        logger.info("🚀 ======================================================");

        // Environment properties — includes the one injected by EnvironmentPostProcessor
        logger.info("🚀 Application Name  : {}", environment.getProperty("spring.application.name"));
        logger.info("🚀 Active Profiles   : {}", Arrays.toString(environment.getActiveProfiles()));
        logger.info("🚀 Default Profiles  : {}", Arrays.toString(environment.getDefaultProfiles()));
        logger.info("🚀 Server Port       : {}", environment.getProperty("server.port", "8080"));
        logger.info("🚀 Startup Timestamp : {}", environment.getProperty("app.startup.timestamp", "N/A"));
        logger.info("🚀");

        // Demonstrate ApplicationArguments structured access
        logger.info("🚀 Named args (--key=value at launch):");
        if (args.getOptionNames().isEmpty()) {
            logger.info("🚀   (none — try launching with --demo.mode=true)");
        } else {
            args.getOptionNames().forEach(name ->
                    logger.info("🚀   --{} = {}", name, args.getOptionValues(name)));
        }

        logger.info("🚀 Non-option args:");
        if (args.getNonOptionArgs().isEmpty()) {
            logger.info("🚀   (none)");
        } else {
            args.getNonOptionArgs().forEach(a -> logger.info("🚀   {}", a));
        }

        logger.info("🚀");
        logger.info("🚀 NOTE: ApplicationRunner   → structured ApplicationArguments");
        logger.info("🚀       CommandLineRunner   → raw String[] args");
        logger.info("🚀       Both run after context is fully refreshed.");
        logger.info("🚀       EnvironmentPostProcessor ran BEFORE context refresh.");
        logger.info("🚀 ======================================================");
    }
}
