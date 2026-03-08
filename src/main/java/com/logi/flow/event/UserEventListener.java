package com.logi.flow.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Event / Async Layer — Event Listener
 *
 * Demonstrates two ways to handle an ApplicationEvent fired by UserService (Flow 2):
 *
 *  1. @EventListener (sync)
 *     Runs in the SAME thread as the publisher. The HTTP request thread blocks until
 *     this method returns. Use for lightweight, mandatory side effects.
 *     Log pattern: thread = http-nio-8080-exec-N  (same as controller)
 *
 *  2. @Async @EventListener
 *     Spring dispatches the event to a thread pool thread IMMEDIATELY and the caller
 *     does NOT wait — true fire-and-forget for the publisher.
 *     Log pattern: thread = task-N  (different thread, logs appear AFTER HTTP response)
 *     Requires @EnableAsync on a @Configuration class.
 *
 * Both methods receive the same UserProcessedEvent.
 * Spring calls them independently — each gets its own invocation.
 */
@Component
public class UserEventListener {

    private static final Logger logger = LoggerFactory.getLogger(UserEventListener.class);

    /**
     * SYNC listener — runs in the HTTP request thread.
     * Caller blocks until this returns.
     * Use case: in-memory audit trail, counters, mandatory post-processing.
     */
    @EventListener
    public void onUserProcessedSync(UserProcessedEvent event) {
        logger.info("📣 EVENT LISTENER (SYNC)  - thread={} | Received: {}",
                Thread.currentThread().getName(), event);
        logger.info("📣 EVENT LISTENER (SYNC)  - Use case: lightweight audit log, in-memory stats");
    }

    /**
     * ASYNC listener — runs in a thread pool thread, completely independent of the HTTP request.
     * HTTP response is already sent before this method finishes.
     * Use case: send email, push notification, write to external system.
     */
    @Async
    @EventListener
    public void onUserProcessedAsync(UserProcessedEvent event) {
        logger.info("⚡ EVENT LISTENER (ASYNC) - thread={} | Received: {}",
                Thread.currentThread().getName(), event);

        // Simulate a slow side-effect (e.g. sending a welcome email) that must not block HTTP
        try { Thread.sleep(500); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }

        logger.info("⚡ EVENT LISTENER (ASYNC) - Completed async side-effect for user: {}", event.getUserName());
    }
}
