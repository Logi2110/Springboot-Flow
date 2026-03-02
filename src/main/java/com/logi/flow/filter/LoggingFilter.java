package com.logi.flow.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Filter executes at the Servlet container level - FIRST in the execution chain
 * This runs before Spring's DispatcherServlet processes the request
 */
public class LoggingFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(LoggingFilter.class);

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        logger.info("🔧 FILTER INITIALIZED: LoggingFilter");
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, 
                        FilterChain filterChain) throws IOException, ServletException {
        
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        
        logger.info("🔥 1. FILTER - BEFORE: {} {} from {}", 
                   request.getMethod(), 
                   request.getRequestURI(), 
                   request.getRemoteAddr());
        
        // Add custom request/response headers if needed
        response.setHeader("X-Custom-Filter", "LoggingFilter-Processed");
        
        long startTime = System.currentTimeMillis();
        
        try {
            // Continue the filter chain - this will call next filter or DispatcherServlet
            filterChain.doFilter(servletRequest, servletResponse);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            logger.info("🔥 8. FILTER - AFTER: {} {} completed in {}ms with status {}", 
                       request.getMethod(), 
                       request.getRequestURI(), 
                       duration,
                       response.getStatus());
        }
    }

    @Override
    public void destroy() {
        logger.info("🔧 FILTER DESTROYED: LoggingFilter");
    }
}