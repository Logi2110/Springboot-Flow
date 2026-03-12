package com.logi.flow.mvc;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerAdapter;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;

import java.util.Map;

/**
 * LAYER 9 DEMO — HandlerAdapter
 *
 * HandlerAdapter answers one question: "How do I invoke this specific handler type?"
 *
 * DispatcherServlet calls supports() on every registered HandlerAdapter to find
 * one that can execute the handler returned by HandlerMapping.
 * Then it calls handle() on the matched adapter.
 *
 * Different handler types require different invocation strategies:
 *   - RequestMappingHandlerAdapter   → @Controller methods — resolves @RequestBody,
 *                                       @PathVariable, @Valid, runs @InitBinder, etc.
 *   - SimpleControllerHandlerAdapter → legacy Controller interface (handleRequest method)
 *   - HttpRequestHandlerAdapter      → HttpRequestHandler (e.g. static resource servlet)
 *
 * WHY we drive ViewResolver + View explicitly here (not via returned ModelAndView):
 *   Spring Boot registers ContentNegotiatingViewResolver at Ordered.HIGHEST_PRECEDENCE.
 *   It collects ALL ViewResolver candidates and picks the best based on Accept header.
 *   If content-type negotiation fails, InternalResourceViewResolver wins and forwards
 *   to "flow10-view" as a relative URL → "/api/users/flow10-view" → 500 error.
 *
 *   Driving the pipeline explicitly avoids that race. It also mirrors exactly what
 *   RequestMappingHandlerAdapter does for @RestController: write the response and
 *   return null — DispatcherServlet sees no ModelAndView and skips its ViewResolver loop.
 *
 * Execution position in Flow 10:
 *   [1] DispatcherServlet.doDispatch()
 *   [2] HandlerMapping.getHandler()
 *        → [3] THIS.handle(request, response, handler)  ← you are here
 *              → [4] viewResolver.resolveViewName(...)   (called explicitly below)
 *              → [5] view.render(...)                    (called explicitly below)
 *              → returns null — response already written, DispatcherServlet skips its own view loop
 */
@Component
public class MvcInternalsHandlerAdapter implements HandlerAdapter {

    private static final Logger log = LoggerFactory.getLogger(MvcInternalsHandlerAdapter.class);

    @Autowired
    private MvcInternalsViewResolver viewResolver;

    /**
     * Called by DispatcherServlet to check whether this adapter can execute the given handler.
     * DispatcherServlet iterates all HandlerAdapter beans and picks the first that returns true.
     */
    @Override
    public boolean supports(Object handler) {
        boolean supported = handler instanceof CustomMvcHandler;
        if (supported) {
            log.info("  🔧 [HANDLER ADAPTER] MvcInternalsHandlerAdapter.supports() → TRUE (handler is CustomMvcHandler)");
        }
        return supported;
    }

    /**
     * Called by DispatcherServlet to execute the handler and produce (optionally) a ModelAndView.
     *
     * We explicitly drive steps 4 and 5 here, then return null so DispatcherServlet
     * does NOT run its own ViewResolver loop (which would let ContentNegotiatingViewResolver
     * interfere and potentially forward to the wrong URL).
     *
     * This is identical in spirit to how @RestController works:
     *   RequestMappingHandlerAdapter writes the response body via HttpMessageConverter
     *   and returns null — DispatcherServlet skips ViewResolver entirely.
     */
    @Override
    public ModelAndView handle(HttpServletRequest request,
                               HttpServletResponse response,
                               Object handler) throws Exception {
        log.info("  🔧 [HANDLER ADAPTER] MvcInternalsHandlerAdapter.handle() — invoking CustomMvcHandler");

        // Step 3 — execute the handler's business logic
        CustomMvcHandler customHandler = (CustomMvcHandler) handler;
        Map<String, Object> data = customHandler.execute(request);

        // Step 4 — explicitly call ViewResolver (same call DispatcherServlet would make)
        log.info("  🔧 [HANDLER ADAPTER] step 4 — calling viewResolver.resolveViewName('flow10-view')");
        View view = viewResolver.resolveViewName("flow10-view", request.getLocale());

        // Step 5 — explicitly call View.render() (same call DispatcherServlet would make)
        log.info("  🔧 [HANDLER ADAPTER] step 5 — calling view.render(model, request, response)");
        view.render(Map.of("data", data), request, response);

        // Return null: response is committed. DispatcherServlet will skip its ViewResolver loop.
        log.info("  🔧 [HANDLER ADAPTER] returning null (response already written) — DispatcherServlet skips ViewResolver");
        return null;
    }

    @Override
    public long getLastModified(HttpServletRequest request, Object handler) {
        return -1; // -1 = no last-modified caching
    }
}
