package com.logi.flow.controller;

import com.logi.flow.dto.StartupInfoResponse;
import com.logi.flow.dto.UserRequest;
import com.logi.flow.dto.UserResponse;
import com.logi.flow.resolver.InjectRequestInfo;
import com.logi.flow.resolver.RequestInfo;
import com.logi.flow.service.UserService;
import com.logi.flow.startup.StartupInfoStore;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.InitBinder;

import java.util.Map;

/**
 * Controller demonstrating different execution flow scenarios.
 *
 * Flow 1 - /hello          : Minimal flow — Controller only, no validation, no service
 * Flow 2 - POST /          : Full flow   — ArgumentResolver + RequestBodyAdvice + MessageConverter
 *                                          + @Valid + Service + ResponseBodyAdvice
 * Flow 3 - GET /{id}       : Programmatic exception — manual throw → IllegalArgumentException handler
 * Flow 4 - GET /error-demo : Unhandled exception — RuntimeException → global exception handler
 * Flow 5 - GET /startup-info : Startup + Bean Lifecycle events collected during boot
 */
@RestController
@RequestMapping("/api/users")
@Validated
@CrossOrigin(origins = "*")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private StartupInfoStore startupInfoStore;

    /**
     * VALIDATION LAYER — DataBinder
     * Called by Spring MVC before binding request data to any method parameter in this controller.
     * Runs after Interceptor.preHandle() and before @Valid bean validation.
     *
     * Execution position in Flow 2 (POST /api/users):
     *   Interceptor.preHandle → ArgumentResolver → MessageConverter.read
     *     → @InitBinder (trim whitespace) → @Valid → Controller method
     */
    @InitBinder
    public void initBinder(WebDataBinder binder) {
        logger.info("🔗 VALIDATION - INIT BINDER: Registering StringTrimmerEditor — trimming all String fields");
        // Trims leading/trailing whitespace from all bound String values.
        // false = keep empty strings as-is (do not convert to null)
        binder.registerCustomEditor(String.class, new StringTrimmerEditor(false));
    }

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
     * FLOW 2: Full flow — all Layer 2 components active
     * Filter → Interceptor → ArgumentResolver → RequestBodyAdvice + MessageConverter.read
     *   → AOP → Controller → @Valid → Service → AOP → ResponseBodyAdvice + MessageConverter.write
     *   → Interceptor → Filter
     *
     * @param request      Resolved by @RequestBody + RequestBodyAdvice + MessageConverter
     * @param requestInfo  Resolved by RequestInfoArgumentResolver (triggered by @InjectRequestInfo)
     */
    @PostMapping
    public ResponseEntity<UserResponse> processUser(@Valid @RequestBody UserRequest request,
                                                    @InjectRequestInfo RequestInfo requestInfo) {
        logger.info("📋 3. CONTROLLER - EXECUTING: processUser() requestInfo={}, request={}", requestInfo, request);

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

    /**
     * FLOW 5: Startup and Bean Lifecycle Layer demo
     * Returns real-time startup events captured during boot by:
     *   EnvironmentPostProcessor → BeanFactoryPostProcessor → BeanPostProcessor
     *   → @PostConstruct → ApplicationRunner → CommandLineRunner
     *
     * @param requestInfo  Resolved by RequestInfoArgumentResolver
     */
    @GetMapping("/startup-info")
    public ResponseEntity<StartupInfoResponse> getStartupInfo(@InjectRequestInfo RequestInfo requestInfo) {
        logger.info("📋 3. CONTROLLER - EXECUTING: getStartupInfo() requestInfo={}", requestInfo);
        StartupInfoResponse response = startupInfoStore.toResponse();
        logger.info("📋 5. CONTROLLER - RETURNING startup info: {} flow beans, {} startup events",
                response.getFlowBeanCount(), response.getStartupSequence().size());
        return ResponseEntity.ok(response);
    }
}