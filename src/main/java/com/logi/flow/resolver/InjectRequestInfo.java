package com.logi.flow.resolver;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Custom method parameter annotation — triggers RequestInfoArgumentResolver.
 *
 * Usage: add @InjectRequestInfo to any controller method parameter of type RequestInfo.
 * Spring MVC will invoke RequestInfoArgumentResolver to populate it automatically.
 *
 * Example:
 *   public ResponseEntity<?> myEndpoint(@InjectRequestInfo RequestInfo info) { ... }
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface InjectRequestInfo {
}
