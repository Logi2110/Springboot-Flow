package com.logi.flow.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * AOP Aspect for method-level cross-cutting concerns
 * This runs around method executions based on pointcut definitions
 */
@Aspect
@Component
public class MethodLoggingAspect {

    private static final Logger logger = LoggerFactory.getLogger(MethodLoggingAspect.class);

    // Pointcut for all controller methods
    @Pointcut("execution(* com.logi.flow.controller..*(..))")
    public void controllerMethods() {}

    // Pointcut for all service methods
    @Pointcut("execution(* com.logi.flow.service..*(..))")
    public void serviceMethods() {}

    // Around advice for controller methods
    @Around("controllerMethods()")
    public Object logControllerMethods(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        Object[] args = joinPoint.getArgs();
        
        logger.info("🎯 3a. AOP - CONTROLLER BEFORE: {}.{}() with args: {}", 
                   className, methodName, Arrays.toString(args));
        
        long startTime = System.currentTimeMillis();
        Object result = null;
        
        try {
            result = joinPoint.proceed(); // Execute the actual method
            
            long duration = System.currentTimeMillis() - startTime;
            logger.info("🎯 5c. AOP - CONTROLLER AFTER (@Around exits): {}.{}() completed in {}ms, result: {}", 
                       className, methodName, duration, result);
            
            return result;
        } catch (Exception e) {
            logger.error("🎯 AOP - CONTROLLER EXCEPTION: {}.{}() threw: {}", 
                        className, methodName, e.getMessage());
            throw e;
        }
    }

    // Around advice for service methods
    @Around("serviceMethods()")
    public Object logServiceMethods(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        Object[] args = joinPoint.getArgs();
        
        logger.info("🔧 4a. AOP - SERVICE BEFORE: {}.{}() with args: {}", 
                   className, methodName, Arrays.toString(args));
        
        long startTime = System.currentTimeMillis();
        
        try {
            Object result = joinPoint.proceed();
            
            long duration = System.currentTimeMillis() - startTime;
            logger.info("🔧 4b. AOP - SERVICE AFTER: {}.{}() completed in {}ms", 
                       className, methodName, duration);
            
            return result;
        } catch (Exception e) {
            logger.error("🔧 AOP - SERVICE EXCEPTION: {}.{}() threw: {}", 
                        className, methodName, e.getMessage());
            throw e;
        }
    }

    // Before advice example
    @Before("controllerMethods()")
    public void beforeControllerMethod(JoinPoint joinPoint) {
        logger.info("🎯 3b. AOP - @Before: About to execute {}", 
                   joinPoint.getSignature().getName());
    }

    // After advice example — fires SECOND after method completes (after @AfterReturning, before @Around)
    @After("controllerMethods()")
    public void afterControllerMethod(JoinPoint joinPoint) {
        logger.info("🎯 5b. AOP - @After (fires 2nd, always): Finished executing {}", 
                   joinPoint.getSignature().getName());
    }

    // AfterReturning advice example — fires FIRST after method returns (before @After and @Around)
    @AfterReturning(pointcut = "controllerMethods()", returning = "result")
    public void afterReturning(JoinPoint joinPoint, Object result) {
        logger.info("🎯 5a. AOP - @AfterReturning (fires 1st on return): {} returned: {}", 
                   joinPoint.getSignature().getName(), result);
    }

    // AfterThrowing advice example
    @AfterThrowing(pointcut = "controllerMethods()", throwing = "exception")
    public void afterThrowing(JoinPoint joinPoint, Exception exception) {
        logger.error("🎯 AOP - @AfterThrowing: {} threw: {}", 
                    joinPoint.getSignature().getName(), exception.getMessage());
    }
}