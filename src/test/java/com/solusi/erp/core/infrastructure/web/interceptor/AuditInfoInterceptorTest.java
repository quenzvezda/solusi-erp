package com.solusi.erp.core.infrastructure.web.interceptor;

import com.solusi.erp.core.dto.BaseAuditResponse;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.ModelAndView;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

class AuditInfoInterceptorTest {

    private final AuditInfoInterceptor interceptor = new AuditInfoInterceptor();

    @Test
    void postHandle_withBaseAuditResponse_addsAuditInfo() throws Exception {
        var auditResponse = new TestAuditResponse();
        auditResponse.setId(1L);

        var mav = new ModelAndView("view");
        mav.addObject("item", auditResponse);

        interceptor.postHandle(null, null, null, mav);

        assertThat(mav.getModel().get("auditInfo")).isSameAs(auditResponse);
    }

    @Test
    void postHandle_withNullModelAndView_noError() throws Exception {
        assertThatCode(() -> interceptor.postHandle(null, null, null, null))
                .doesNotThrowAnyException();
    }

    @Test
    void postHandle_auditInfoAlreadySet_doesNotOverwrite() throws Exception {
        var existing = new TestAuditResponse();
        existing.setId(99L);
        var another = new TestAuditResponse();
        another.setId(1L);

        var mav = new ModelAndView("view");
        mav.addObject("auditInfo", existing);
        mav.addObject("item", another);

        interceptor.postHandle(null, null, null, mav);

        assertThat(((BaseAuditResponse) mav.getModel().get("auditInfo")).getId()).isEqualTo(99L);
    }

    @Test
    void postHandle_withNoBaseAuditResponse_doesNotAddAuditInfo() throws Exception {
        var mav = new ModelAndView("view");
        mav.addObject("item", "plainString");

        interceptor.postHandle(null, null, null, mav);

        assertThat(mav.getModel()).doesNotContainKey("auditInfo");
    }

    private static class TestAuditResponse extends BaseAuditResponse {
    }
}
