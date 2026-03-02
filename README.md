# Spring Boot Execution Flow Demo

Welcome to your comprehensive Spring Boot execution flow learning project! This project demonstrates **every component** in the Spring Boot request processing pipeline.

## 🎯 What You'll Learn

- **Complete execution order** from HTTP request to response
- **Filters** (Servlet container level)
- **Interceptors** (Spring MVC level)  
- **AOP Aspects** (Method level cross-cutting concerns)
- **Controller** processing with validation
- **Service layer** business logic
- **Global Exception Handling**
- **Method-level annotations** and their impact

## 🚀 Quick Start

### Option 1: Using Maven (Recommended)
```bash
# Make sure Maven is installed
mvn --version

# Run the application
mvn clean spring-boot:run
```

### Option 2: Using IDE
1. Open the project in your IDE (IntelliJ IDEA, Eclipse, VS Code)
2. Run the `FlowApplication.java` main class
3. The application will start on `http://localhost:8080`

### Option 3: Using the Startup Script (Windows)
```bash
./start-demo.cmd
```

## 🧪 Testing the Execution Flow

Once the application is running, you'll see detailed logs in the console. Test these endpoints to observe the complete execution flow:

### 1. Simple GET Request
```bash
curl -X GET http://localhost:8080/api/users/hello
```
**Expected Flow**: 
```
🔥 1. FILTER - BEFORE
🚀 2. INTERCEPTOR - PRE-HANDLE  
🎯 3a. AOP - CONTROLLER BEFORE
🎯 3b. AOP - @Before
📋 3. CONTROLLER - EXECUTING
🎯 5a. AOP - CONTROLLER AFTER
🎯 5b. AOP - @After
🎯 5c. AOP - @AfterReturning
📋 5. CONTROLLER - RETURNING
🚀 6. INTERCEPTOR - POST-HANDLE
🚀 7. INTERCEPTOR - AFTER-COMPLETION
🔥 8. FILTER - AFTER
```

### 2. POST with Validation (Success)
```bash
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{
    "name": "John Doe",
    "email": "john@example.com",
    "department": "IT"
  }'
```
**Expected Flow**: Same as above + Service layer execution

### 3. POST with Validation Error
```bash
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{
    "name": "",
    "email": "invalid-email"
  }'
```
**Expected Flow**: Request processing + Validation failure + Exception handler

### 4. Get User by ID
```bash
curl -X GET http://localhost:8080/api/users/1
```

### 5. Exception Demo
```bash
curl -X GET http://localhost:8080/api/users/error-demo
```
**Expected Flow**: Request processing + Exception thrown + Exception handler

### 6. Large ID (Triggers Service Exception)
```bash
curl -X GET http://localhost:8080/api/users/1001
```

## 📊 Understanding the Console Logs

When you run any request, watch the console for logs with these emoji indicators:

- **🔥** = Filter level (Servlet container)
- **🚀** = Interceptor level (Spring MVC)  
- **🎯** = AOP Aspect level (Method level)
- **📋** = Controller level (Business endpoint)
- **🔧** = Service level (Business logic)
- **🚨** = Exception Handler (Error processing)

## 📋 Project Structure

```
src/main/java/com/logi/flow/
├── FlowApplication.java              # Main Spring Boot application
├── config/
│   └── ExecutionFlowConfig.java      # Configuration for filters & interceptors
├── filter/
│   └── LoggingFilter.java           # Servlet-level filter
├── interceptor/
│   └── LoggingInterceptor.java      # Spring MVC interceptor  
├── aspect/
│   └── MethodLoggingAspect.java     # AOP aspects for method-level concerns
├── controller/
│   └── UserController.java         # REST controller with various endpoints
├── service/
│   └── UserService.java            # Business logic services
├── dto/
│   ├── UserRequest.java             # Request DTO with validation
│   └── UserResponse.java            # Response DTO
├── exception/
│   └── GlobalExceptionHandler.java  # Global exception handling
└── demo/
    └── ExecutionFlowDemoRunner.java # Demo instructions on startup
```

## 🔍 Key Learning Points

### Execution Order (Important!)
1. **Filter** (Servlet container - lowest level)
2. **Interceptor preHandle()** (Spring MVC level)  
3. **AOP @Around/@Before** (Method level)
4. **Controller** method execution
5. **Service** method execution (with its own AOP)
6. **Controller** returns response
7. **AOP @After/@AfterReturning** (Method level)
8. **Interceptor postHandle() & afterCompletion()** (Spring MVC level)
9. **Filter** cleanup (Servlet container)

### Exception Handling
- Exceptions can occur at **any level**
- **GlobalExceptionHandler** catches and processes them
- **AOP @AfterThrowing** triggers on method exceptions
- **Interceptor afterCompletion()** always executes (even with exceptions)
- **Filter** cleanup always happens

### Method-Level Annotations Demonstrated
- **@Valid**: Automatic request validation
- **@RestController**: REST endpoint exposure
- **@RequestMapping**: URL endpoint mapping  
- **@Aspect**: Cross-cutting concern implementation
- **@Around/@Before/@After**: AOP advice types
- **@ControllerAdvice**: Global exception handling

## 💡 Tips for Learning

1. **Start the application** and watch startup logs
2. **Run each test endpoint** and observe the flow
3. **Try invalid data** to see validation and exception handling
4. **Read the detailed guide**: [EXECUTION_FLOW_GUIDE.md](EXECUTION_FLOW_GUIDE.md)
5. **Modify the code** and see how it affects the flow
6. **Add your own components** following the same patterns

## 🛠️ Troubleshooting

### Maven Issues
If Maven is not installed:
- **Download**: https://maven.apache.org/download.cgi  
- **Install**: Follow Maven installation guides
- **Alternative**: Use your IDE to run `FlowApplication.java`

### Port Issues  
If port 8080 is busy:
- **Change port**: Update `server.port` in `application.yaml`
- **Kill process**: Find and kill the process using port 8080

### Dependency Issues
If you see compilation errors:
- **Clean build**: `mvn clean compile`
- **Reimport**: Refresh/reimport the project in your IDE

## 📚 Further Learning

- **Spring Boot Documentation**: https://spring.io/projects/spring-boot
- **Spring AOP**: https://docs.spring.io/spring-framework/reference/core/aop.html
- **Bean Validation**: https://docs.spring.io/spring-framework/reference/core/validation.html
- **Spring MVC**: https://docs.spring.io/spring-framework/reference/web/webmvc.html

## 🎉 Success Indicators

You'll know everything is working when:
1. ✅ Application starts without errors
2. ✅ You see the demo instructions in console logs  
3. ✅ All curl commands return proper responses
4. ✅ Console logs show the complete execution flow with emoji indicators
5. ✅ Exception scenarios are handled gracefully

**Happy Learning!** 🚀