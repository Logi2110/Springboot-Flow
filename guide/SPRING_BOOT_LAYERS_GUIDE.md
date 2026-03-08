# Spring Boot - Complete Layers Reference Guide

## ✅ Already in This Project

| Layer | Location | Purpose |
|---|---|---|
| **Filter** | `filter/` | Servlet-level (lowest layer) |
| **Interceptor** | `interceptor/` | Spring MVC level |
| **AOP Aspect** | `aspect/` | Method-level cross-cutting |
| **Controller** | `controller/` | HTTP request handling |
| **Service** | `service/` | Business logic |
| **Exception Handler** | `exception/` | `@ControllerAdvice` / `@ExceptionHandler` |
| **RequestBodyAdvice** | `advice/LoggingRequestBodyAdvice.java` | Pre-process inbound JSON before controller |
| **ResponseBodyAdvice** | `advice/LoggingResponseBodyAdvice.java` | Post-process outbound JSON before serialization |
| **ArgumentResolver** | `resolver/RequestInfoArgumentResolver.java` | Inject custom `RequestInfo` via `@InjectRequestInfo` |
| **MessageConverter** | `config/LoggingMessageConverter.java` | Custom Jackson converter with read/write logging |

---

## ❌ Missing Layers (Grouped by Scenario)

### 1. Data / Persistence Layer
> Most common missing layer in real applications

| Layer | Annotation | When Used |
|---|---|---|
| **Repository** | `@Repository` | Every app with database (Spring Data JPA, JDBC) |
| **Entity** | `@Entity` | ORM/JPA domain model |
| **Transaction** | `@Transactional` | Database operations requiring ACID guarantees |

---

### ~~2. Request / Response Processing Layer~~ ✅ Added

| Layer | Location | When Used |
|---|---|---|
| **RequestBodyAdvice** | `advice/LoggingRequestBodyAdvice.java` | Pre-process all inbound JSON bodies (decryption, logging) |
| **ResponseBodyAdvice** | `advice/LoggingResponseBodyAdvice.java` | Post-process all outbound JSON (wrapping responses, encryption) |
| **ArgumentResolver** | `resolver/RequestInfoArgumentResolver.java` | Injects `RequestInfo` into controller methods via `@InjectRequestInfo` |
| **MessageConverter** | `config/LoggingMessageConverter.java` | Customise JSON/XML serialisation globally |

---

### 3. Validation Layer

| Layer | Interface | When Used |
|---|---|---|
| **Custom Validator** | `ConstraintValidator` | Custom `@Valid` annotation rules beyond built-in ones |
| **DataBinder** | `@InitBinder` | Pre-process request data per-controller |

---

### 4. Caching Layer

| Layer | Annotation | When Used |
|---|---|---|
| **Cache** | `@Cacheable`, `@CacheEvict`, `@CachePut` | Avoid repeated expensive calls (DB, APIs) |

---

### 5. Event / Async Layer

| Layer | Annotation | When Used |
|---|---|---|
| **Event Publisher** | `ApplicationEventPublisher` | Decoupled communication between beans |
| **Event Listener** | `@EventListener`, `@TransactionalEventListener` | React to application events |
| **Async Processing** | `@Async` | Background threads without blocking HTTP response |
| **Scheduler** | `@Scheduled` | Time-based recurring tasks (cron, fixed-rate) |

> 💡 `ApplicationEventPublisher` is already referenced (commented out) in `UserController.java`

---

### 6. Bean Lifecycle Layer

| Layer | Annotation | When Used |
|---|---|---|
| **Init / Destroy** | `@PostConstruct`, `@PreDestroy` | Setup/cleanup on bean creation or shutdown |
| **BeanPostProcessor** | `BeanPostProcessor` | Intercept and modify every Spring bean after creation |
| **BeanFactoryPostProcessor** | `BeanFactoryPostProcessor` | Modify bean definitions before instantiation |

---

### 7. Security Layer *(requires Spring Security dependency)*

| Layer | Component | When Used |
|---|---|---|
| **Security Filter Chain** | `SecurityFilterChain` | Runs before your `LoggingFilter` — authentication, JWT, OAuth2 |
| **UserDetailsService** | `UserDetailsService` | Load user credentials for authentication |
| **Method Security** | `@PreAuthorize`, `@PostAuthorize` | Fine-grained role/permission checks at method level |

---

### 8. Startup Layer

| Layer | Interface | When Used |
|---|---|---|
| **ApplicationRunner** | `ApplicationRunner` | Code that runs once at startup with args |
| **EnvironmentPostProcessor** | `EnvironmentPostProcessor` | Modify environment properties before context refresh |

> 💡 This project already uses `CommandLineRunner` via `ExecutionFlowDemoRunner.java`

---

### 9. Internal Spring MVC Layers *(transparent — always active, no code needed)*

