package com.solusi.erp.accounting.period.web.controller;

import com.solusi.erp.accounting.period.application.usecase.command.*;
import com.solusi.erp.accounting.period.application.usecase.query.*;
import com.solusi.erp.accounting.period.domain.model.AccountingPeriod;
import com.solusi.erp.accounting.period.domain.model.FiscalYear;
import com.solusi.erp.accounting.period.web.dto.*;
import com.solusi.erp.accounting.period.web.mapper.PeriodWebMapper;
import com.solusi.erp.core.domain.model.DeleteResult;
import com.solusi.erp.core.dto.ApiResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@DisplayName("PeriodController — Unit Test")
public class PeriodControllerTest {

    private final CreateFiscalYearUseCase createFiscalYearUseCase = mock(CreateFiscalYearUseCase.class);
    private final UpdateFiscalYearUseCase updateFiscalYearUseCase = mock(UpdateFiscalYearUseCase.class);
    private final DeleteFiscalYearUseCase deleteFiscalYearUseCase = mock(DeleteFiscalYearUseCase.class);
    private final ClosePeriodUseCase closePeriodUseCase = mock(ClosePeriodUseCase.class);
    private final ReopenPeriodUseCase reopenPeriodUseCase = mock(ReopenPeriodUseCase.class);
    private final FindFiscalYearsUseCase findFiscalYearsUseCase = mock(FindFiscalYearsUseCase.class);
    private final GetFiscalYearDetailUseCase getFiscalYearDetailUseCase = mock(GetFiscalYearDetailUseCase.class);
    private final PeriodWebMapper webMapper = mock(PeriodWebMapper.class);
    private final MessageSource messageSource = mock(MessageSource.class);

    private final PeriodController controller = new PeriodController(
            createFiscalYearUseCase, updateFiscalYearUseCase, deleteFiscalYearUseCase,
            closePeriodUseCase, reopenPeriodUseCase,
            findFiscalYearsUseCase, getFiscalYearDetailUseCase, webMapper, messageSource
    );

    // ── helpers ──────────────────────────────────────────────────────────────

