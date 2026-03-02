package com.logi.flow.controller;

import com.logi.flow.dto.UserRequest;
import com.logi.flow.dto.UserResponse;
import com.logi.flow.service.UserService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controller demonstrating different execution flow scenarios.
 *
 * Flow 1 - /hello          : Minimal flow — Controller only, no validation, no service
 * Flow 2 - POST /          : Full flow  — Bean validation (@Valid) → Service layer
 * Flow 3 - GET /{id}       : Programmatic exception — manual throw → IllegalArgumentException handler
 * Flow 4 - GET /error-demo : Unhandled exception — RuntimeException → global exception handler
 */
@RestController
@RequestMapping("/api/users")
@Validated
@CrossOrigin(origins = "*")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    /**
     * FLOW 1: Minimal flow
     * Filter → Interceptor → AOP → Controller (no validation, no service)
     */
    @GetMapping("/hello")
    public ResponseEntity<Map<String, String>> hello() {
        logger.info("📋 3. CONTROLLER - EXECUTING: hello()");

        Map<String, String> response = Map.of(
            "message", "Hello from Spring Boot Flow Demo!",
            "timestamp", String.valueOf(System.currentTimeMillis())
        );

        return ResponseEntity.ok(response);
    }

    /**
     * FLOW 2: Full flow with bean validation and service layer
     * Filter → Interceptor → AOP → Controller → @Valid → Service
     */
    @PostMapping
    public ResponseEntity<UserResponse> processUser(@Valid @RequestBody UserRequest request) {
        logger.info("📋 3. CONTROLLER - EXECUTING: processUser() with request: {}", request);

        UserResponse response = userService.processUser(request);

        logger.info("📋 5. CONTROLLER - RETURNING: {}", response);
        return ResponseEntity.ok(response);
    }

    /**
     * FLOW 3: Programmatic exception flow
     * Filter → Interceptor → AOP → Controller → manual throw → IllegalArgumentException handler
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        logger.info("📋 3. CONTROLLER - EXECUTING: getUserById() with id: {}", id);

        if (id <= 0) {
            throw new IllegalArgumentException("User ID must be positive");
        }

        UserResponse response = userService.getUserById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * FLOW 4: Unhandled exception flow
     * Filter → Interceptor → AOP → Controller → RuntimeException → GlobalExceptionHandler
     */
    @GetMapping("/error-demo")
    public ResponseEntity<String> errorDemo() {
        logger.info("📋 3. CONTROLLER - EXECUTING: errorDemo()");
        throw new RuntimeException("This is a demo exception to show error handling flow");
    }
}