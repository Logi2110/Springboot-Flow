package com.logi.flow.mvc;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.HandlerMapping;

/**
 * LAYER 9 DEMO — HandlerMapping
 *
 * HandlerMapping answers one question: "Which handler should process this request?"
 *
 * DispatcherServlet holds a sorted list of all HandlerMapping beans and calls
 * getHandler(request) on each one (in order) until a non-null result is returned.
 *
 * Spring Boot's default HandlerMapping beans (registered automatically):
 *   1. RequestMappingHandlerMapping  — maps @GetMapping / @PostMapping / @RequestMapping
 *   2. BeanNameUrlHandlerMapping     — maps URL "/beanName" → a bean named "/beanName"
 *   3. RouterFunctionMapping         — maps WebFlux-style RouterFunction routes
 *   4. WelcomePageHandlerMapping     — maps "/" → welcome page (index.html)
 *
 * This custom HandlerMapping runs first (@Order(1)) and exclusively handles
 * GET /api/users/flow10 → returns a CustomMvcHandler instance.
 * For every other URL it returns null so DispatcherServlet falls through to
 * RequestMappingHandlerMapping which serves all @RestController endpoints.
 *
 * Execution position in Flow 10:
 *   [1] DispatcherServlet.doDispatch()
 *        → [2] THIS.getHandler(request)   ← you are here
 *              → returns HandlerExecutionChain(CustomMvcHandler)
 *        → [3] HandlerAdapter.handle(...)
 *        → [4] ViewResolver.resolveViewName(...)
 *        → [5] View.render(...)
 */
@Component
@Order(1)  // Run before Spring's RequestMappingHandlerMapping (which is @Order(0) by default but added last)
public class MvcInternalsHandlerMapping implements HandlerMapping {

    private static final Logger log = LoggerFactory.getLogger(MvcInternalsHandlerMapping.class);
    private static final String DEMO_PATH = "/api/users/flow10";

    // A single stateless handler instance — safe to reuse across requests
    private final CustomMvcHandler handler = new CustomMvcHandler();

    /**
     * Called by DispatcherServlet for every incoming request.
     *
     * @return HandlerExecutionChain wrapping the handler — or null to pass to the next HandlerMapping.
     */
    @Override
    public HandlerExecutionChain getHandler(HttpServletRequest request) {
        String path   = request.getRequestURI();
        String method = request.getMethod();

        log.debug("  🗺️  [HANDLER MAPPING] MvcInternalsHandlerMapping.getHandler() path='{}' method='{}'", path, method);

        if (DEMO_PATH.equals(path) && "GET".equalsIgnoreCase(method)) {
            log.info("  🗺️  [HANDLER MAPPING] MATCHED path='{}' → returning CustomMvcHandler", path);
            // HandlerExecutionChain can also carry interceptors — here we use none
            return new HandlerExecutionChain(handler);
        }

        // Returning null tells DispatcherServlet to try the next HandlerMapping in its list
        return null;
    }
}
