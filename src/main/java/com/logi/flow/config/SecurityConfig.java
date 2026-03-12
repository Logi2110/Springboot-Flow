package com.logi.flow.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Security Layer — Layer 7.
 *
 * Components:
 *   SecurityFilterChain  — URL-level rules; runs BEFORE LoggingFilter (servlet level, ordered first)
 *   UserDetailsService   — Supplies user credentials for HTTP Basic authentication
 *   @EnableMethodSecurity — Enables @PreAuthorize / @PostAuthorize on service methods
 *
 * Demo users (in-memory, not for production):
 *   user  / pass123  → ROLE_USER
 *   admin / admin123 → ROLE_USER, ROLE_ADMIN
 *
 * Endpoint rules:
 *   /api/users/secure/public     → permitAll  (no auth required)
 *   /api/users/secure/user-only  → any authenticated user (ROLE_USER or ROLE_ADMIN)
 *   /api/users/secure/admin-only → ROLE_ADMIN only (URL-level rule)
 *   /api/users/secure/method-*   → ruled by @PreAuthorize / @PostAuthorize on service method
 *   /api/users/**                → permitAll  (existing flows 1–8 unaffected)
 *   /actuator/**                 → permitAll
 *
 * Auth scheme: HTTP Basic (stateless REST — no session cookie)
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity          // enables @PreAuthorize, @PostAuthorize, @Secured
public class SecurityConfig {

    private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);

    /**
     * SECURITY FILTER CHAIN
     * Defines URL-level security rules applied inside the SecurityFilterChain servlet filter,
     * which runs BEFORE every other Filter (including our LoggingFilter).
     *
     * Execution position in the full request lifecycle:
     *   [SecurityFilterChain]   ← HERE (Spring Security, ordered 0 — before all others)
     *       [LoggingFilter]     ← our custom Filter (ordered 1)
     *           [DispatcherServlet] ...
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        logger.debug("🔒 SECURITY - SecurityFilterChain configuring HTTP rules");

        http
            // Disable CSRF — REST APIs use tokens/Basic auth, not browser sessions
            .csrf(AbstractHttpConfigurer::disable)

            // Stateless — no HTTP session; each request must carry credentials
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            // URL authorisation rules (evaluated top-to-bottom, first match wins)
            .authorizeHttpRequests(auth -> auth
                // Security demo public endpoint — no auth
                .requestMatchers("/api/users/secure/public").permitAll()
                // Admin-only URL-level restriction (also enforced by @PreAuthorize on service)
                .requestMatchers("/api/users/secure/admin-only").hasRole("ADMIN")
                // Any authenticated user (ROLE_USER or ROLE_ADMIN)
                .requestMatchers("/api/users/secure/user-only").authenticated()
                // Method-security endpoints — URL permits all, access controlled by @PreAuthorize
                .requestMatchers("/api/users/secure/method-*").authenticated()
                // All existing flow endpoints (1–8) remain open — not breaking existing demos
                .requestMatchers("/api/users/**").permitAll()
                // Actuator — open for demo
                .requestMatchers("/actuator/**").permitAll()
                // Everything else requires authentication
                .anyRequest().authenticated()
            )

            // HTTP Basic auth — browser prompt or Authorization: Basic base64(user:pass) header
            .httpBasic(basic -> basic
                .realmName("Spring Boot Flow Demo"));

        return http.build();
    }

    /**
     * USER DETAILS SERVICE — in-memory user store for demo.
     *
     * Production: replace with JdbcUserDetailsManager, JPA-backed UserDetailsService,
     * or delegate to an IdP via OAuth2/OIDC.
     *
     * Users:
     *   user  / pass123  → [ROLE_USER]
     *   admin / admin123 → [ROLE_USER, ROLE_ADMIN]
     */
    @Bean
    public UserDetailsService userDetailsService(PasswordEncoder encoder) {
        logger.debug("🔒 SECURITY - InMemoryUserDetailsManager creating demo users");

        var user = User.builder()
                .username("user")
                .password(encoder.encode("pass123"))
                .roles("USER")
                .build();

        var admin = User.builder()
                .username("admin")
                .password(encoder.encode("admin123"))
                .roles("USER", "ADMIN")
                .build();

        return new InMemoryUserDetailsManager(user, admin);
    }

    /**
     * PASSWORD ENCODER — BCrypt (work factor 10).
     * Never store plain-text passwords; always encode at the UserDetailsService boundary.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
