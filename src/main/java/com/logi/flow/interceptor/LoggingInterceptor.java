package com.logi.flow.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

/**
 * Interceptor executes at Spring MVC level - AFTER Filter but BEFORE Controller
 * This runs within Spring's DispatcherServlet processing
 */
public class LoggingInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(LoggingInterceptor.class);

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, 
                           Object handler) throws Exception {
        logger.info("🚀 2. INTERCEPTOR - PRE-HANDLE: {} {} - Handler: {}", 
                   request.getMethod(), 
                   request.getRequestURI(), 
                   handler.getClass().getSimpleName());
        
        // You can add authentication, authorization, logging here
        // Return false to stop the execution chain
        request.setAttribute("interceptor.startTime", System.currentTimeMillis());
        
        return true; // Continue to controller
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, 
                          Object handler, ModelAndView modelAndView) throws Exception {
        logger.info("🚀 6. INTERCEPTOR - POST-HANDLE: {} {} - ModelAndView: {}", 
                   request.getMethod(), 
                   request.getRequestURI(), 
                   modelAndView != null ? modelAndView.getViewName() : "null");
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, 
                              Object handler, Exception ex) throws Exception {
        Long startTime = (Long) request.getAttribute("interceptor.startTime");
        long duration = startTime != null ? System.currentTimeMillis() - startTime : 0;
        
        logger.info("🚀 7. INTERCEPTOR - AFTER-COMPLETION: {} {} completed in {}ms - Exception: {}", 
                   request.getMethod(), 
                   request.getRequestURI(), 
                   duration,
                   ex != null ? ex.getMessage() : "null");
    }
}