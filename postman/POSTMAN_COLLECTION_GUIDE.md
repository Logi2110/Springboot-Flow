# Spring Boot Flow API - Postman Collection Guide

## Files
- `SpringBoot-Flow-API.postman_collection.json` - Complete API collection
- `SpringBoot-Flow-Development.postman_environment.json` - Environment variables

---

## Import Instructions

1. Open Postman → click **Import**
2. Select `SpringBoot-Flow-API.postman_collection.json` → **Import**
3. Import again → select `SpringBoot-Flow-Development.postman_environment.json`
4. Select **"SpringBoot Flow - Development"** from the environment dropdown

---

## Collection Structure

### 📁 Execution Flows
Each request demonstrates a **distinct path** through the layer stack.

| Request | Endpoint | Layers Involved |
|---|---|---|
| Flow 1 — Minimal | `GET /api/users/hello` | Filter → Interceptor → AOP → Controller |
| Flow 2 — Full | `POST /api/users` | + `@Valid` → Service |
| Flow 3 — Programmatic Guard (happy) | `GET /api/users/5` | + manual guard passes → Service |
| Flow 4 — Unhandled Exception | `GET /api/users/error-demo` | + RuntimeException → GlobalExceptionHandler |

### 📁 Exception Paths
Requests that trigger exception handler branches.

| Request | Endpoint | Exception Handler |
|---|---|---|
| Flow 3 — Guard fails | `GET /api/users/-1` | `handleIllegalArgumentException()` |
| Flow 2 — Validation fails | `POST /api/users` (invalid body) | `handleValidationExceptions()` |

### 📁 Actuator
Spring Boot monitoring endpoints — outside the execution flow.

| Request | Endpoint |
|---|---|
| Health Check | `GET /actuator/health` |
| Application Info | `GET /actuator/info` |
| Metrics Overview | `GET /actuator/metrics` |
| JVM Memory Metrics | `GET /actuator/metrics/jvm.memory.used` |

---

## Recommended Run Sequence

1. **Health Check** — confirm app is running
2. **Flow 1** — minimal path, see Filter/Interceptor/AOP in logs
3. **Flow 2** — full path, see validation + service in logs
4. **Flow 3 (happy)** — service called, see `processUser` vs `getUserById` in logs
5. **Flow 4** — see `@AfterThrowing` + `GlobalExceptionHandler` in logs
6. **Flow 3 exception** — see `handleIllegalArgumentException()` in logs
7. **Flow 2 validation fail** — see `handleValidationExceptions()` in logs

---

## Environment Variables

| Variable | Default | Description |
|---|---|---|
| `base_url` | `http://localhost:8080` | Spring Boot application base URL |
| `user_id` | `1` | Captured from Flow 2 POST response |

---

## What the Test Scripts Validate

- **Status codes** — correct HTTP status per flow
- **Response structure** — verifies error response fields (`status`, `error`, `message`, `timestamp`)
- **Response time** — global test: must be < 2000ms
- **Console log** — every request logs `[METHOD] URL → STATUS (Xms)`