package com.solusi.erp.core.infrastructure.web.interceptor;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.ModelAndView;

import static org.assertj.core.api.Assertions.assertThat;

class HtmxViewInterceptorTest {

    private final HtmxViewInterceptor interceptor = new HtmxViewInterceptor();

    @Test
    void postHandle_nonHtmxRequest_noChange() {
        var request = new MockHttpServletRequest();
        var response = new MockHttpServletResponse();
        var mav = new ModelAndView("items/list");

        interceptor.postHandle(request, response, null, mav);

        assertThat(mav.getViewName()).isEqualTo("items/list");
    }

    @Test
    void postHandle_htmxRedirect_setsHxRedirectHeader() {
        var request = htmxRequest(null);
        var response = new MockHttpServletResponse();
        var mav = new ModelAndView("redirect:/items");

        interceptor.postHandle(request, response, null, mav);

        assertThat(response.getHeader("HX-Redirect")).isEqualTo("/items");
    }

    @Test
    void postHandle_htmxRedirectWithTarget_returnsEmptyAndHxTrigger() {
        var request = htmxRequest("row-42");
        var response = new MockHttpServletResponse();
        var mav = new ModelAndView("redirect:/items");

        interceptor.postHandle(request, response, null, mav);

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getHeader("HX-Trigger")).isEqualTo("refresh-table");
        assertThat(mav.getModel()).isEmpty();
    }

    @Test
    void postHandle_htmxWithTarget_appendsFragment() {
        var request = htmxRequest("main-content");
        var response = new MockHttpServletResponse();
        var mav = new ModelAndView("items/list");

        interceptor.postHandle(request, response, null, mav);

        assertThat(mav.getViewName()).isEqualTo("items/list :: main-content");
    }

    @Test
    void postHandle_htmxWithTargetAndExistingFragment_noChange() {
        var request = htmxRequest("main-content");
        var response = new MockHttpServletResponse();
        var mav = new ModelAndView("items/list :: custom-fragment");

        interceptor.postHandle(request, response, null, mav);

        assertThat(mav.getViewName()).isEqualTo("items/list :: custom-fragment");
    }

    @Test
    void postHandle_htmxWithNullModelAndView_noError() {
        var request = htmxRequest(null);
        var response = new MockHttpServletResponse();

        interceptor.postHandle(request, response, null, null);
        // no exception means pass
    }

    @Test
    void postHandle_htmxRedirectTargetBody_setsHxRedirect() {
        var request = htmxRequest("body");
        var response = new MockHttpServletResponse();
        var mav = new ModelAndView("redirect:/items");

        interceptor.postHandle(request, response, null, mav);

        assertThat(response.getHeader("HX-Redirect")).isEqualTo("/items");
    }

    private MockHttpServletRequest htmxRequest(String target) {
        var request = new MockHttpServletRequest();
        request.addHeader("HX-Request", "true");
        if (target != null) {
            request.addHeader("HX-Target", target);
        }
        return request;
    }
}
