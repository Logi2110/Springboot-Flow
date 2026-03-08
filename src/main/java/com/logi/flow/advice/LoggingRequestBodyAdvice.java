package com.logi.flow.advice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdvice;

import java.io.IOException;
import java.lang.reflect.Type;

/**
 * REQUEST BODY ADVICE — Layer 2 (Request/Response Processing)
 *
 * Intercepts the raw request body AFTER MessageConverter reads it,
 * before the resolved object is passed to the controller method.
 *
 * Execution order:
 *   Filter → Interceptor → ArgumentResolver → MessageConverter.read()
 *     → beforeBodyRead() → [body read] → afterBodyRead()   ← HERE
 *   → AOP → Controller
 *
 * Real-world uses:
 *   - Decryption of encrypted payloads
 *   - Request body logging / auditing
 *   - Signature verification
 *   - Header-based tenant context injection into body
 */
@RestControllerAdvice
public class LoggingRequestBodyAdvice implements RequestBodyAdvice {

    private static final Logger logger = LoggerFactory.getLogger(LoggingRequestBodyAdvice.class);

    /**
     * Apply this advice to all @RequestBody parameters across all controllers.
     */
    @Override
    public boolean supports(MethodParameter methodParameter, Type targetType,
                            Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    /**
     * Called BEFORE the MessageConverter reads the request body stream.
     * Can wrap/replace the HttpInputMessage (e.g. decrypt body here).
     */
    @Override
    public HttpInputMessage beforeBodyRead(HttpInputMessage inputMessage,
                                           MethodParameter parameter,
                                           Type targetType,
                                           Class<? extends HttpMessageConverter<?>> converterType) throws IOException {
        logger.info("📨 2a. REQUEST BODY ADVICE - beforeBodyRead: parameter='{}', targetType='{}', converter='{}'",
                parameter.getParameterName(),
                targetType.getTypeName(),
                converterType.getSimpleName());
        // Return as-is — no modification in this demo
        return inputMessage;
    }

    /**
     * Called AFTER the MessageConverter has successfully deserialized the body.
     * The 'body' object is the fully deserialized Java object (e.g. UserRequest).
     * Can replace or modify it before it reaches the controller.
     */
    @Override
    public Object afterBodyRead(Object body, HttpInputMessage inputMessage,
                                MethodParameter parameter, Type targetType,
                                Class<? extends HttpMessageConverter<?>> converterType) {
        logger.info("📨 2c. REQUEST BODY ADVICE - afterBodyRead: deserialized body='{}'", body);
        // Return as-is — no modification in this demo
        return body;
    }

    /**
     * Called when the request body is empty (no body sent).
     */
    @Override
    public Object handleEmptyBody(Object body, HttpInputMessage inputMessage,
                                  MethodParameter parameter, Type targetType,
                                  Class<? extends HttpMessageConverter<?>> converterType) {
        logger.info("📨 REQUEST BODY ADVICE - handleEmptyBody: parameter='{}'", parameter.getParameterName());
        return body;
    }
}
