package com.logi.flow.mvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.ViewResolver;

import java.util.Locale;
import java.util.Map;

/**
 * LAYER 9 DEMO — ViewResolver + View
 *
 * ViewResolver answers one question: "Given this logical view name, which View object renders it?"
 *
 * After HandlerAdapter.handle() returns a ModelAndView, DispatcherServlet calls
 * resolveViewName() on every registered ViewResolver (in priority order) until
 * a non-null View is returned.
 *
 * Spring Boot's default ViewResolver beans (registered automatically):
 *   1. ContentNegotiatingViewResolver  — delegates to others based on Accept header; highest priority
 *   2. BeanNameViewResolver            — looks for a @Bean whose name matches the view name
 *   3. ThymeleafViewResolver           — resolves "viewName" → templates/viewName.html (if Thymeleaf present)
 *   4. InternalResourceViewResolver    — resolves "viewName" → /WEB-INF/viewName.jsp (lowest fallback)
 *
 * This resolver runs at @Order(1) and exclusively handles the "flow10-view" name.
 * For all other view names it returns null so DispatcherServlet tries the next resolver.
 *
 * Inner class JsonView is the actual View — it renders the model as JSON.
 *
 * Key insight: @RestController + @ResponseBody BYPASSES ViewResolver entirely.
 * RequestMappingHandlerAdapter writes the response via HttpMessageConverter and
 * returns null from handle() — DispatcherServlet sees no view name to resolve.
 * This flow only activates when a handler returns a real ModelAndView (as we do here).
 *
 * Execution position in Flow 10:
 *   [1] DispatcherServlet.doDispatch()
 *   [2] HandlerMapping.getHandler()
 *   [3] HandlerAdapter.handle()  → returns ModelAndView("flow10-view", model)
 *        → [4] THIS.resolveViewName("flow10-view")  ← you are here
 *              → returns JsonView
 *        → [5] JsonView.render(model, request, response)
 */
@Component
@Order(1)
public class MvcInternalsViewResolver implements ViewResolver {

    private static final Logger log = LoggerFactory.getLogger(MvcInternalsViewResolver.class);
    private static final String DEMO_VIEW = "flow10-view";

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Called by DispatcherServlet with the logical view name from ModelAndView.
     *
     * @return a View that knows how to render the model — or null to try the next ViewResolver.
     */
    @Override
    public View resolveViewName(String viewName, Locale locale) {
        log.debug("  👁️  [VIEW RESOLVER] MvcInternalsViewResolver.resolveViewName('{}', {})", viewName, locale);

        if (DEMO_VIEW.equals(viewName)) {
            log.info("  👁️  [VIEW RESOLVER] MATCHED viewName='{}' → returning JsonView", viewName);
            return new JsonView(objectMapper);
        }

        // Returning null passes responsibility to the next ViewResolver in the chain
        return null;
    }

    // ─────────────────────────────────────────────────────────────────────────

    /**
     * LAYER 9 DEMO — View
     *
     * View is the final step in DispatcherServlet's processing pipeline.
     * After the ViewResolver returns a View, DispatcherServlet calls View.render()
     * which writes the actual HTTP response body.
     *
     * Common built-in View implementations:
     *   - ThymeleafView              → renders .html template with Thymeleaf engine
     *   - MappingJackson2JsonView    → renders model as JSON (already in Spring MVC)
     *   - InternalResourceView       → forwards to a JSP file
     *   - RedirectView               → sends HTTP 302 redirect
     *
     * This JsonView serialises the "data" model attribute as the JSON response body.
     */
    static class JsonView implements View {

        private static final Logger log = LoggerFactory.getLogger(JsonView.class);
        private final ObjectMapper objectMapper;

        JsonView(ObjectMapper objectMapper) {
            this.objectMapper = objectMapper;
        }

        @Override
        public String getContentType() {
            return MediaType.APPLICATION_JSON_VALUE;
        }

        /**
         * Called by DispatcherServlet after ViewResolver returns this View.
         * model contains all attributes from ModelAndView — we render "data".
         */
        @Override
        public void render(Map<String, ?> model,
                           HttpServletRequest request,
                           HttpServletResponse response) throws Exception {
            log.info("  🎨 [VIEW RENDER] JsonView.render() — writing JSON response body");
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding("UTF-8");
            objectMapper.writeValue(response.getWriter(), model.get("data"));
            log.info("  🎨 [VIEW RENDER] response written — DispatcherServlet pipeline complete");
        }
    }
}
