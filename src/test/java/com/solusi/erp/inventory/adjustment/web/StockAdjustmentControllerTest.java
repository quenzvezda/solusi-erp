package com.solusi.erp.inventory.adjustment.web;

import com.solusi.erp.core.domain.model.AuditMetadata;
import com.solusi.erp.inventory.adjustment.application.usecase.command.*;
import com.solusi.erp.inventory.adjustment.application.usecase.query.*;
import com.solusi.erp.inventory.adjustment.domain.model.AdjustmentStatus;
import com.solusi.erp.inventory.adjustment.domain.model.StockAdjustment;
import com.solusi.erp.inventory.adjustment.web.controller.StockAdjustmentController;
import com.solusi.erp.inventory.adjustment.web.dto.*;
import com.solusi.erp.inventory.adjustment.web.mapper.StockAdjustmentWebMapper;
import com.solusi.erp.master.dto.CurrencyResponse;
import com.solusi.erp.master.service.CurrencyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.MessageSource;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("StockAdjustmentController Tests")
class StockAdjustmentControllerTest {

    private CreateStockAdjustmentUseCase createUseCase;
    private UpdateStockAdjustmentUseCase updateUseCase;
    private DeleteStockAdjustmentUseCase deleteUseCase;
    private ProcessStockAdjustmentUseCase processUseCase;
    private FindStockAdjustmentsUseCase findUseCase;
    private GetStockAdjustmentUseCase getUseCase;
    private GetStockAdjustmentEditViewUseCase getEditViewUseCase;
    private StockAdjustmentWebMapper webMapper;
    private CurrencyService currencyService;
    private MessageSource messageSource;

    private StockAdjustmentController controller;

    @BeforeEach
    void setUp() {
        createUseCase = mock(CreateStockAdjustmentUseCase.class);
        updateUseCase = mock(UpdateStockAdjustmentUseCase.class);
        deleteUseCase = mock(DeleteStockAdjustmentUseCase.class);
        processUseCase = mock(ProcessStockAdjustmentUseCase.class);
        findUseCase = mock(FindStockAdjustmentsUseCase.class);
        getUseCase = mock(GetStockAdjustmentUseCase.class);
        getEditViewUseCase = mock(GetStockAdjustmentEditViewUseCase.class);
        webMapper = mock(StockAdjustmentWebMapper.class);
        currencyService = mock(CurrencyService.class);
        messageSource = mock(MessageSource.class);

        controller = new StockAdjustmentController(createUseCase, updateUseCase, deleteUseCase,
                processUseCase, findUseCase, getUseCase, getEditViewUseCase,
                webMapper, currencyService, messageSource);
    }

    private StockAdjustment draftDomain() {
        return new StockAdjustment(
                new AuditMetadata(1L, 1L, null, null, null, null),
                "ADJ-001", LocalDate.now(), AdjustmentStatus.DRAFT, null,
                1L, "WH", 1L, "IDR", BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ZERO, List.of());
    }

    private StockAdjustment completedDomain() {
        return new StockAdjustment(
                new AuditMetadata(1L, 1L, null, null, null, null),
                "ADJ-001", LocalDate.now(), AdjustmentStatus.COMPLETED, null,
                1L, "WH", 1L, "IDR", BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ZERO, List.of());
    }

    @Test
    @DisplayName("list returns list view with page")
    void list_returnsListView() {
        com.solusi.erp.core.domain.model.Page<StockAdjustment> domainPage =
                new com.solusi.erp.core.domain.model.Page<>(List.of(), 0, 10, 0L);
        when(findUseCase.execute(any(), any())).thenReturn(domainPage);

        Model model = new ExtendedModelMap();
        org.springframework.data.domain.Pageable springPageable =
                org.springframework.data.domain.PageRequest.of(0, 10);
        String view = controller.list(null, springPageable, model);

        assertThat(view).isEqualTo("inventory/adjustments/list");
        assertThat(model.getAttribute("page")).isNotNull();
    }

    @Test
    @DisplayName("createForm returns form view with stockAdjustment in model")
    void createForm_returnsFormWithModel() {
        when(currencyService.getDefaultCurrency()).thenReturn(null);
        when(currencyService.findAllActive()).thenReturn(List.of());

        Model model = new ExtendedModelMap();
        String view = controller.createForm(model);

        assertThat(view).isEqualTo("inventory/adjustments/form");
        assertThat(model.getAttribute("stockAdjustment")).isInstanceOf(StockAdjustmentSaveRequest.class);
        assertThat(model.getAttribute("currencies")).isNotNull();
    }

    @Test
    @DisplayName("editForm DRAFT returns form view")
    void editForm_draft_returnsFormView() {
        when(getEditViewUseCase.execute(1L)).thenReturn(Optional.of(draftDomain()));
        when(webMapper.toSaveRequest(any())).thenReturn(new StockAdjustmentSaveRequest());
        when(currencyService.findAllActive()).thenReturn(List.of());
        when(currencyService.getDefaultCurrency()).thenReturn(null);

        Model model = new ExtendedModelMap();
        String view = controller.editForm(1L, model);

        assertThat(view).isEqualTo("inventory/adjustments/form");
    }

    @Test
    @DisplayName("editForm COMPLETED redirects to view")
    void editForm_completed_redirectsToView() {
        when(getEditViewUseCase.execute(1L)).thenReturn(Optional.of(completedDomain()));

        Model model = new ExtendedModelMap();
        String view = controller.editForm(1L, model);

        assertThat(view).isEqualTo("redirect:/inventory/adjustments/view/1");
    }

    @Test
    @DisplayName("view returns view page with stockAdjustment in model")
    void view_returnsViewPage() {
        when(getUseCase.execute(1L)).thenReturn(Optional.of(draftDomain()));
        StockAdjustmentDetailResponse detail = new StockAdjustmentDetailResponse();
        when(webMapper.toDetailResponse(any())).thenReturn(detail);

        Model model = new ExtendedModelMap();
        String view = controller.view(1L, model);

        assertThat(view).isEqualTo("inventory/adjustments/view");
        assertThat(model.getAttribute("stockAdjustment")).isSameAs(detail);
    }

    @Test
    @DisplayName("process calls processUseCase and redirects to view")
    void process_redirectsToView() {
        doNothing().when(processUseCase).execute(1L);
        when(messageSource.getMessage(anyString(), any(), any())).thenReturn("Success");

        RedirectAttributesModelMap ra = new RedirectAttributesModelMap();
        String view = controller.process(1L, ra);

        assertThat(view).isEqualTo("redirect:/inventory/adjustments/view/1");
        verify(processUseCase).execute(1L);
    }

    @Test
    @DisplayName("delete calls deleteUseCase and returns OK")
    void delete_callsDeleteAndReturnsOk() {
        doNothing().when(deleteUseCase).execute(1L);
        when(messageSource.getMessage(anyString(), any(), any())).thenReturn("Deleted");

        ResponseEntity<Void> response = controller.delete(1L);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        verify(deleteUseCase).execute(1L);
    }
}
