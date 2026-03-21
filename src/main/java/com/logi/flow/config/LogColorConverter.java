package com.logi.flow.config;

import ch.qos.logback.classic.pattern.ClassicConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;

/**
 * Logback converter that colorizes log messages by application layer.
 *
 * Registered as %colorMsg in logback-spring.xml.
 * Color is chosen based on the logger's package / class name so that each
 * layer in the Spring Boot execution flow visually stands out.
 *
 * Color map — 24-bit true-color, matched to the draw.io diagram
 * "Spring Boot -- Full Request Lifecycle":
 *
 *   Filter             → #FF007F  hot pink        (diagram: Filter)
 *   DispatcherServlet  → #1B6EFF  vivid blue      (diagram: DispatcherServlet)
 *   Interceptor        → #00FFCC  neon teal        (diagram: Interceptor)
 *   Advice/Resolver/
 *     Converter        → #BC13FE  neon purple      (diagram: Advice)
 *   Validation         → #FF8C00  amber/orange     (diagram: Validation Layer)
 *   Aspect / AOP       → #39FF14  neon green       (diagram: AOP + Controller)
 *   Controller         → #39FF14  neon green       (diagram: AOP + Controller)
 *   UserTransaction    → Bold + #F7EB07            (diagram: Service / Repository)
 *   Service            → #F7EB07  bright yellow    (diagram: Service / Repository)
 *   Repository / Entity→ #FF6A00  deep orange      (diagram: Database)
 *   Security/Exception → #FF1744  neon red         (diagram: Security)
 *   Cache              → #00E7FF  neon cyan        (infra)
 *   Lifecycle / Startup→ White                     (bootstrap)
 *   Event / Async /
 *     Scheduled        → #00E7FF  neon cyan        (infra)
 *   Config / Demo /
 *     Runner           → Bright White              (app setup)
 *   Hibernate / HikariCP→ #FF6A00 deep orange      (diagram: Database)
 *   Default            → Default terminal color
 */
public class LogColorConverter extends ClassicConverter {

    // ── ANSI 24-bit true-color — format: \u001B[38;2;<R>;<G>;<B>m ─────────────
    // Each constant matches the exact hex from the draw.io neon-flow diagram.
    private static final String RESET = "\u001B[0m";
    private static final String BOLD  = "\u001B[1m";

    // Diagram: Filter           #FF007F  (255,   0, 127) hot pink
    private static final String COLOR_FILTER      = "\u001B[38;2;255;0;127m";
    // Diagram: DispatcherServlet #1B6EFF (27,  110, 255) vivid blue
    private static final String COLOR_DISPATCHER  = "\u001B[38;2;27;110;255m";
    // Diagram: Interceptor      #00FFCC  (  0, 255, 204) neon teal
    private static final String COLOR_INTERCEPTOR = "\u001B[38;2;0;255;204m";
    // Diagram: Advice           #BC13FE  (188,  19, 254) neon purple
    private static final String COLOR_ADVICE      = "\u001B[38;2;188;19;254m";
    // Diagram: Validation Layer #FF8C00  (255, 140,   0) amber/orange
    private static final String COLOR_VALIDATION  = "\u001B[38;2;255;140;0m";
    // Diagram: AOP + Controller #39FF14  ( 57, 255,  20) neon green
    private static final String COLOR_AOP_CTRL    = "\u001B[38;2;57;255;20m";
    // Diagram: Service/Repo     #F7EB07  (247, 235,   7) bright yellow
    private static final String COLOR_SERVICE     = "\u001B[38;2;247;235;7m";
    // Diagram: Database         #FF6A00  (255, 106,   0) deep orange
    private static final String COLOR_DATABASE    = "\u001B[38;2;255;106;0m";
    // Diagram: Security         #FF1744  (255,  23,  68) neon red
    private static final String COLOR_SECURITY    = "\u001B[38;2;255;23;68m";
    // Infra (cache / async / event) — neon cyan, matches diagram Client #00E7FF
    private static final String COLOR_INFRA       = "\u001B[38;2;0;231;255m";
    // Bootstrap (lifecycle / startup / runner / config) — white/bright-white
    private static final String WHITE             = "\u001B[37m";
    private static final String BRIGHT_WHITE      = "\u001B[97m";
    // Spring framework internals — dark gray (keep unobtrusive)
    private static final String DARK_GRAY         = "\u001B[90m";

    // ── Converter entry-point ─────────────────────────────────────────────────

    @Override
    public String convert(ILoggingEvent event) {
        String ansi = ansiFor(event.getLoggerName());
        return ansi + event.getFormattedMessage() + RESET;
    }

    // ── Package → color mapping ───────────────────────────────────────────────

    /** Package-visible so LoggerColorConverter can share the same mapping. */
    static String ansiFor(String logger) {
        if (logger == null) return "";
        String l = logger.toLowerCase();

        // ── Request lifecycle chain ──────────────────────────────────────────
        if (l.contains("filter"))          return COLOR_FILTER;
        if (l.contains("interceptor"))     return COLOR_INTERCEPTOR;
        if (l.contains("aspect"))          return COLOR_AOP_CTRL;

        // ── MVC ──────────────────────────────────────────────────────────────
        if (l.contains("controller"))      return COLOR_AOP_CTRL;

        // ── Data / Persistence Layer (TX service checked before generic service)
        if (l.contains("usertransaction")) return BOLD + COLOR_SERVICE;
        if (l.contains("service"))         return COLOR_SERVICE;
        if (l.contains("repository"))      return COLOR_DATABASE;
        if (l.contains("entity"))          return COLOR_DATABASE;

        // ── Security ─────────────────────────────────────────────────────────
        if (l.contains("security"))        return COLOR_SECURITY;

        // ── Cache ─────────────────────────────────────────────────────────────
        if (l.contains("cache"))           return COLOR_INFRA;

        // ── Bootstrap / Lifecycle ─────────────────────────────────────────────
        if (l.contains("lifecycle"))       return WHITE;
        if (l.contains("startup"))         return WHITE;
        if (l.contains("runner"))          return WHITE;

        // ── Async / Events / Scheduled ────────────────────────────────────────
        if (l.contains("event"))           return COLOR_INFRA;
        if (l.contains("async"))           return COLOR_INFRA;
        if (l.contains("scheduled"))       return COLOR_INFRA;

        // ── Validation ────────────────────────────────────────────────────────
        if (l.contains("validation"))      return COLOR_VALIDATION;

        // ── MVC infrastructure ────────────────────────────────────────────────
        if (l.contains("advice"))          return COLOR_ADVICE;
        if (l.contains("resolver"))        return COLOR_ADVICE;
        if (l.contains("converter"))       return COLOR_ADVICE;

        // ── Error handling ────────────────────────────────────────────────────
        if (l.contains("exception"))       return COLOR_SECURITY;
        if (l.contains("handler"))         return COLOR_SECURITY;

        // ── Configuration / demos ─────────────────────────────────────────────
        if (l.contains("config"))          return BRIGHT_WHITE;
        if (l.contains("demo"))            return BRIGHT_WHITE;

        // ── Spring / Hibernate Framework internals ────────────────────────────
        if (l.contains("hibernate"))       return COLOR_DATABASE;
        if (l.contains("hikari"))          return COLOR_DATABASE;
        if (l.contains("springframework")) return DARK_GRAY;

        return "";  // default terminal color
    }
}
