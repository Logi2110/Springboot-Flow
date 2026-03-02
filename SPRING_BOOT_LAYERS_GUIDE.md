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

### 2. Request / Response Processing Layer

| Layer | Interface / Annotation | When Used |
|---|---|---|
| **RequestBodyAdvice** | `RequestBodyAdvice` | Pre-process all inbound JSON bodies (decryption, logging) |
| **ResponseBodyAdvice** | `ResponseBodyAdvice` | Post-process all outbound JSON (wrapping responses, encryption) |
| **ArgumentResolver** | `HandlerMethodArgumentResolver` | Inject custom objects into controller method parameters |
| **MessageConverter** | `HttpMessageConverter` | Customise JSON/XML serialisation globally |

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
[ArgumentResolver]               ← Resolve method params (Layer 2, missing)
     │
     ▼
[RequestBodyAdvice]              ← Pre-process request body (Layer 2, missing)
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
[ResponseBodyAdvice]             ← Post-process response (Layer 2, missing)
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
| 🔴 High | **ResponseBodyAdvice** | Very common for standard API response wrapping |
| 🟡 Medium | **Custom Validator** | Frequently needed for business-specific rules |
| 🟡 Medium | **Event Publisher + Listener** | Clean decoupling for side effects |
| 🟡 Medium | **@Async + @Scheduled** | Background processing patterns |
| 🟢 Low | **Spring Security** | When auth is required |
| 🟢 Low | **BeanPostProcessor** | Advanced framework-level customisation |
| 🟢 Low | **ArgumentResolver** | Custom parameter injection patterns |
