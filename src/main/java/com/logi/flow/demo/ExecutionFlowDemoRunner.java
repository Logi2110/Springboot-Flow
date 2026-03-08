package com.logi.flow.demo;

import com.logi.flow.startup.StartupInfoStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.boot.CommandLineRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Demo runner to show example requests and execution flow
 * This will run automatically when the application starts
 */
@Component
public class ExecutionFlowDemoRunner implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(ExecutionFlowDemoRunner.class);

    @Autowired
    private StartupInfoStore startupInfoStore;

    @Override
    public void run(String... args) throws Exception {
        logger.info("🌟 ===============================================");
        logger.info("🌟 Spring Boot Execution Flow Demo Application Started!");
        logger.info("🌟 ===============================================");
        logger.info("🌟");
        logger.info("🌟 Test the execution flow with these curl commands:");
        logger.info("🌟");
        logger.info("🌟 1. Simple GET request:");
        logger.info("🌟    curl -X GET http://localhost:8080/api/users/hello");
        logger.info("🌟");
        logger.info("🌟 2. Create user (with validation):");
        logger.info("🌟    curl -X POST http://localhost:8080/api/users \\");
        logger.info("🌟      -H \"Content-Type: application/json\" \\");
        logger.info("🌟      -d '{\"name\":\"John Doe\",\"email\":\"john@example.com\",\"department\":\"IT\"}'");
        logger.info("🌟");
        logger.info("🌟 3. Get user by ID:");
        logger.info("🌟    curl -X GET http://localhost:8080/api/users/1");
        logger.info("🌟");
        logger.info("🌟 4. Invalid request (validation error):");
        logger.info("🌟    curl -X POST http://localhost:8080/api/users \\");
        logger.info("🌟      -H \"Content-Type: application/json\" \\");
        logger.info("🌟      -d '{\"name\":\"\",\"email\":\"invalid-email\"}'");
        logger.info("🌟");
        logger.info("🌟 5. Exception demo:");
        logger.info("🌟    curl -X GET http://localhost:8080/api/users/error-demo");
        logger.info("🌟");
        logger.info("🌟 6. Startup + Bean Lifecycle info (Flow 5):");
        logger.info("🌟    curl -X GET http://localhost:8080/api/users/startup-info");
        logger.info("🌟");
        logger.info("🌟 Watch the console logs to see the complete execution flow!");
        logger.info("🌟 ===============================================");

        startupInfoStore.addEvent("🌟 [8] CommandLineRunner.run() — ExecutionFlowDemoRunner executed (same phase as ApplicationRunner)");
    }
}