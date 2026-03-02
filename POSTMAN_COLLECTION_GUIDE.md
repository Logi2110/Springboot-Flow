# Spring Boot Flow API - Postman Collection Guide

## Files Created
- `SpringBoot-Flow-API.postman_collection.json` - Complete API collection
- `SpringBoot-Flow-Development.postman_environment.json` - Environment variables

## Import Instructions

### 1. Import Collection
1. Open Postman
2. Click **Import** button
3. Select `SpringBoot-Flow-API.postman_collection.json`
4. Click **Import**

### 2. Import Environment
1. Click **Import** button again
2. Select `SpringBoot-Flow-Development.postman_environment.json`
3. Click **Import**
4. Select "SpringBoot Flow - Development" environment from the dropdown

## Collection Structure

### 📁 User Management API
- **GET** `/api/users/hello` - Simple connectivity test
- **POST** `/api/users` - Create user with validation
- **GET** `/api/users/{id}` - Get user by ID
- **PUT** `/api/users/{id}` - Update user
- **DELETE** `/api/users/{id}` - Delete user

### 📁 Error Handling & Validation
- **GET** `/api/users/error-demo` - Exception handling demo
- **GET** `/api/users/-1` - Invalid ID validation
- **POST** `/api/users` - Invalid data validation

### 📁 Actuator Endpoints
- **GET** `/actuator/health` - Application health
- **GET** `/actuator/info` - Application info
- **GET** `/actuator/metrics` - Metrics overview
- **GET** `/actuator/metrics/jvm.memory.used` - Memory metrics

## Testing Workflow

### Quick Test Sequence
1. **Health Check** - Verify application is running
2. **Hello Endpoint** - Test basic connectivity
3. **Create User** - Test full execution flow with validation
4. **Get User** - Verify creation and retrieval
5. **Update User** - Test update functionality
6. **Delete User** - Test deletion
7. **Error Demo** - Test exception handling

### Validation Tests
- Run "Invalid User Data" to see validation errors
- Run "Invalid User ID" to see path validation
- Check logs to see execution flow (Filter → Interceptor → Aspect → Controller → Service)

## Features Included

### ✅ Test Scripts
- Automatic response validation
- Status code verification
- Response structure validation
- Performance testing (response time < 2000ms)

### ✅ Environment Variables
- `{{base_url}}` - Configurable server URL
- `{{user_id}}` - Automatically captured from create user response

### ✅ Request Examples
- Valid request bodies with proper data types
- Invalid request examples for validation testing
- Proper headers and content types

### ✅ Execution Flow Coverage
- Tests all layers: Filter → Interceptor → Aspect → Controller → Service
- Validates exception handling and global error handler
- Covers all HTTP methods (GET, POST, PUT, DELETE)

## Environment Variables

| Variable | Default Value | Description |
|----------|---------------|-------------|
| `base_url` | `http://localhost:8080` | API base URL |
| `user_id` | `1` | User ID for individual operations |
| `api_version` | `v1` | API version |

## Notes
- Make sure your Spring Boot application is running on port 8080
- The collection automatically captures user IDs from create operations
- All requests include proper validation and error handling tests
- Check the Postman console for execution flow logs