| Layer | Component | Role |
|---|---|---|
| **DispatcherServlet** | Built-in | Front controller — routes requests to handlers |
| **HandlerMapping** | Built-in | Decides which controller handles the request |
| **HandlerAdapter** | Built-in | Calls the controller method with resolved parameters |
| **ViewResolver** | Built-in | Resolves view names to templates (Thymeleaf, etc.) |

---

## Visual: Full Request Lifecycle

```
Browser Request
     │
     ▼
[Security Filter Chain]          ← Spring Security (Layer 7, if added)
     │
     ▼
[LoggingFilter]                  ← jakarta.servlet.Filter ✅
     │
     ▼
[DispatcherServlet]              ← Spring MVC front controller (Layer 9, built-in)
     │
     ▼
[HandlerMapping]                 ← Resolve which controller (Layer 9, built-in)
     │
     ▼
[HandlerInterceptor.preHandle]   ← Spring Interceptor ✅
     │
     ▼
[RequestBodyAdvice]              ← Pre-process request body ✅ (LoggingRequestBodyAdvice)
     │
     ▼
[MessageConverter.read()]        ← Deserialize JSON ✅ (LoggingMessageConverter)
     │
     ▼
[ArgumentResolver]               ← Resolve method params ✅ (RequestInfoArgumentResolver)
     │
     ▼
[AOP @Before / @Around]          ← AOP Aspect ✅
     │
     ▼
[Controller Method]              ← Controller ✅
     │
     ▼
[Service + @Transactional]       ← Service ✅ | Transaction (Layer 1, missing)
     │
     ▼
[Repository → Database]          ← Repository (Layer 1, missing)
     │
     ▼
[AOP @AfterReturning]            ← AOP Aspect ✅
     │
     ▼
[ResponseBodyAdvice]             ← Post-process response ✅ (LoggingResponseBodyAdvice)
     │
     ▼
[MessageConverter.write()]       ← Serialize JSON ✅ (LoggingMessageConverter)
     │
     ▼
[HandlerInterceptor.postHandle]  ← Interceptor ✅
     │
     ▼
[HandlerInterceptor.afterCompletion]
     │
     ▼
[LoggingFilter AFTER]            ← Filter ✅
     │
     ▼
Browser Response

          (if exception at any point → GlobalExceptionHandler ✅)
```

---

## Priority: What to Add Next

| Priority | Layer | Reason |
|---|---|---|
| 🔴 High | **Repository + Entity + Transaction** | Foundation of every real app |
| ✅ Done | **ResponseBodyAdvice** | Added — `advice/LoggingResponseBodyAdvice.java` |
| ✅ Done | **RequestBodyAdvice** | Added — `advice/LoggingRequestBodyAdvice.java` |
| ✅ Done | **ArgumentResolver** | Added — `resolver/RequestInfoArgumentResolver.java` |
| ✅ Done | **MessageConverter** | Added — `config/LoggingMessageConverter.java` |
| 🟡 Medium | **Custom Validator** | Frequently needed for business-specific rules |
| 🟡 Medium | **Event Publisher + Listener** | Clean decoupling for side effects |
| 🟡 Medium | **@Async + @Scheduled** | Background processing patterns |
| 🟢 Low | **Spring Security** | When auth is required |
| 🟢 Low | **BeanPostProcessor** | Advanced framework-level customisation |


## Full Flow Sequence (source of truth flow)

🔥 1.  FILTER - BEFORE
🚀 2.  INTERCEPTOR - preHandle

📨 2a. REQUEST BODY ADVICE - beforeBodyRead             ← new
🔄 2b. MESSAGE CONVERTER - read (deserializing)         ← new
📨 2c. REQUEST BODY ADVICE - afterBodyRead              ← new
🔑 2d. ARGUMENT RESOLVER - injecting RequestInfo        ← new
🎯 3a. AOP - CONTROLLER BEFORE (@Around)
🎯 3b. AOP - @Before
📋 3.  CONTROLLER - EXECUTING: processUser()
🔧 4a. AOP - SERVICE BEFORE
🔧 4.  SERVICE - EXECUTING: processUser()
🔧 4b. AOP - SERVICE AFTER
📋 5.  CONTROLLER - RETURNING
🎯 5a. AOP - @AfterReturning          ← fires 1st (success path only)
🎯 5b. AOP - @After                   ← fires 2nd (always, success or throw)
🎯 5c. AOP - CONTROLLER AFTER         ← fires 3rd (@Around regains control)
📤 5d. RESPONSE BODY ADVICE - beforeBodyWrite           ← new
🔄 5e. MESSAGE CONVERTER - write (serializing)          ← new
🚀 6.  INTERCEPTOR - postHandle
🚀 7.  INTERCEPTOR - afterCompletion
🔥 8.  FILTER - AFTER