    private FiscalYear sampleDomain() {
        return FiscalYear.createNew("FY-0001", "Fiscal Year 2026",
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31), true);
    }

    private FiscalYearSummaryResponse sampleSummary() {
        FiscalYearSummaryResponse dto = new FiscalYearSummaryResponse();
        dto.setId(1L);
        dto.setCode("FY-0001");
        dto.setName("Fiscal Year 2026");
        dto.setStartDate(LocalDate.of(2026, 1, 1));
        dto.setEndDate(LocalDate.of(2026, 12, 31));
        dto.setIsActive(true);
        dto.setPeriodCount(12);
        return dto;
    }

    private FiscalYearDetailResponse sampleDetail() {
        FiscalYearDetailResponse dto = new FiscalYearDetailResponse();
        dto.setId(1L);
        dto.setCode("FY-0001");
        dto.setName("Fiscal Year 2026");
        dto.setStartDate(LocalDate.of(2026, 1, 1));
        dto.setEndDate(LocalDate.of(2026, 12, 31));
        dto.setIsActive(true);
        dto.setPeriods(List.of());
        return dto;
    }

    private FiscalYearSaveRequest validRequest() {
        FiscalYearSaveRequest req = new FiscalYearSaveRequest();
        req.setName("Fiscal Year 2026");
        req.setStartDate(LocalDate.of(2026, 1, 1));
        req.setEndDate(LocalDate.of(2026, 12, 31));
        req.setIsActive(true);
        return req;
    }

    // ── list ─────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("list — returns correct view and model attributes")
    void list_returnsCorrectViewAndModel() {
        FiscalYear domain = sampleDomain();
        com.solusi.erp.core.domain.model.Page<FiscalYear> domainPage =
                new com.solusi.erp.core.domain.model.Page<>(List.of(domain), 0, 1, 1L);
        when(findFiscalYearsUseCase.execute(any(), any())).thenReturn(domainPage);
        when(webMapper.toSummaryResponse(any(FiscalYear.class))).thenReturn(sampleSummary());

        org.springframework.data.domain.Pageable springPageable =
                org.springframework.data.domain.PageRequest.of(0, 20);
        Model model = new ExtendedModelMap();

        String view = controller.list(null, springPageable, model);

        assertEquals("accounting/period/list", view);
        Object pageObj = model.getAttribute("page");
        assertThat(pageObj).isInstanceOf(org.springframework.data.domain.Page.class);
        org.springframework.data.domain.Page<?> springPage = (org.springframework.data.domain.Page<?>) pageObj;
        assertEquals(1, springPage.getTotalElements());
        assertThat(springPage.getContent().get(0)).isInstanceOf(FiscalYearSummaryResponse.class);
        assertThat(model.getAttribute("keyword")).isNull();
    }

    // ── showCreateForm ───────────────────────────────────────────────────────

    @Test
    @DisplayName("showCreateForm — returns form view with correct defaults")
    void showCreateForm_returnsFormViewWithDefaults() {
        Model model = new ExtendedModelMap();

        String view = controller.showCreateForm(model);

        assertEquals("accounting/period/form", view);
        FiscalYearSaveRequest req = (FiscalYearSaveRequest) model.getAttribute("fyRequest");
        assertThat(req).isNotNull();
        assertThat(req.getIsActive()).isTrue();
    }

    // ── create ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("create — valid request returns CREATED with data")
    void create_returnsCreatedResponse() {
        FiscalYear domain = sampleDomain();
        FiscalYearDetailResponse detail = sampleDetail();
        when(createFiscalYearUseCase.execute(any(), any(), any(), any()))
                .thenReturn(domain);
        when(webMapper.toDetailResponse(any(FiscalYear.class))).thenReturn(detail);
        when(messageSource.getMessage(eq("msg.success.create"), any(), any())).thenReturn("Created");

        ResponseEntity<ApiResponse<FiscalYearDetailResponse>> response = controller.create(validRequest());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isTrue();
        assertThat(response.getBody().getData().getId()).isEqualTo(1L);
    }

    // ── detail ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("detail — returns detail view with fiscal year")
    void detail_returnsDetailViewWithFiscalYear() {
        FiscalYear domain = sampleDomain();
        FiscalYearDetailResponse detail = sampleDetail();
        when(getFiscalYearDetailUseCase.execute(1L)).thenReturn(Optional.of(domain));
        when(webMapper.toDetailResponse(any(FiscalYear.class))).thenReturn(detail);

        Model model = new ExtendedModelMap();
        String view = controller.detail(1L, model);

        assertEquals("accounting/period/detail", view);
        assertThat(model.getAttribute("fy")).isNotNull();
        assertThat(model.getAttribute("fy")).isInstanceOf(FiscalYearDetailResponse.class);
    }

    @Test
    @DisplayName("detail — throws when not found")
    void detail_throwsWhenNotFound() {
        when(getFiscalYearDetailUseCase.execute(999L)).thenReturn(Optional.empty());
        Model model = new ExtendedModelMap();

        assertThatThrownBy(() -> controller.detail(999L, model))
                .isInstanceOf(RuntimeException.class);
    }

    // ── showEditForm ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("showEditForm — populates model with request and audit info")
    void showEditForm_populatesModel() {
        FiscalYear domain = sampleDomain();
        when(getFiscalYearDetailUseCase.execute(1L)).thenReturn(Optional.of(domain));
        FiscalYearSaveRequest saveReq = validRequest();
        saveReq.setId(1L);
        when(webMapper.toSaveRequest(any(FiscalYear.class))).thenReturn(saveReq);
        when(webMapper.toDetailResponse(any(FiscalYear.class))).thenReturn(sampleDetail());

        Model model = new ExtendedModelMap();
        String view = controller.showEditForm(1L, model);

        assertEquals("accounting/period/form", view);
        assertThat(model.getAttribute("fyRequest")).isNotNull();
        assertThat(model.getAttribute("auditInfo")).isNotNull();
    }

    @Test
    @DisplayName("showEditForm — throws when not found")
    void showEditForm_throwsWhenNotFound() {
        when(getFiscalYearDetailUseCase.execute(999L)).thenReturn(Optional.empty());
        Model model = new ExtendedModelMap();

        assertThatThrownBy(() -> controller.showEditForm(999L, model))
                .isInstanceOf(RuntimeException.class);
    }

    // ── update ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("update — valid request returns OK")
    void update_returnsOkResponse() {
        FiscalYear domain = sampleDomain();
        FiscalYearDetailResponse detail = sampleDetail();
        when(updateFiscalYearUseCase.execute(any(), any(), any()))
                .thenReturn(domain);
        when(webMapper.toDetailResponse(any(FiscalYear.class))).thenReturn(detail);
        when(messageSource.getMessage(eq("msg.success.update"), any(), any())).thenReturn("Updated");

        ResponseEntity<ApiResponse<FiscalYearDetailResponse>> response = controller.update(1L, validRequest());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isTrue();
    }

    // ── delete ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("delete — hard delete returns OK")
    void delete_returnsOkWithHardDelete() {
        when(deleteFiscalYearUseCase.execute(1L)).thenReturn(DeleteResult.HARD_DELETED);
        when(messageSource.getMessage(eq("msg.success.delete"), any(), any())).thenReturn("Deleted");

        ResponseEntity<Void> response = controller.delete(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("delete — soft delete returns OK with warning")
    void delete_returnsOkWithSoftDelete() {
        when(deleteFiscalYearUseCase.execute(1L)).thenReturn(DeleteResult.SOFT_DELETED);
        when(messageSource.getMessage(eq("msg.success.deactivated"), any(), any())).thenReturn("Deactivated");

        ResponseEntity<Void> response = controller.delete(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    // ── closePeriod ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("closePeriod — returns OK with HX-Refresh header")
    void closePeriod_returnsOkWithHxRefresh() {
        when(closePeriodUseCase.execute(10L)).thenReturn(mock(AccountingPeriod.class));
        when(messageSource.getMessage(eq("msg.success.update"), any(), any())).thenReturn("Updated");

        ResponseEntity<Void> response = controller.closePeriod(10L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getHeaders().getFirst("HX-Refresh")).isEqualTo("true");
    }

    // ── reopenPeriod ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("reopenPeriod — returns OK with HX-Refresh header")
    void reopenPeriod_returnsOkWithHxRefresh() {
        when(reopenPeriodUseCase.execute(10L)).thenReturn(mock(AccountingPeriod.class));
        when(messageSource.getMessage(eq("msg.success.update"), any(), any())).thenReturn("Updated");

        ResponseEntity<Void> response = controller.reopenPeriod(10L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getHeaders().getFirst("HX-Refresh")).isEqualTo("true");
    }
}
