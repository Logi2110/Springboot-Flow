package com.logi.flow.resolver;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.util.UUID;

/**
 * ARGUMENT RESOLVER — Layer 2 (Request/Response Processing)
 *
 * Resolves controller method parameters annotated with @InjectRequestInfo.
 * Spring MVC calls supportsParameter() for every parameter in every controller method.
 * When it returns true, resolveArgument() is invoked to produce the parameter value.
 *
 * Execution order (within a single request):
 *   Filter → Interceptor.preHandle → ArgumentResolver  ← HERE → RequestBodyAdvice → AOP → Controller
 *
 * Real-world uses:
 *   - Inject authenticated User object without hitting the DB in every controller
 *   - Inject resolved Tenant, Locale, or Feature-flag objects from headers/tokens
 *   - Inject pagination settings parsed from query params into a custom PageRequest object
 *   - Eliminate repeated request-header boilerplate across controllers
 */
public class RequestInfoArgumentResolver implements HandlerMethodArgumentResolver {

    private static final Logger logger = LoggerFactory.getLogger(RequestInfoArgumentResolver.class);

    /**
     * Called for every parameter of every controller method.
     * Return true only for parameters annotated with @InjectRequestInfo.
     */
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(InjectRequestInfo.class)
                && parameter.getParameterType().equals(RequestInfo.class);
    }

    /**
     * Called when supportsParameter() returns true.
     * Build and return the RequestInfo object from the raw HttpServletRequest.
     */
    @Override
    public Object resolveArgument(MethodParameter parameter,
                                  ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest,
                                  WebDataBinderFactory binderFactory) {
        HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);

        String requestId = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        String method    = request != null ? request.getMethod() : "UNKNOWN";
        String uri       = request != null ? request.getRequestURI() : "UNKNOWN";
        String remote    = request != null ? request.getRemoteAddr() : "UNKNOWN";

        RequestInfo info = new RequestInfo(requestId, method, uri, remote, System.currentTimeMillis());

        logger.info("🔑 2a. ARGUMENT RESOLVER - resolveArgument: injecting RequestInfo {} for parameter '{}'",
                info, parameter.getParameterName());

        return info;
    }
}
