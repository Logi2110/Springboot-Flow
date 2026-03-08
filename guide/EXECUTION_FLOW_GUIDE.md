# Spring Boot Execution Flow Demonstration

This project demonstrates the complete execution flow in Spring Boot applications, including all components from filters to method-level annotations.

## 🚀 Complete Execution Flow Order

When a request comes to your Spring Boot application, here's the **exact order** of execution:

### 1. **Filter Level (Servlet Container)**
```
🔥 1. FILTER - BEFORE: HTTP request enters the servlet container
```
- **Location**: `LoggingFilter.java`
- **Level**: Servlet container level (before Spring MVC)
- **Purpose**: Cross-cutting concerns like authentication, logging, CORS
- **Scope**: Can modify request/response headers, body
- **Order**: Executes FIRST, before Spring processes the request

### 2. **Interceptor Level (Spring MVC)**
```
🚀 2. INTERCEPTOR - PRE-HANDLE: Spring MVC level processing starts
```
- **Location**: `LoggingInterceptor.java`
- **Level**: Spring MVC level (within DispatcherServlet)
- **Purpose**: Spring-specific concerns like authentication, request preprocessing
- **Scope**: Access to Spring context, handler information
- **Methods**:
  - `preHandle()` - Before controller method
  - `postHandle()` - After controller, before view rendering
  - `afterCompletion()` - After everything, even after exceptions

### 3. **AOP Aspects (Method Level)**
```
🎯 3a. AOP - CONTROLLER BEFORE: Method-level cross-cutting concerns
🎯 3b. AOP - @Before: Before advice execution
```
- **Location**: `MethodLoggingAspect.java`
- **Level**: Method execution level
- **Purpose**: Cross-cutting concerns like logging, security, transactions
- **Types**:
  - `@Around` - Wraps method execution
  - `@Before` - Before method execution
  - `@After` - After method execution (always)
  - `@AfterReturning` - After successful execution
  - `@AfterThrowing` - After exception

### 4. **Controller Method Execution**
```
📋 3. CONTROLLER - EXECUTING: Business endpoint logic
```
- **Location**: `UserController.java`
- **Level**: Application business logic
- **Purpose**: Handle HTTP requests, demonstrate distinct flow scenarios
- **Endpoints**:
  - `GET /api/users/hello` — Flow 1: minimal, controller only
  - `POST /api/users` — Flow 2: full with `@Valid` + service
  - `GET /api/users/{id}` — Flow 3: programmatic guard + service
  - `GET /api/users/error-demo` — Flow 4: throws RuntimeException

### 5. **Service Layer Execution**
```
🔧 4a. AOP - SERVICE BEFORE: Service method AOP
🔧 4. SERVICE - EXECUTING: Business logic processing
🔧 4b. AOP - SERVICE AFTER: Service method AOP completion
```
- **Location**: `UserService.java`
- **Level**: Business logic layer
- **Purpose**: Implement business rules, data processing
- **Annotations**: `@Service`, `@Transactional`, `@Async`

### 6. **Response Processing (Reverse Order)**
```
🎯 5a. AOP - CONTROLLER AFTER: Controller AOP completion
🎯 5b. AOP - @After: After advice execution
🎯 5c. AOP - @AfterReturning: Successful return advice
📋 5. CONTROLLER - RETURNING: Controller response
🚀 6. INTERCEPTOR - POST-HANDLE: After controller execution
🚀 7. INTERCEPTOR - AFTER-COMPLETION: Complete request processing
🔥 8. FILTER - AFTER: Request processing completed
```

### 7. **Exception Handling (If Errors Occur)**
```
🚨 EXCEPTION HANDLER: Global exception processing
```
- **Location**: `GlobalExceptionHandler.java` 
- **Level**: Application-wide error handling
- **Purpose**: Convert exceptions to proper HTTP responses
- **Annotations**: `@ControllerAdvice`, `@ExceptionHandler`

## 📋 Key Components Explained

### **Filters (`jakarta.servlet.Filter`)**
- **Execution**: Servlet container level
- **Use Cases**: 
  - Authentication/Authorization
  - Request/Response logging
  - CORS handling
  - Request/Response modification
  - Rate limiting

### **Interceptors (`HandlerInterceptor`)**
- **Execution**: Spring MVC level
- **Use Cases**:
  - Spring Security integration
  - Request data preprocessing  
  - Model data addition
  - Performance monitoring

### **AOP Aspects (`@Aspect`)**
- **Execution**: Method level
- **Use Cases**:
  - Transaction management (`@Transactional`)
  - Security (`@PreAuthorize`, `@PostAuthorize`)
  - Caching (`@Cacheable`, `@CacheEvict`)
  - Audit logging
  - Performance monitoring

