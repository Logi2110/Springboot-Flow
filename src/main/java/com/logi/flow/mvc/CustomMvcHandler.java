package com.logi.flow.mvc;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * LAYER 9 DEMO — The "Handler" object
 *
 * This is the object that HandlerMapping returns to DispatcherServlet.
 *
 * Normally Spring uses @Controller methods as handlers, wrapped in
 * HandlerMethod objects. Here we define our own handler type so you can
 * see explicitly what DispatcherServlet hands to HandlerAdapter.
 *
 * Chain so far:
 *   DispatcherServlet → HandlerMapping returns THIS → HandlerAdapter executes THIS
 */
public class CustomMvcHandler {

    private static final Logger log = LoggerFactory.getLogger(CustomMvcHandler.class);

    /**
     * The actual business logic for this handler.
     * Called by MvcInternalsHandlerAdapter.handle().
     */
    public Map<String, Object> execute(HttpServletRequest request) {
        log.info("  ⚙️  [HANDLER] CustomMvcHandler.execute() — business logic running");
        log.info("       uri={} method={}", request.getRequestURI(), request.getMethod());

        Map<String, Object> model = new LinkedHashMap<>();
        model.put("flow",               "Flow 10 — Internal Spring MVC Internals Demo");
        model.put("step_1_dispatcher",  "DispatcherServlet received request and iterated HandlerMapping beans");
        model.put("step_2_mapping",     "MvcInternalsHandlerMapping matched /api/users/flow10 → returned CustomMvcHandler");
        model.put("step_3_adapter",     "MvcInternalsHandlerAdapter.handle() invoked CustomMvcHandler.execute()");
        model.put("step_4_view_resolver","MvcInternalsViewResolver resolved 'flow10-view' → JsonView instance");
        model.put("step_5_view_render", "JsonView.render() serialised this model to JSON and wrote the HTTP response");
        model.put("uri",                request.getRequestURI());
        model.put("note",               "In a @RestController, steps 4+5 are replaced by HttpMessageConverter — ViewResolver is bypassed");
        return model;
    }
}
