package com.solusi.erp.purchasing.purchaseorder.web.controller;

import com.solusi.erp.common.approval.domain.repository.ApprovalRequestRepository;
import com.solusi.erp.core.domain.model.AuditMetadata;
import com.solusi.erp.core.dto.ApiResponse;
import com.solusi.erp.purchasing.purchaseorder.application.usecase.command.*;
import com.solusi.erp.purchasing.purchaseorder.application.usecase.query.*;
import com.solusi.erp.purchasing.purchaseorder.domain.model.*;
import com.solusi.erp.purchasing.purchaseorder.web.dto.*;
import com.solusi.erp.purchasing.purchaseorder.web.mapper.PurchaseOrderWebMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("PurchaseOrderController Tests")
public class PurchaseOrderControllerTest {

    private CreatePurchaseOrderUseCase createUc;
    private UpdatePurchaseOrderUseCase updateUc;
    private DeletePurchaseOrderUseCase deleteUc;
    private SubmitPurchaseOrderUseCase submitUc;
    private SendPurchaseOrderUseCase sendUc;
    private CancelPurchaseOrderUseCase cancelUc;
    private FindPurchaseOrdersUseCase findUc;
    private GetPurchaseOrderEditViewUseCase editViewUc;
    private ApprovalRequestRepository approvalRequestRepository;
    private PurchaseOrderWebMapper webMapper;
    private MessageSource messageSource;
    private PurchaseOrderController controller;

    @BeforeEach
    void setUp() {
        createUc = mock(CreatePurchaseOrderUseCase.class);
        updateUc = mock(UpdatePurchaseOrderUseCase.class);
        deleteUc = mock(DeletePurchaseOrderUseCase.class);
        submitUc = mock(SubmitPurchaseOrderUseCase.class);
        sendUc = mock(SendPurchaseOrderUseCase.class);
        cancelUc = mock(CancelPurchaseOrderUseCase.class);
        findUc = mock(FindPurchaseOrdersUseCase.class);
        editViewUc = mock(GetPurchaseOrderEditViewUseCase.class);
        approvalRequestRepository = mock(ApprovalRequestRepository.class);
        webMapper = mock(PurchaseOrderWebMapper.class);
        messageSource = mock(MessageSource.class);

        controller = new PurchaseOrderController(
            createUc, updateUc, deleteUc, submitUc, sendUc, cancelUc,
            findUc, editViewUc, approvalRequestRepository, webMapper, messageSource
        );
    }

    private PurchaseOrder buildDraftPo() {
        AuditMetadata metadata = new AuditMetadata(1L, 1L, null, null, null, null);
        PurchaseOrderLine line = new PurchaseOrderLine(
            AuditMetadata.empty(), 1L, 10L,
            new BigDecimal("5.0000"), BigDecimal.ZERO, 1L,
            new BigDecimal("100.00"), new BigDecimal("0.10"),
            null, "Test line"
        );
        return new PurchaseOrder(metadata, "PO-001",
            LocalDate.of(2026, 7, 1), LocalDate.of(2026, 8, 1),
            100L, 200L, 1L, BigDecimal.ONE,
            new BigDecimal("500.00"), new BigDecimal("50.00"), new BigDecimal("550.00"),
            PurchaseOrderStatus.DRAFT,
            30, null, PurchaseOrderType.DIRECT, "Test note", true, List.of(line));
    }

    private PurchaseOrder buildSubmittedPo() {
        AuditMetadata metadata = new AuditMetadata(2L, 1L, null, null, null, null);
        return new PurchaseOrder(metadata, "PO-002",
            LocalDate.of(2026, 7, 1), null,
            100L, null, 1L, BigDecimal.ONE,
            BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
            PurchaseOrderStatus.SUBMITTED,
            30, null, PurchaseOrderType.DIRECT, null, true, List.of());
    }

    private PurchaseOrder buildApprovedPo() {
        AuditMetadata metadata = new AuditMetadata(3L, 1L, null, null, null, null);
        return new PurchaseOrder(metadata, "PO-003",
            LocalDate.of(2026, 7, 1), null,
            100L, null, 1L, BigDecimal.ONE,
            BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
            PurchaseOrderStatus.APPROVED,
            30, null, PurchaseOrderType.DIRECT, null, true, List.of());
    }