### **Method-Level Annotations**
- **`@Valid`**: Bean validation (JSR-303)
- **`@Transactional`**: Database transaction management
- **`@Cacheable`**: Method result caching
- **`@Async`**: Asynchronous method execution
- **`@EventListener`**: Application event handling
- **`@PreAuthorize`**: Method-level security

## 🧪 Testing the Flow

Each endpoint demonstrates a distinct execution path. Run them in order to see progressive layer involvement.

### Flow 1 — Minimal (Controller Only)
```bash
curl -X GET http://localhost:8080/api/users/hello
```
**Stack**: Filter → Interceptor → AOP → Controller → AOP → Interceptor → Filter

---

### Flow 2 — Full (Bean Validation + Service)
```bash
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{
    "name": "John Doe",
    "email": "john@example.com",
    "department": "Engineering"
  }'
```
**Stack**: Filter → Interceptor → RequestBodyAdvice → MessageConverter.read → afterBodyRead → `@InitBinder` → `@Valid` → ArgumentResolver → AOP → Controller → Service → AOP → ResponseBodyAdvice → MessageConverter.write → Interceptor → Filter

**Exception branch** — validation failure:
```bash
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{"name": "", "email": "not-an-email", "department": "Unknown"}'
```
**Stack**: ... → `@InitBinder` → `@Valid` fails → `MethodArgumentNotValidException` → `handleValidationExceptions()`

---

### Flow 3 — Programmatic Guard (Path Variable)
```bash
# Happy path — id > 0, proceeds to service
curl -X GET http://localhost:8080/api/users/5

# Exception branch — id <= 0, throws IllegalArgumentException
curl -X GET http://localhost:8080/api/users/-1
```
**Happy path stack**: Filter → Interceptor → AOP → Controller (guard passes) → Service → AOP → Interceptor → Filter

**Exception branch stack**: ... → Controller → `IllegalArgumentException` → `handleIllegalArgumentException()`

---

### Flow 4 — Unhandled Exception (RuntimeException)
```bash
curl -X GET http://localhost:8080/api/users/error-demo
```
**Stack**: Filter → Interceptor → AOP → Controller → `RuntimeException` → AOP `@AfterThrowing` → `GlobalExceptionHandler` → Interceptor `afterCompletion` → Filter

## 📊 Execution Flow Visualization

```
HTTP Request
     │
     ▼
┌─────────────────┐
│   🔥 Filter     │  ◄── Servlet Container Level
└─────────────────┘
     │
     ▼
┌─────────────────┐
│ 🚀 Interceptor  │  ◄── Spring MVC Level  
│   preHandle()   │
└─────────────────┘
     │
     ▼
┌─────────────────┐
│  🎯 AOP Aspect  │  ◄── Method Level
│    @Around      │
└─────────────────┘
     │
     ▼
┌─────────────────┐
│ 📋 Controller   │  ◄── Business Logic
└─────────────────┘
     │
     ▼
┌─────────────────┐
│ 🔧 Service      │  ◄── Business Layer
└─────────────────┘
     │
     ▼
┌─────────────────┐
│  Response       │
└─────────────────┘
     │ (Reverse Order)
     ▼
┌─────────────────┐
│ 🚀 Interceptor  │
│  postHandle()   │
│afterCompletion()│
└─────────────────┘
     │
     ▼
┌─────────────────┐
│   🔥 Filter     │
│    (cleanup)    │
└─────────────────┘
     │
     ▼
HTTP Response
```

## 🔍 Key Learning Points

1. **Filters** operate at the **lowest level** (servlet container)
2. **Interceptors** operate at **Spring MVC level** with access to Spring context  
3. **AOP Aspects** operate at **method level** for cross-cutting concerns
4. **Validation** happens automatically with `@Valid` annotations
5. **Exception handling** can interrupt the normal flow at any point
6. **Execution order** is crucial for understanding request processing
7. **Each component** has specific use cases and capabilities

## 🚦 Best Practices

1. **Use Filters** for servlet-level concerns (authentication, CORS)
2. **Use Interceptors** for Spring-specific preprocessing  
3. **Use AOP** for cross-cutting concerns (logging, transactions)
4. **Use @Valid** for automatic request validation
5. **Use @ControllerAdvice** for centralized exception handling
6. **Keep the execution chain** as lean as possible
7. **Log appropriately** at each level for debugging

Start the application and check the console logs to see the complete execution flow in action!