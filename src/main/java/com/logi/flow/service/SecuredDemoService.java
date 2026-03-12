package com.logi.flow.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Security Layer — method-level access control demo.
 *
 * Demonstrates @PreAuthorize and @PostAuthorize, which are enforced by
 * Spring Security's AOP proxy BEFORE and AFTER the method body runs.
 *
 * Requires @EnableMethodSecurity on SecurityConfig.
 *
 * Execution position in the layer stack (for @PreAuthorize):
 *   [SecurityFilterChain]  ← already authenticated here
 *   [LoggingFilter]
 *   [DispatcherServlet → Interceptor → Controller]
 *       [AOP @PreAuthorize proxy]  ← HERE — throws AccessDeniedException if rule fails
 *           [SecuredDemoService method body]
 *       [AOP @PostAuthorize proxy] ← or HERE for @PostAuthorize (after method runs)
 *
 * @PreAuthorize  — evaluated BEFORE the method runs; use for input-based access control
 * @PostAuthorize — evaluated AFTER the method runs against the return value;
 *                  use to restrict what data the caller can see (owner checks, etc.)
 */
@Service
public class SecuredDemoService {

    private static final Logger logger = LoggerFactory.getLogger(SecuredDemoService.class);

    /**
     * No security annotation — any caller (including unauthenticated) can invoke this.
     * URL-level rule in SecurityFilterChain is the only gate here.
     */
    public Map<String, Object> getPublicData() {
        logger.info("  🔓 SECURITY SERVICE - getPublicData(): no method-level restriction");
        return Map.of(
                "data",       "Public data — visible to everyone, no credentials required",
                "security",   "none",
                "annotation", "none — URL rule: permitAll"
        );
    }

    /**
     * @PreAuthorize("isAuthenticated()")
     *
     * Spring Security's AOP proxy checks authentication BEFORE calling this method.
     * Any valid credentials (ROLE_USER or ROLE_ADMIN) pass.
     * Unauthenticated calls → 401 Unauthorized (never reaches this method body).
     *
     * SpEL expression: isAuthenticated() — true if principal is not anonymous
     */
    @PreAuthorize("isAuthenticated()")
    public Map<String, Object> getUserData(String callerName) {
        logger.info("🔐 SECURITY SERVICE - getUserData(): @PreAuthorize(isAuthenticated) passed for '{}'", callerName);
        return Map.of(
                "data",       "User data — requires any valid login",
                "caller",     callerName,
                "security",   "@PreAuthorize(isAuthenticated)",
                "annotation", "@PreAuthorize"
        );
    }

    /**
     * @PreAuthorize("hasRole('ADMIN')")
     *
     * Only callers with ROLE_ADMIN pass. ROLE_USER → 403 Forbidden.
     * Spring prefixes roles automatically: hasRole('ADMIN') checks for 'ROLE_ADMIN'.
     *
     * SpEL expression: hasRole('ADMIN') — equivalent to hasAuthority('ROLE_ADMIN')
     */
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, Object> getAdminData(String callerName) {
        logger.info("  🔐 SECURITY SERVICE - getAdminData(): @PreAuthorize(ADMIN) passed for '{}'", callerName);
        return Map.of(
                "data",       "Admin data — restricted to ROLE_ADMIN",
                "caller",     callerName,
                "security",   "@PreAuthorize(hasRole('ADMIN'))",
                "annotation", "@PreAuthorize"
        );
    }

    /**
     * @PostAuthorize("returnObject.get('owner') == authentication.name or hasRole('ADMIN')")
     *
     * Method body ALWAYS runs; Spring evaluates the SpEL expression against the returned object
     * AFTER the method completes. If the rule fails → 403 Forbidden (and the response is blocked).
     *
     * Use case: row-level / record-level ownership checks where the owner is only known
     * after fetching the data from the store.
     *
     * SpEL variables:
     *   returnObject       — the Map returned by this method
     *   authentication.name — the logged-in username
     */
    @PostAuthorize("returnObject.get('owner') == authentication.name or hasRole('ADMIN')")
    public Map<String, Object> getOwnedData(String requestedOwner) {
        logger.info("  🔐 SECURITY SERVICE - getOwnedData(): method ran for owner='{}', @PostAuthorize will check caller", requestedOwner);
        // Method always runs; @PostAuthorize decides whether the caller can see the result
        return Map.of(
                "data",       "Owned record — owner=" + requestedOwner,
                "owner",      requestedOwner,
                "security",   "@PostAuthorize(owner == caller or ADMIN)",
                "annotation", "@PostAuthorize",
                "note",       "This method already ran — @PostAuthorize checks returnObject after execution"
        );
    }
}
