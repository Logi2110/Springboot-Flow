package com.logi.flow.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

/**
 * Event / Async Layer — @Async Demo Service
 *
 * Demonstrates Spring's @Async mechanism directly (without events).
 * Used by Flow 6 (GET /api/users/async-demo).
 *
 * How it works:
 *   1. Controller calls runAsync() — Spring intercepts via AOP proxy.
 *   2. The real method is submitted to the async thread pool and runs independently.
 *   3. Controller returns HTTP 202 IMMEDIATELY — before runAsync() even starts.
 *   4. Watch the logs: "CONTROLLER - RETURNING" appears BEFORE "ASYNC SERVICE - FINISHED".
 *
 * Key constraints (Spring @Async AOP rules):
 *   - @Async method MUST be in a different class from its caller (AOP proxy limitation —
 *     self-calls skip the proxy and @Async is silently ignored).
 *   - The containing class must be a Spring bean.
 *   - Requires @EnableAsync on a @Configuration class.
 *
 * Return type options:
 *   - void                  : pure fire-and-forget
 *   - CompletableFuture<T>  : caller can optionally join the result later
 *   - Future<T>             : older style, prefer CompletableFuture
 */
@Service
public class AsyncDemoService {

    private static final Logger logger = LoggerFactory.getLogger(AsyncDemoService.class);

    /**
     * Simulates a 2-second background task (e.g. report generation, batch processing).
     * The CompletableFuture return allows the caller to optionally wait for the result,
     * but Flow 6 ignores it to demonstrate fire-and-forget.
     */
    @Async
    public CompletableFuture<String> runAsync(String requestId) {
        logger.info("⚡ ASYNC SERVICE - STARTED  | requestId={} | thread={}",
                requestId, Thread.currentThread().getName());

        try {
            Thread.sleep(5000); // simulate background work
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        String result = "Background task done for requestId=" + requestId;
        logger.info("⚡ ASYNC SERVICE - FINISHED | requestId={} | thread={}",
                requestId, Thread.currentThread().getName());
        return CompletableFuture.completedFuture(result);
    }
}