    @Test
    @DisplayName("list returns list view with page model")
    void listShouldReturnListViewAndModel() {
        PurchaseOrder po = buildDraftPo();
        com.solusi.erp.core.domain.model.Page<PurchaseOrder> domainPage =
            new com.solusi.erp.core.domain.model.Page<>(List.of(po), 0, 20, 1L);
        when(findUc.execute(any(), any())).thenReturn(domainPage);

        PurchaseOrderSummaryResponse summary = new PurchaseOrderSummaryResponse();
        summary.setId(1L);
        summary.setCode("PO-001");
        summary.setStatus(PurchaseOrderStatus.DRAFT);
        summary.setLineCount(1);
        when(webMapper.toSummaryResponse(any(PurchaseOrder.class))).thenReturn(summary);

        org.springframework.data.domain.Pageable springPageable =
            org.springframework.data.domain.PageRequest.of(0, 20);
        Model model = new ExtendedModelMap();

        String view = controller.list(null, springPageable, model);

        assertEquals("purchasing/purchase-orders/list", view);
        Object pageObj = model.getAttribute("page");
        assertThat(pageObj).isInstanceOf(org.springframework.data.domain.Page.class);
        org.springframework.data.domain.Page<?> springPage = (org.springframework.data.domain.Page<?>) pageObj;
        assertEquals(1, springPage.getTotalElements());
        Object first = springPage.getContent().get(0);
        assertThat(first).isInstanceOf(PurchaseOrderSummaryResponse.class);
        assertEquals("PO-001", ((PurchaseOrderSummaryResponse) first).getCode());
    }

    @Test
    @DisplayName("showCreateForm returns form view with defaults")
    void showCreateFormShouldReturnFormViewWithDefaults() {
        Model model = new ExtendedModelMap();
        String view = controller.showCreateForm(model);

        assertEquals("purchasing/purchase-orders/form", view);
        Object req = model.getAttribute("poRequest");
        assertThat(req).isNotNull().isInstanceOf(PurchaseOrderSaveRequest.class);
        PurchaseOrderSaveRequest saveReq = (PurchaseOrderSaveRequest) req;
        assertEquals(LocalDate.now(), saveReq.getOrderDate());
        assertEquals(BigDecimal.ONE, saveReq.getExchangeRate());
        assertEquals(30, saveReq.getPaymentTermDays());
    }

    @Test
    @DisplayName("showEditForm returns form view for DRAFT PO")
    void showEditFormShouldReturnFormForDraft() {
        PurchaseOrder po = buildDraftPo();
        when(editViewUc.execute(1L)).thenReturn(Optional.of(po));

        PurchaseOrderSaveRequest saveReq = new PurchaseOrderSaveRequest();
        saveReq.setId(1L);
        saveReq.setCode("PO-001");
        saveReq.setStatus(PurchaseOrderStatus.DRAFT);
        when(webMapper.toSaveRequest(any(PurchaseOrder.class))).thenReturn(saveReq);

        PurchaseOrderDetailResponse detail = new PurchaseOrderDetailResponse();
        detail.setId(1L);
        when(webMapper.toDetailResponse(any(PurchaseOrder.class))).thenReturn(detail);

        Model model = new ExtendedModelMap();
        String view = controller.showEditForm(1L, model);

        assertEquals("purchasing/purchase-orders/form", view);
        assertThat(model.getAttribute("poRequest")).isNotNull();
        assertThat(model.getAttribute("auditInfo")).isNotNull();
    }

    @Test
    @DisplayName("showEditForm redirects to view for non-DRAFT PO")
    void showEditFormShouldRedirectForNonDraft() {
        PurchaseOrder po = buildSubmittedPo();
        when(editViewUc.execute(2L)).thenReturn(Optional.of(po));

        Model model = new ExtendedModelMap();
        String view = controller.showEditForm(2L, model);

        assertEquals("redirect:/purchasing/purchase-orders/view/2", view);
    }

    @Test
    @DisplayName("view returns view template with PO detail")
    void viewShouldReturnViewTemplate() {
        PurchaseOrder po = buildSubmittedPo();
        when(editViewUc.execute(2L)).thenReturn(Optional.of(po));

        PurchaseOrderDetailResponse detail = new PurchaseOrderDetailResponse();
        detail.setId(2L);
        detail.setCode("PO-002");
        detail.setStatus(PurchaseOrderStatus.SUBMITTED);
        when(webMapper.toDetailResponse(any(PurchaseOrder.class))).thenReturn(detail);

        Model model = new ExtendedModelMap();
        String view = controller.view(2L, model, null);

        assertEquals("purchasing/purchase-orders/view", view);
        Object poObj = model.getAttribute("po");
        assertThat(poObj).isNotNull().isInstanceOf(PurchaseOrderDetailResponse.class);
        assertEquals("PO-002", ((PurchaseOrderDetailResponse) poObj).getCode());
    }

