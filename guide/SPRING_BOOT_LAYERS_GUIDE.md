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
| **ApplicationEventPublisher** | `controller/UserController.java` | Fires `UserProcessedEvent` in Flow 7 (`GET /event-demo`) |
| **@EventListener (sync)** | `event/UserEventListener.java` | Handles event in the same HTTP thread |
| **@Async @EventListener** | `event/UserEventListener.java` | Handles event in a background thread pool |
| **@Async Service** | `event/AsyncDemoService.java` | Fire-and-forget background task (Flow 6) |
| **@Scheduled** | `event/ScheduledDemoTask.java` | Fires every 2 minutes — fixedRate demo |
| **Cache** | `service/CacheDemoService.java` | `@Cacheable` / `@CachePut` / `@CacheEvict` — in-memory cache (Flow 8) |

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

### ~~4. Caching Layer~~ ✅ Added

| Layer | Location | When Used |
|---|---|---|
| **@Cacheable** | `service/CacheDemoService.java` | Cache MISS: runs method, stores result. HIT: Spring skips method entirely. |
| **@CachePut** | `service/CacheDemoService.java` | Always runs method AND updates cache entry. Use after write operations. |
| **@CacheEvict** | `service/CacheDemoService.java` | Removes one or all entries. Next GET is a MISS again. |

**Cache flow** (triggered by `GET/PUT/DELETE /api/users/cache-demo/{id}` — Flow 8):
```
Controller → cacheDemoService.getUser(id)     ← Spring AOP proxy intercepts
                    │
                    ├─ HIT  → returns cached value (~0ms)   ← method body NEVER runs
                    └─ MISS → method body runs (~500ms)
                               stores result in cache 'users'
                               next call is a HIT
```

**Recommended demo sequence** (run in Postman Flow 8a → 8f):
```
Flow 8a: GET  /{id}        → MISS  (~500ms, logs 'CACHE - MISS + CACHE - STORED')
Flow 8b: GET  /{id}        → HIT   (<100ms, NO 'MISS' log — method skipped)
Flow 8c: PUT  /{id}?data=X → @CachePut  (always runs, updates cache)
Flow 8b: GET  /{id}        → HIT   with updated value
Flow 8d: DELETE /{id}      → @CacheEvict (removes one entry)
Flow 8a: GET  /{id}        → MISS  again
Flow 8e: DELETE /           → @CacheEvict(allEntries=true)
```

> 💡 `@EnableCaching` is on `ExecutionFlowConfig`.
> 💡 `CacheManager`: `ConcurrentMapCacheManager` (simple in-memory, no server needed). Switch to `CaffeineCacheManager` (TTL/size), `RedisCacheManager` (distributed) in production.
> 💡 The cache key uses SpEL: `key = "#id"`. Keys must match across `@Cacheable` and `@CachePut`/`@CacheEvict` on the same cache name.
> 💡 Cache state visible at `GET /actuator/caches`.

---

### ~~5. Event / Async Layer~~ ✅ Added

| Layer | Location | When Used |
|---|---|---|
| **Event Publisher** | `controller/UserController.java` | Fires `UserProcessedEvent` in Flow 7 (`GET /event-demo`) |
| **@EventListener (sync)** | `event/UserEventListener.java` | Handles event in the HTTP request thread — lightweight, mandatory side effects |
| **@Async @EventListener** | `event/UserEventListener.java` | Handles event in a thread pool thread — slow/optional side effects (email, push) |
| **@Async Service** | `event/AsyncDemoService.java` | Fire-and-forget background task triggered by Flow 6 |
| **@Scheduled** | `event/ScheduledDemoTask.java` | `fixedRate=2min` — periodic background task demo |

**Event flow** (triggered by `GET /api/users/event-demo` — Flow 7):
```
Controller.eventDemo()
     │
     ├─ eventPublisher.publishEvent(UserProcessedEvent)
     │        │
     │        ├─ UserEventListener.onUserProcessedSync()   ← SYNC: same HTTP thread, blocks here — HTTP waits
     │        └─ UserEventListener.onUserProcessedAsync()  ← ASYNC: thread pool, fires AFTER HTTP 200 sent
     │
     └─ HTTP 200 returned (after sync listener completes)
```

**@Async flow** (triggered by `GET /api/users/async-demo` — Flow 6):
```
Controller calls asyncDemoService.runAsync()
     │
     ├─ Spring proxy submits runAsync() to thread pool
     ├─ Controller returns HTTP 202 IMMEDIATELY  ← watch the logs: response before task finishes
     └─ AsyncDemoService.runAsync() finishes ~2s later on task-N thread
```

> 💡 `@EnableAsync` and `@EnableScheduling` are enabled on `ExecutionFlowConfig`.
> 💡 `@Async` self-calls are silently ignored — the method **must** be called through a Spring proxy (i.e., from a different class).

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
| ✅ Done | **Event Publisher + Listener** | Added — `event/UserEventListener.java`, `UserProcessedEvent.java` |
| ✅ Done | **@Async + @Scheduled** | Added — `event/AsyncDemoService.java`, `ScheduledDemoTask.java` |
| 🟢 Low | **Spring Security** | When auth is required |
| ✅ Done | **Bean Lifecycle** | Added — `lifecycle/BeanLifecycleDemoBean.java`, `FlowBeanPostProcessor`, `FlowBeanFactoryPostProcessor` |
| ✅ Done | **Startup Layer** | Added — `startup/StartupApplicationRunner.java`, `StartupEnvironmentPostProcessor.java`, `StartupInfoStore.java` |
| ✅ Done | **Caching Layer** | Added — `service/CacheDemoService.java` — `@Cacheable` / `@CachePut` / `@CacheEvict` (Flow 8) |


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

### Flow 8: Cache Layer Demo (GET/PUT/DELETE /api/users/cache-demo/{id})

🔥 1.  FILTER - BEFORE
🚀 2.  INTERCEPTOR - preHandle
🎯 3a. AOP - CONTROLLER BEFORE
📋 3.  CONTROLLER - EXECUTING: cacheableDemo() / cachePutDemo() / cacheEvictDemo()
💾 4a. CACHE LAYER - Spring AOP proxy intercepts cacheDemoService call
              ├─ HIT : 💾 CACHE HIT — method skipped, returns instantly (elapsedMs < 100)
              └─ MISS: 💾 CACHE MISS — method runs, result stored (elapsedMs > 400)
📋 5.  CONTROLLER - RETURNING: HTTP 200 with elapsedMs + cacheHint
🎯 5a. AOP - @AfterReturning / @After / @Around AFTER
🚀 6.  INTERCEPTOR - postHandle / afterCompletion
🔥 7.  FILTER - AFTER