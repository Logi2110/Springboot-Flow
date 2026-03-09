package com.logi.flow.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Caching Layer demo — demonstrates @Cacheable, @CachePut, @CacheEvict.
 *
 * Used by Flow 8 endpoints (GET/PUT/DELETE /api/users/cache-demo).
 *
 * How to observe caching in the logs:
 *   MISS  — "CACHE - MISS" log line appears + ~500ms delay (simulated DB call)
 *   HIT   — Spring intercepts BEFORE the method runs → NO log line, instant response
 *   PUT   — "CACHE - @CachePut" log always appears (method always executes)
 *   EVICT — "CACHE - @CacheEvict" log; next GET is a MISS again
 *
 * Key insight: @Cacheable skips the method body entirely on a cache hit.
 * The absence of the "MISS" log line IS the proof of a cache hit.
 */
@Service
public class CacheDemoService {

    private static final Logger logger = LoggerFactory.getLogger(CacheDemoService.class);

    // Simulated "database" — in-memory store pre-seeded with demo data
    private final Map<Long, String> db = new ConcurrentHashMap<>(Map.of(
            1L, "Alice (Engineering)",
            2L, "Bob (Marketing)",
            3L, "Carol (Engineering)"
    ));

    /**
     * @Cacheable — executes ONLY on cache MISS; Spring returns cached value on HIT.
     *
     * On MISS : method runs, result stored in cache 'users' under key=id
     * On HIT  : Spring returns cached value immediately — this method body is NEVER called
     *
     * Observable proof of HIT: "MISS" log line does NOT appear + response is instant (~0ms vs ~500ms).
     *
     * Cache name : "users"
     * Key        : #id (SpEL — evaluates to the id parameter value)
     */
    @Cacheable(value = "users", key = "#id")
    public Map<String, Object> getUser(Long id) {
        logger.info("  💾 CACHE - MISS — id={} NOT in cache, loading from source (simulated 500ms DB call)", id);

        // Simulate slow data source (DB query, remote API, heavy computation)
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        String data = db.getOrDefault(id, "Unknown User (id=" + id + ")");
        Map<String, Object> result = Map.of(
                "id",       id,
                "data",     data,
                "source",   "database",
                "cachedAt", LocalDateTime.now().toString()
        );
        logger.info("  💾 CACHE - STORED — id={} saved in cache 'users'. Next call will be a HIT.", id);
        return result;
    }

    /**
     * @CachePut — ALWAYS executes method body AND updates the cache entry.
     *
     * Unlike @Cacheable, it never skips the method — use for updates so the cache stays
     * consistent with the data source after a write.
     *
     * Cache name : "users"
     * Key        : #id (must match the @Cacheable key to replace the correct entry)
     */
    @CachePut(value = "users", key = "#id")
    public Map<String, Object> updateUser(Long id, String newData) {
        logger.info("  💾 CACHE - @CachePut — always executes + updates cache. id={} newData='{}'", id, newData);

        db.put(id, newData);

        Map<String, Object> result = Map.of(
                "id",       id,
                "data",     newData,
                "source",   "database (updated)",
                "cachedAt", LocalDateTime.now().toString()
        );
        logger.info("  💾 CACHE - @CachePut DONE — id={} refreshed in cache 'users'. Next GET is instant.", id);
        return result;
    }

    /**
     * @CacheEvict — removes one specific entry from the cache.
     * The next call to getUser(id) will be a MISS and load from the data source again.
     *
     * Cache name : "users"
     * Key        : #id
     */
    @CacheEvict(value = "users", key = "#id")
    public void evictUser(Long id) {
        logger.info("  💾 CACHE - @CacheEvict — id={} removed from cache 'users'. Next GET will be a MISS.", id);
    }

    /**
     * @CacheEvict(allEntries = true) — clears ALL entries from the "users" cache at once.
     * Every subsequent GET will be a MISS until re-populated.
     */
    @CacheEvict(value = "users", allEntries = true)
    public void evictAll() {
        logger.info("  💾 CACHE - @CacheEvict(allEntries=true) — ALL entries cleared from cache 'users'");
    }
}
