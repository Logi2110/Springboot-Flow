package com.logi.flow.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Event / Async Layer — @Scheduled Demo
 *
 * Demonstrates Spring's three scheduling strategies. Requires @EnableScheduling
 * on a @Configuration class to activate the scheduler.
 *
 * Three scheduling modes:
 *
 *   fixedRate  = N ms → fires every N ms measured from the START of the last execution.
 *                       If execution takes longer than N, the next run starts immediately.
 *                       Use for: heartbeats, polling, cache refresh.
 *
 *   fixedDelay = N ms → fires N ms after the END of the last execution.
 *                       Guarantees a gap between runs regardless of execution time.
 *                       Use for: rate-limited API polling, sequential processing.
 *
 *   cron             → Unix cron expression for exact time-of-day scheduling.
 *                       Format: second minute hour day-of-month month day-of-week
 *                       Use for: daily reports, business-hours-only tasks.
 *
 * Note: only one @Scheduled method is active below to avoid log spam during development.
 * The others are shown as examples — uncomment to try them.
 */
@Component
public class ScheduledDemoTask {

    private static final Logger logger = LoggerFactory.getLogger(ScheduledDemoTask.class);

    /**
     * Active demo: fires every 2 minutes from app start.
     * Adjust to fixedRate = 10_000 (10s) to see it fire faster during testing.
     *
     * Real-world use: cleanup expired sessions, refresh config cache, emit health metrics.
     */
    @Scheduled(fixedRate = 120_000)
    public void periodicCleanup() {
        logger.info("⏰ SCHEDULED (fixedRate=2min) - thread={} | Use case: periodic cleanup / heartbeat",
                Thread.currentThread().getName());
    }

    // --- Other scheduling styles — uncomment to experiment ---

    // Fires 30s after the PREVIOUS execution finishes
    // @Scheduled(fixedDelay = 30_000)
    // public void fixedDelayExample() {
    //     logger.info("⏰ SCHEDULED (fixedDelay=30s) - runs 30s after last execution ended");
    // }

    // Fires every day at 08:00 AM (server timezone)
    // @Scheduled(cron = "0 0 8 * * *")
    // public void dailyReportExample() {
    //     logger.info("⏰ SCHEDULED (cron=daily 08:00) - generate daily report");
    // }
}