    @Test
    @DisplayName("list with keyword passes keyword to model")
    void listWithKeywordShouldPassToModel() {
        com.solusi.erp.core.domain.model.Page<PurchaseOrder> domainPage =
            new com.solusi.erp.core.domain.model.Page<>(List.of(), 0, 20, 0L);
        when(findUc.execute(any(), any())).thenReturn(domainPage);

        org.springframework.data.domain.Pageable springPageable =
            org.springframework.data.domain.PageRequest.of(0, 20);
        Model model = new ExtendedModelMap();

        String view = controller.list("test", springPageable, model);

        assertEquals("purchasing/purchase-orders/list", view);
        assertEquals("test", model.getAttribute("keyword"));
        org.springframework.data.domain.Page<?> springPage =
            (org.springframework.data.domain.Page<?>) model.getAttribute("page");
        assertThat(springPage).isNotNull();
        assertEquals(0, springPage.getTotalElements());
    }

    @Test
    @DisplayName("create calls use case with correct args")
    void createShouldCallUseCaseWithCorrectArgs() {
        PurchaseOrder po = buildDraftPo();
        when(createUc.execute(any(), any(), any(), any(), any(), any(), anyInt(), any(), any(), any(), anyList())).thenReturn(po);

        PurchaseOrderDetailResponse detail = new PurchaseOrderDetailResponse();
        detail.setId(1L);
        detail.setCode("PO-001");
        when(webMapper.toDetailResponse(any(PurchaseOrder.class))).thenReturn(detail);
        when(webMapper.toLineInputs(anyList())).thenReturn(List.of());
        when(messageSource.getMessage(any(), any(), any())).thenReturn("Created");

        PurchaseOrderSaveRequest request = new PurchaseOrderSaveRequest();
        request.setOrderDate(LocalDate.of(2026, 7, 1));
        request.setSupplierId(100L);
        request.setCurrencyId(1L);
        request.setExchangeRate(BigDecimal.ONE);
        request.setPaymentTermDays(30);

        ResponseEntity<ApiResponse<PurchaseOrderDetailResponse>> response = controller.create(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isTrue();
        verify(createUc).execute(any(), any(), eq(100L), any(), eq(1L), any(), eq(30), any(), any(), any(), anyList());
    }

    @Test
    @DisplayName("update calls use case with correct args")
    void updateShouldCallUseCaseWithCorrectArgs() {
        PurchaseOrder po = buildDraftPo();
        when(updateUc.execute(eq(1L), any(), any(), any(), any(), any(), anyInt(), any(), anyList())).thenReturn(po);

        PurchaseOrderDetailResponse detail = new PurchaseOrderDetailResponse();
        detail.setId(1L);
        when(webMapper.toDetailResponse(any(PurchaseOrder.class))).thenReturn(detail);
        when(webMapper.toLineInputs(anyList())).thenReturn(List.of());
        when(messageSource.getMessage(any(), any(), any())).thenReturn("Updated");

        PurchaseOrderSaveRequest request = new PurchaseOrderSaveRequest();
        request.setOrderDate(LocalDate.of(2026, 7, 1));
        request.setSupplierId(100L);
        request.setCurrencyId(1L);
        request.setExchangeRate(BigDecimal.ONE);
        request.setPaymentTermDays(30);

        ResponseEntity<ApiResponse<PurchaseOrderDetailResponse>> response = controller.update(1L, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(updateUc).execute(eq(1L), any(), any(), any(), any(), any(), eq(30), any(), anyList());
    }

    @Test
    @DisplayName("delete calls use case")
    void deleteShouldCallUseCase() {
        when(messageSource.getMessage(any(), any(), any())).thenReturn("Deleted");

        ResponseEntity<Void> response = controller.delete(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(deleteUc).execute(1L);
    }

    @Test
    @DisplayName("submit calls use case with approverId")
    void submitShouldCallUseCaseWithApproverId() {
        PurchaseOrder po = buildSubmittedPo();
        when(submitUc.execute(1L, 99L)).thenReturn(po);

        PurchaseOrderDetailResponse detail = new PurchaseOrderDetailResponse();
        detail.setId(1L);
        when(webMapper.toDetailResponse(any(PurchaseOrder.class))).thenReturn(detail);
        when(messageSource.getMessage(any(), any(), any())).thenReturn("Submitted");

        ResponseEntity<ApiResponse<PurchaseOrderDetailResponse>> response = controller.submit(1L, 99L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(submitUc).execute(1L, 99L);
    }

    @Test
    @DisplayName("send calls use case")
    void sendShouldCallUseCase() {
        PurchaseOrder po = buildApprovedPo();
        when(sendUc.execute(3L)).thenReturn(po);

        PurchaseOrderDetailResponse detail = new PurchaseOrderDetailResponse();
        detail.setId(3L);
        when(webMapper.toDetailResponse(any(PurchaseOrder.class))).thenReturn(detail);
        when(messageSource.getMessage(any(), any(), any())).thenReturn("Sent");

        ResponseEntity<ApiResponse<PurchaseOrderDetailResponse>> response = controller.send(3L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(sendUc).execute(3L);
    }
}
