package com.logi.flow.service;

import com.logi.flow.dto.UserRequest;
import com.logi.flow.dto.UserResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Service layer demonstrating business logic execution in the flow.
 *
 * Used by:
 *   Flow 2 - processUser() : full flow with bean validation
 *   Flow 3 - getUserById() : programmatic exception path
 */
@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final AtomicLong idGenerator = new AtomicLong(1);

    /**
     * FLOW 2: Called after @Valid passes — demonstrates service layer execution
     */
    public UserResponse processUser(UserRequest request) {
        logger.info("🔧 4. SERVICE - EXECUTING: processUser() with request: {}", request);

        // Simulate business logic / DB processing
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        UserResponse response = new UserResponse(
            idGenerator.getAndIncrement(),
            request.getName(),
            request.getEmail(),
            request.getDepartment()
        );

        logger.info("🔧 4. SERVICE - COMPLETED: processUser() returning: {}", response);
        return response;
    }

    /**
     * FLOW 3: Called only when id > 0 (manual validation already done in controller)
     */
    public UserResponse getUserById(Long id) {
        logger.info("🔧 4. SERVICE - EXECUTING: getUserById() with id: {}", id);

        // Simulate DB lookup
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        UserResponse response = new UserResponse(
            id,
            "User " + id,
            "user" + id + "@example.com",
            "Department " + (id % 5 + 1)
        );

        logger.info("🔧 4. SERVICE - COMPLETED: getUserById() returning: {}", response);
        return response;
    }
}