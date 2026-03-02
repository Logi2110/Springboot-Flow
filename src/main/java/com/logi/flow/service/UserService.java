package com.logi.flow.service;

import com.logi.flow.dto.UserRequest;
import com.logi.flow.dto.UserResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Service layer demonstrating business logic execution in the flow
 */
@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    
    private final AtomicLong idGenerator = new AtomicLong(1);

    public UserResponse createUser(UserRequest request) {
        logger.info("🔧 4. SERVICE - EXECUTING: createUser() with request: {}", request);
        
        // Simulate some business logic processing time
        try {
            Thread.sleep(100); // Simulate database operations
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Create user response
        UserResponse response = new UserResponse(
            idGenerator.getAndIncrement(),
            request.getName(),
            request.getEmail(),
            request.getDepartment()
        );
        
        logger.info("🔧 4. SERVICE - COMPLETED: createUser() returning: {}", response);
        return response;
    }

    public UserResponse getUserById(Long id) {
        logger.info("🔧 4. SERVICE - EXECUTING: getUserById() with id: {}", id);
        
        // Simulate database lookup
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        if (id > 1000) {
            throw new RuntimeException("User not found with id: " + id);
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

    public UserResponse updateUser(Long id, UserRequest request) {
        logger.info("🔧 4. SERVICE - EXECUTING: updateUser() with id: {}, request: {}", id, request);
        
        // Simulate update operations
        try {
            Thread.sleep(75);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        UserResponse response = new UserResponse(
            id,
            request.getName(),
            request.getEmail(),
            request.getDepartment()
        );
        
        logger.info("🔧 4. SERVICE - COMPLETED: updateUser() returning: {}", response);
        return response;
    }

    public void deleteUser(Long id) {
        logger.info("🔧 4. SERVICE - EXECUTING: deleteUser() with id: {}", id);
        
        // Simulate delete operations
        try {
            Thread.sleep(30);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        logger.info("🔧 4. SERVICE - COMPLETED: deleteUser() for id: {}", id);
    }
}