package com.logi.flow.config;

import ch.qos.logback.classic.pattern.ClassicConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;

/**
 * Logback converter that colorizes the logger name using the same
 * component-color mapping as LogColorConverter.
 *
 * Registered as %colorLogger in logback-spring.xml.
 * Also abbreviates the fully-qualified class name to fit ~36 chars,
 * matching the behaviour of the built-in %logger{36}.
 */
public class LoggerColorConverter extends ClassicConverter {

    private static final String RESET = "\u001B[0m";
    private static final int MAX_LEN  = 36;

    @Override
    public String convert(ILoggingEvent event) {
        String full  = event.getLoggerName();
        String short_ = abbreviate(full);
        String ansi  = LogColorConverter.ansiFor(full);
        return ansi + short_ + RESET;
    }

    /**
     * Abbreviates a dotted class name to at most MAX_LEN characters by
     * shortening each package segment to its first character, keeping the
     * simple class name intact.
     *
     * Examples:
     *   com.logi.flow.controller.UserController → c.l.f.controller.UserController  (30 chars)
     *   org.springframework.web.servlet.DispatcherServlet → o.s.w.s.DispatcherServlet
     */
    static String abbreviate(String name) {
        if (name == null)              return "";
        if (name.length() <= MAX_LEN)  return name;

        String[] parts = name.split("\\.");
        if (parts.length <= 1)         return name;

        StringBuilder sb = new StringBuilder();
        // Abbreviate all segments except the last (class name)
        for (int i = 0; i < parts.length - 1; i++) {
            if (!parts[i].isEmpty()) {
                sb.append(parts[i].charAt(0)).append('.');
            }
        }
        sb.append(parts[parts.length - 1]);

        String result = sb.toString();
        // Hard-truncate if still over the limit (rare)
        if (result.length() > MAX_LEN) {
            result = result.substring(result.length() - MAX_LEN);
        }
        return result;
    }
}
