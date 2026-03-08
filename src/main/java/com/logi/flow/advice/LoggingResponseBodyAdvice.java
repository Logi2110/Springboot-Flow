package com.logi.flow.advice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * RESPONSE BODY ADVICE — Layer 2 (Request/Response Processing)
 *
 * Intercepts the response object AFTER the controller returns it,
 * BEFORE MessageConverter serializes it to JSON and writes to the stream.
 *
 * Execution order:
 *   Controller → Service → AOP @AfterReturning
 *     → beforeBodyWrite()   ← HERE → MessageConverter.write() → Interceptor postHandle → Filter
 *
 * Real-world uses:
 *   - Wrapping all responses in a standard envelope: { "data": ..., "status": "OK" }
 *   - Adding trace IDs / pagination metadata to every response
 *   - Response encryption
 *   - Masking sensitive fields before serialization
 */
@RestControllerAdvice
public class LoggingResponseBodyAdvice implements ResponseBodyAdvice<Object> {

    private static final Logger logger = LoggerFactory.getLogger(LoggingResponseBodyAdvice.class);

    /**
     * Apply this advice to all @RestController methods.
     */
    @Override
    public boolean supports(MethodParameter returnType,
                            Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    /**
     * Called BEFORE MessageConverter serializes the body to JSON.
     * Receives the exact object the controller returned.
     * Can replace/wrap it — e.g. return new ApiResponse<>(body) instead.
     *
     * @param body              The object returned by the controller
     * @param returnType        The controller method return type
     * @param selectedContentType The negotiated content type (e.g. application/json)
     * @param selectedConverterType The chosen MessageConverter class
     * @return The object to actually serialize (return 'body' unchanged here)
     */
    @Override
    public Object beforeBodyWrite(Object body,
                                  MethodParameter returnType,
                                  MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  ServerHttpRequest request,
                                  ServerHttpResponse response) {
        logger.info("📤 5d. RESPONSE BODY ADVICE - beforeBodyWrite: method='{}', contentType='{}', body='{}'",
                returnType.getMethod() != null ? returnType.getMethod().getName() : "unknown",
                selectedContentType,
                body);
        // Return as-is — in real apps you'd wrap: return new ApiResponse<>(body);
        return body;
    }
}
