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
| **Custom Validator** | `validation/DepartmentValidator.java` | `@ValidDepartment` — rejects unknown department values |
| **DataBinder** | `controller/UserController.java` `@InitBinder` | Trims whitespace from all String fields before `@Valid` |
| **CommandLineRunner** | `demo/ExecutionFlowDemoRunner.java` | Logs demo curl commands at startup (raw `String[]` args) |
| **ApplicationRunner** | `startup/StartupApplicationRunner.java` | Logs env info at startup via structured `ApplicationArguments` |
| **EnvironmentPostProcessor** | `startup/StartupEnvironmentPostProcessor.java` | Injects `app.startup.timestamp` property **before** context refresh |
| **@PostConstruct / @PreDestroy** | `lifecycle/BeanLifecycleDemoBean.java` | Init / cleanup hooks on a single bean after injection / before destruction |
| **BeanPostProcessor** | `lifecycle/FlowBeanPostProcessor.java` | Intercepts every bean before/after its init method (used by AOP, `@Async`, etc.) |
| **BeanFactoryPostProcessor** | `lifecycle/FlowBeanFactoryPostProcessor.java` | Inspects all bean definitions before any instance is created |

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

### ~~3. Validation Layer~~ ✅ Added

| Layer | Location | When Used |
|---|---|---|
| **Custom Validator** | `validation/DepartmentValidator.java` | `@ValidDepartment` — checks department is in allowed set |
| **DataBinder** | `controller/UserController.java` — `@InitBinder` | Registers `StringTrimmerEditor`; runs before `@Valid` for every request to this controller |

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

### ~~6. Bean Lifecycle Layer~~ ✅ Added

| Layer | Location | When Used |
|---|---|---|
| **@PostConstruct / @PreDestroy** | `lifecycle/BeanLifecycleDemoBean.java` | Init/cleanup on one bean after injection / before context shutdown |
| **BeanPostProcessor** | `lifecycle/FlowBeanPostProcessor.java` | Hook called before+after init for **every** bean — how Spring builds AOP proxies |
| **BeanFactoryPostProcessor** | `lifecycle/FlowBeanFactoryPostProcessor.java` | Inspects/modifies bean **definitions** before any instance is created |

**Lifecycle order** (earliest → latest within context startup):
```
🏭 BeanFactoryPostProcessor.postProcessBeanFactory()      ← definitions only, no bean instances yet
         ↓  [Spring instantiates beans — constructors run]
         ↓
🔬 BeanPostProcessor.postProcessBeforeInitialization()    ← before @PostConstruct
🌱 @PostConstruct                                          ← BeanLifecycleDemoBean.init()
🔬 BeanPostProcessor.postProcessAfterInitialization()     ← after @PostConstruct (AOP proxy created here)
         ↓  [Bean ready in context — serves requests]
         ↓
🌱 @PreDestroy                                             ← BeanLifecycleDemoBean.cleanup() on shutdown
```

> 💡 `BeanPostProcessor` is the internal mechanism Spring uses for `@Autowired`, AOP proxies, `@Async`, and `@Scheduled` — your custom one runs alongside all of those.

---

### 7. Security Layer *(requires Spring Security dependency)*

| Layer | Component | When Used |
|---|---|---|
| **Security Filter Chain** | `SecurityFilterChain` | Runs before your `LoggingFilter` — authentication, JWT, OAuth2 |
| **UserDetailsService** | `UserDetailsService` | Load user credentials for authentication |
| **Method Security** | `@PreAuthorize`, `@PostAuthorize` | Fine-grained role/permission checks at method level |

---

### ~~8. Startup Layer~~ ✅ Added

| Layer | Location | When Used |
|---|---|---|
| **CommandLineRunner** | `demo/ExecutionFlowDemoRunner.java` | Already present — logs demo curl commands; receives raw `String[]` args |
| **ApplicationRunner** | `startup/StartupApplicationRunner.java` | Logs env / arg info at startup; receives structured `ApplicationArguments` |
| **EnvironmentPostProcessor** | `startup/StartupEnvironmentPostProcessor.java` | Injects `app.startup.timestamp` **before** context refresh — registered via `META-INF/spring.factories` |

**Startup order** (earliest → latest):
```
🌱 EnvironmentPostProcessor.postProcessEnvironment()   ← BEFORE context refresh (no beans yet)
     │
     ▼  [ApplicationContext created & refreshed]
     │
🚀 ApplicationRunner.run(ApplicationArguments)         ← AFTER context refresh
🌟 CommandLineRunner.run(String...)                    ← AFTER context refresh (same phase)
```

> 💡 `EnvironmentPostProcessor` cannot be a `@Component` — it must be registered in `META-INF/spring.factories` because component scanning hasn't run yet.

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
[HandlerInterceptor.preHandle]         ← Spring Interceptor ✅
     │
     ▼
[RequestBodyAdvice.beforeBodyRead()]   ← Pre-read hook ✅ (LoggingRequestBodyAdvice)
     │
     ▼
[MessageConverter.read()]              ← Deserialize JSON ✅ (LoggingMessageConverter)
     │
     ▼
[RequestBodyAdvice.afterBodyRead()]    ← Post-read hook ✅ (LoggingRequestBodyAdvice)
     │
     ▼
[@InitBinder]                          ← DataBinder setup ✅ (UserController.initBinder)
     │
     ▼
[@Valid → DepartmentValidator]         ← Bean Validation ✅ (DepartmentValidator.isValid)
     │
     ▼
[ArgumentResolver]                     ← Resolve method params ✅ (RequestInfoArgumentResolver)
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
[ResponseBodyAdvice.beforeBodyWrite()] ← Post-process response ✅ (LoggingResponseBodyAdvice)
     │
     ▼
[MessageConverter.write()]             ← Serialize JSON ✅ (LoggingMessageConverter)
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
| ✅ Done | **Custom Validator** | Added — `validation/DepartmentValidator.java` + `@InitBinder` in `UserController` |
| 🟡 Medium | **Event Publisher + Listener** | Clean decoupling for side effects |
| 🟡 Medium | **@Async + @Scheduled** | Background processing patterns |
| 🟢 Low | **Spring Security** | When auth is required |
| ✅ Done | **Bean Lifecycle** | Added — `lifecycle/BeanLifecycleDemoBean.java`, `FlowBeanPostProcessor`, `FlowBeanFactoryPostProcessor` |
| 🟢 Low | **Spring Security** | When auth is required |


## Full Flow Sequence (source of truth flow)

🔥 1.  FILTER - BEFORE
🚀 2.  INTERCEPTOR - preHandle
📨 2a. REQUEST BODY ADVICE - beforeBodyRead
🔄 2b. MESSAGE CONVERTER - read (deserializing)
📨 2c. REQUEST BODY ADVICE - afterBodyRead
✂️ 2d. INIT BINDER - initBinder() → StringTrimmerEditor registered     ← Validation Layer
✅ 2e. @VALID - DepartmentValidator.isValid()                            ← Validation Layer
🔑 2f. ARGUMENT RESOLVER - injecting RequestInfo
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
📤 5d. RESPONSE BODY ADVICE - beforeBodyWrite
🔄 5e. MESSAGE CONVERTER - write (serializing)
🚀 6.  INTERCEPTOR - postHandle
🚀 7.  INTERCEPTOR - afterCompletion
🔥 8.  FILTER - AFTER