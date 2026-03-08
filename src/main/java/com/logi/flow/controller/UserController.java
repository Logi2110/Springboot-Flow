package com.logi.flow.controller;

import com.logi.flow.dto.StartupInfoResponse;
import com.logi.flow.dto.UserRequest;
import com.logi.flow.dto.UserResponse;
import com.logi.flow.event.AsyncDemoService;
import com.logi.flow.event.UserProcessedEvent;
import com.logi.flow.resolver.InjectRequestInfo;
import com.logi.flow.resolver.RequestInfo;
import com.logi.flow.service.UserService;
import com.logi.flow.startup.StartupInfoStore;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

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
 * Flow 6 - GET /async-demo   : @Async fire-and-forget — returns 202, task finishes ~2s later
 * Flow 7 - GET /event-demo   : Event chain only — publishes UserProcessedEvent, observe sync + async listeners
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

    @Autowired
    private AsyncDemoService asyncDemoService;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

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

    /**
     * FLOW 6: Async Layer demo
     *
     * What to observe in the logs:
     *   1. CONTROLLER - EXECUTING  (HTTP thread)
     *   2. CONTROLLER - RETURNING  (HTTP thread — response sent BEFORE async finishes)
     *   3. ASYNC SERVICE - STARTED (task-N thread — AFTER controller returns!)
     *   4. ASYNC SERVICE - FINISHED (task-N thread — 2s later)
     *
     * Also try Flow 7 (GET /api/users/event-demo) to see the UserProcessedEvent chain:
     *   Controller → publishEvent() → sync listener (same thread) + async listener (task-N)
     */
    @GetMapping("/async-demo")
    public ResponseEntity<Map<String, String>> asyncDemo() {
        String requestId = String.valueOf(System.currentTimeMillis());
        logger.info("📋 3. CONTROLLER - EXECUTING: asyncDemo() requestId={} | thread={}",
                requestId, Thread.currentThread().getName());

        // Fire-and-forget — Spring submits runAsync() to the thread pool and returns immediately
        asyncDemoService.runAsync(requestId);

        logger.info("📋 5. CONTROLLER - RETURNING immediately (async task still running in background) | thread={}",
                Thread.currentThread().getName());
        return ResponseEntity.accepted().body(Map.of(
                "message", "Async task started — check logs for background execution (finishes in ~2s)",
                "requestId", requestId,
                "hint", "Also try GET /api/users/event-demo to see UserProcessedEvent → sync + async listeners"
        ));
    }

    /**
     * FLOW 7: Event chain demo — publish UserProcessedEvent directly
     *
     * Decouples event observation from the full user-creation flow (Flow 2).
     * What to observe in the logs:
     *   1. CONTROLLER - EXECUTING            (HTTP thread)
     *   2. EVENT LISTENER SYNC - RECEIVED    (same HTTP thread — blocks here)
     *   3. EVENT LISTENER SYNC - DONE        (same HTTP thread)
     *   4. CONTROLLER - RETURNING            (HTTP thread — response sent)
     *   5. EVENT LISTENER ASYNC - RECEIVED   (task-N thread — fires AFTER response)
     *   6. EVENT LISTENER ASYNC - DONE       (task-N thread — ~500ms later)
     *
     * @param name  name for the demo event (default: "Demo User")
     * @param email email for the demo event (default: "demo@example.com")
     */
    @GetMapping("/event-demo")
    public ResponseEntity<Map<String, Object>> eventDemo(
            @RequestParam(defaultValue = "Demo User") String name,
            @RequestParam(defaultValue = "demo@example.com") String email) {

        logger.info("📋 3. CONTROLLER - EXECUTING: eventDemo() name={} email={} | thread={}",
                name, email, Thread.currentThread().getName());

        // Publish event — sync listeners run HERE on this HTTP thread before publishEvent() returns.
        // Async listeners are submitted to the thread pool and run AFTER this method returns.
        eventPublisher.publishEvent(new UserProcessedEvent(this, name, email));

        logger.info("📋 5. CONTROLLER - RETURNING: event published, sync listeners done, async still running | thread={}",
                Thread.currentThread().getName());

        return ResponseEntity.ok(Map.of(
                "message", "UserProcessedEvent published — check logs for sync + async listener execution",
                "name", name,
                "email", email,
                "hint", "Sync listener ran on this HTTP thread; async listener runs on task-N AFTER this response"
        ));
    }
}