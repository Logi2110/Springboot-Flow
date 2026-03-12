package com.logi.flow.mvc;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerAdapter;
import org.springframework.web.servlet.ModelAndView;

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
 * This adapter supports only CustomMvcHandler and invokes it, then returns a
 * ModelAndView so that the ViewResolver step fires next.
 *
 * Key insight: @RestController methods NEVER produce a ModelAndView.
 * RequestMappingHandlerAdapter detects @ResponseBody and writes directly via
 * HttpMessageConverter — ViewResolver is skipped entirely.
 * Here we return a ModelAndView explicitly so you can see the resolver step.
 *
 * Execution position in Flow 10:
 *   [1] DispatcherServlet.doDispatch()
 *   [2] HandlerMapping.getHandler()
 *        → [3] THIS.handle(request, response, handler)  ← you are here
 *   [4] ViewResolver.resolveViewName(...)
 *   [5] View.render(...)
 */
@Component
public class MvcInternalsHandlerAdapter implements HandlerAdapter {

    private static final Logger log = LoggerFactory.getLogger(MvcInternalsHandlerAdapter.class);

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
     * Called by DispatcherServlet to execute the handler and produce a ModelAndView.
     *
     * For @ResponseBody methods, RequestMappingHandlerAdapter writes the response
     * directly here and returns null (or an empty ModelAndView) — skipping ViewResolver.
     * We return a named ModelAndView to demonstrate the ViewResolver step.
     */
    @Override
    public ModelAndView handle(HttpServletRequest request,
                               HttpServletResponse response,
                               Object handler) throws Exception {
        log.info("  🔧 [HANDLER ADAPTER] MvcInternalsHandlerAdapter.handle() — invoking CustomMvcHandler");

        CustomMvcHandler customHandler = (CustomMvcHandler) handler;
        Map<String, Object> data = customHandler.execute(request);

        // ModelAndView(viewName, modelAttributeName, modelValue)
        // DispatcherServlet will pass "flow10-view" to ViewResolver next
        ModelAndView mav = new ModelAndView("flow10-view", "data", data);
        log.info("  🔧 [HANDLER ADAPTER] returning ModelAndView(viewName='flow10-view') → ViewResolver will resolve");
        return mav;
    }

    @Override
    public long getLastModified(HttpServletRequest request, Object handler) {
        return -1; // -1 = no last-modified caching
    }
}
