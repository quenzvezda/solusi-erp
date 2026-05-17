package com.solusi.erp.purchasing.purchaseorder.web.controller;

import com.solusi.erp.common.approval.application.usecase.query.FindApprovalRequestByReferenceUseCase;
import com.solusi.erp.core.domain.model.AuditMetadata;
import com.solusi.erp.core.dto.ApiResponse;
import com.solusi.erp.core.dto.LookupDto;
import com.solusi.erp.inventory.facility.domain.port.FacilityLookupProvider;
import com.solusi.erp.inventory.goodsreceipt.application.usecase.query.CountGoodsReceiptsByPoUseCase;
import com.solusi.erp.inventory.product.domain.port.ProductLookupProvider;
import com.solusi.erp.inventory.uom.domain.port.UomLookupProvider;
import com.solusi.erp.master.currency.domain.port.CurrencyLookupProvider;
import com.solusi.erp.master.party.domain.port.PartyLookupProvider;
import com.solusi.erp.purchasing.purchaseorder.application.usecase.command.*;
import com.solusi.erp.purchasing.purchaseorder.application.usecase.query.*;
import com.solusi.erp.purchasing.purchaseorder.domain.model.*;
import com.solusi.erp.purchasing.purchaserequisition.domain.model.PurchaseRequisition;
import com.solusi.erp.purchasing.purchaserequisition.domain.model.PurchaseRequisitionPriority;
import com.solusi.erp.purchasing.purchaserequisition.domain.model.PurchaseRequisitionStatus;
import com.solusi.erp.purchasing.purchaserequisition.domain.repository.PurchaseRequisitionRepository;
import com.solusi.erp.purchasing.purchaseorder.web.dto.*;
import com.solusi.erp.purchasing.purchaseorder.web.mapper.PurchaseOrderWebMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
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
    private FindPurchaseOrderPrSelectorUseCase findPrSelectorUc;
    private FindPurchaseOrderPrLineSelectorUseCase findPrLineSelectorUc;
    private FindApprovalRequestByReferenceUseCase findApprovalRequestByReferenceUseCase;
    private CountGoodsReceiptsByPoUseCase countGoodsReceiptsByPoUseCase;
    private PurchaseOrderWebMapper webMapper;
    private MessageSource messageSource;
    private PartyLookupProvider partyLookupProvider;
    private FacilityLookupProvider facilityLookupProvider;
    private CurrencyLookupProvider currencyLookupProvider;
    private PurchaseRequisitionRepository purchaseRequisitionRepository;
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
        findPrSelectorUc = mock(FindPurchaseOrderPrSelectorUseCase.class);
        findPrLineSelectorUc = mock(FindPurchaseOrderPrLineSelectorUseCase.class);
        findApprovalRequestByReferenceUseCase = mock(FindApprovalRequestByReferenceUseCase.class);
        countGoodsReceiptsByPoUseCase = mock(CountGoodsReceiptsByPoUseCase.class);
        webMapper = mock(PurchaseOrderWebMapper.class);
        messageSource = mock(MessageSource.class);
        partyLookupProvider = mock(PartyLookupProvider.class);
        facilityLookupProvider = mock(FacilityLookupProvider.class);
        currencyLookupProvider = mock(CurrencyLookupProvider.class);
        purchaseRequisitionRepository = mock(PurchaseRequisitionRepository.class);

        controller = new PurchaseOrderController(
            createUc, updateUc, deleteUc, submitUc, sendUc, cancelUc,
            findUc, editViewUc, findPrSelectorUc, findPrLineSelectorUc,
            findApprovalRequestByReferenceUseCase, countGoodsReceiptsByPoUseCase, webMapper, messageSource,
            partyLookupProvider, facilityLookupProvider, currencyLookupProvider, purchaseRequisitionRepository
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

    private PurchaseOrder buildDraftStandardPo() {
        AuditMetadata metadata = new AuditMetadata(4L, 1L, null, null, null, null);
        PurchaseOrderLine line = new PurchaseOrderLine(
            AuditMetadata.empty(), 2L, 20L,
            new BigDecimal("2.0000"), BigDecimal.ZERO, 1L,
            new BigDecimal("750.00"), BigDecimal.ZERO,
            501L, "Derived from PR"
        );
        return new PurchaseOrder(metadata, "PO-004",
            LocalDate.of(2026, 7, 2), LocalDate.of(2026, 7, 8),
            100L, 200L, 1L, BigDecimal.ONE,
            new BigDecimal("1500.00"), BigDecimal.ZERO, new BigDecimal("1500.00"),
            PurchaseOrderStatus.DRAFT,
            30, 10L, PurchaseOrderType.STANDARD, "Standard note", true, List.of(line));
    }

    private PurchaseOrder buildSentPo() {
        AuditMetadata metadata = new AuditMetadata(5L, 1L, null, null, null, null);
        return new PurchaseOrder(metadata, "PO-005",
            LocalDate.of(2026, 7, 1), null,
            100L, null, 1L, BigDecimal.ONE,
            BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
            PurchaseOrderStatus.SENT,
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
    @DisplayName("showPrSelector returns PR selector fragment with page model")
    void showPrSelector_returnsFragmentWithPageModel() {
        org.springframework.data.domain.Page<PurchaseOrderPrSelectorRow> selectorPage = new PageImpl<>(List.of(
                new PurchaseOrderPrSelectorRow(
                        10L, "PR-001", LocalDate.of(2026, 7, 1),
                        100L, "Alpha Supplier", "SUP-001",
                        200L, "Main Warehouse", "WH-01",
                        1L, "US Dollar", "USD",
                        2L
                )
        ));
        when(findPrSelectorUc.execute(eq("alpha"), eq(100L), any(org.springframework.data.domain.Pageable.class)))
                .thenReturn(selectorPage);

        Model model = new ExtendedModelMap();
        String view = controller.showPrSelector("alpha", 100L, PageRequest.of(0, 10), model);

        assertEquals("purchasing/purchase-orders/fragments/pr-selector-modal", view);
        assertThat(model.getAttribute("page")).isSameAs(selectorPage);
        assertEquals("alpha", model.getAttribute("keyword"));
        assertEquals(100L, model.getAttribute("supplierId"));
    }

    @Test
    @DisplayName("showPrLineSelector returns PR line selector fragment with context model")
    void showPrLineSelector_returnsFragmentWithContextModel() {
        org.springframework.data.domain.Page<PurchaseOrderPrLineSelectorRow> selectorPage = new PageImpl<>(List.of(
                new PurchaseOrderPrLineSelectorRow(
                        100L, 10L, "PR-001",
                        11L, "Bearing 6204", "BRG-6204",
                        new BigDecimal("10.0000"), new BigDecimal("6.0000"),
                        1L, "PCS", "PCS",
                        new BigDecimal("100.00"), LocalDate.of(2026, 7, 10),
                        "Line note"
                )
        ));
        when(findPrLineSelectorUc.execute(eq(10L), eq("bearing"), eq(List.of(100L)), any(org.springframework.data.domain.Pageable.class)))
                .thenReturn(selectorPage);

        Model model = new ExtendedModelMap();
        String view = controller.showPrLineSelector(10L, "bearing", List.of(100L), PageRequest.of(0, 10), model);

        assertEquals("purchasing/purchase-orders/fragments/pr-line-selector-modal", view);
        assertThat(model.getAttribute("page")).isSameAs(selectorPage);
        assertEquals(10L, model.getAttribute("prId"));
        assertEquals("bearing", model.getAttribute("keyword"));
        assertEquals(List.of(100L), model.getAttribute("excludePrLineIds"));
    }

    @Test
    @DisplayName("showPrLineSelector allows create and update authorities")
    void showPrLineSelector_allowsCreateAndUpdateAuthorities() throws Exception {
        Method method = PurchaseOrderController.class.getMethod(
            "showPrLineSelector",
            Long.class,
            String.class,
            List.class,
            org.springframework.data.domain.Pageable.class,
            Model.class
        );

        PreAuthorize annotation = method.getAnnotation(PreAuthorize.class);

        assertThat(annotation).isNotNull();
        assertThat(annotation.value()).isEqualTo("hasAnyAuthority('PO_CREATE', 'PO_UPDATE')");
    }

    @Test
    @DisplayName("showEditForm returns form view for DRAFT PO")
    void showEditFormShouldReturnFormForDraft() {
        PurchaseOrder po = buildDraftStandardPo();
        when(editViewUc.execute(1L)).thenReturn(Optional.of(po));

        PurchaseOrderSaveRequest saveReq = new PurchaseOrderSaveRequest();
        saveReq.setId(1L);
        saveReq.setCode("PO-001");
        saveReq.setStatus(PurchaseOrderStatus.DRAFT);
        PurchaseOrderLineRequest lineRequest = new PurchaseOrderLineRequest();
        lineRequest.setPrLineId(501L);
        saveReq.setLines(List.of(lineRequest));
        when(webMapper.toSaveRequest(any(PurchaseOrder.class))).thenReturn(saveReq);

        PurchaseOrderDetailResponse detail = new PurchaseOrderDetailResponse();
        detail.setId(1L);
        when(webMapper.toDetailResponse(any(PurchaseOrder.class))).thenReturn(detail);

        // Mock LookupProviders for buildPOUI
        when(partyLookupProvider.resolve(100L)).thenReturn(
            new LookupDto(100L, "ABC Supplier", "Supplier Code: ABC", null));
        when(facilityLookupProvider.resolve(200L)).thenReturn(
            new LookupDto(200L, "Main Warehouse", "Code: WH01", null));
        when(currencyLookupProvider.resolve(1L)).thenReturn(
            new LookupDto(1L, "US Dollar", "USD", null));
        when(purchaseRequisitionRepository.findById(10L)).thenReturn(Optional.of(
            new PurchaseRequisition(
                new AuditMetadata(10L, 1L, null, null, null, null),
                "PR-2604-00002",
                LocalDate.of(2026, 4, 12),
                300L,
                200L,
                "IT",
                PurchaseRequisitionPriority.NORMAL,
                PurchaseRequisitionStatus.APPROVED,
                null,
                true,
                100L,
                1L,
                List.of()
            )
        ));
        when(findPrLineSelectorUc.execute(eq(10L), isNull(), isNull(), any(org.springframework.data.domain.Pageable.class)))
            .thenReturn(new PageImpl<>(List.of(
                new PurchaseOrderPrLineSelectorRow(
                    501L, 10L, "PR-2604-00002",
                    20L, "Laptop 14 inch Core i5", "LP-14-I5",
                    new BigDecimal("5.0000"), new BigDecimal("2.0000"),
                    1L, "PCS", "PCS",
                    new BigDecimal("7400000.00"), LocalDate.of(2026, 5, 1),
                    "Derived from PR"
                )
            )));

        Model model = new ExtendedModelMap();
        String view = controller.showEditForm(1L, model);

        assertEquals("purchasing/purchase-orders/form", view);
        assertThat(model.getAttribute("poRequest")).isNotNull();
        assertThat(model.getAttribute("auditInfo")).isNotNull();
        assertThat(model.getAttribute("poUI")).isNotNull()
            .as("poUI should be present for edit form with Trinity Data");
        
        @SuppressWarnings("unchecked")
        Map<String, Object> poUI = (Map<String, Object>) model.getAttribute("poUI");
        assertThat(poUI).containsKeys("supplierText", "supplierSubtext", "currencyText", "currencySubtext", "prDisplay");
        assertThat(poUI.get("prDisplay")).isEqualTo("PR-2604-00002");
        PurchaseOrderSaveRequest poRequest = (PurchaseOrderSaveRequest) model.getAttribute("poRequest");
        assertThat(poRequest.getLines()).singleElement().satisfies(line ->
            assertThat(line.getMaxQuantity()).isEqualByComparingTo("2.0000")
        );
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
        request.setTaxId(10L);
        request.setTaxCode("PPN-IN");
        request.setTaxName("PPN 11% Inclusive");
        request.setTaxRate(new BigDecimal("11.00"));
        request.setTaxCalculationMode(com.solusi.erp.master.tax.domain.model.TaxCalculationMode.INCLUSIVE);

        ResponseEntity<ApiResponse<PurchaseOrderDetailResponse>> response = controller.create(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isTrue();
        verify(createUc).execute(any(), any(), eq(100L), any(), eq(1L), any(), eq(30), any(), any(),
                eq(10L), eq("PPN-IN"), eq("PPN 11% Inclusive"), eq(new BigDecimal("11.00")),
                eq(com.solusi.erp.master.tax.domain.model.TaxCalculationMode.INCLUSIVE),
                any(), anyList());
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
        request.setTaxId(11L);
        request.setTaxCode("PPN-EX");
        request.setTaxName("PPN 11% Exclusive");
        request.setTaxRate(new BigDecimal("11.00"));
        request.setTaxCalculationMode(com.solusi.erp.master.tax.domain.model.TaxCalculationMode.EXCLUSIVE);

        ResponseEntity<ApiResponse<PurchaseOrderDetailResponse>> response = controller.update(1L, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(updateUc).execute(eq(1L), any(), any(), any(), any(), any(), eq(30),
                eq(11L), eq("PPN-EX"), eq("PPN 11% Exclusive"), eq(new BigDecimal("11.00")),
                eq(com.solusi.erp.master.tax.domain.model.TaxCalculationMode.EXCLUSIVE),
                any(), anyList());
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

    @Test
    @DisplayName("view populates goods receipt count when receipts exist")
    void view_populatesGoodsReceiptCountWhenReceiptsExist() {
        PurchaseOrder po = buildSentPo();
        when(editViewUc.execute(1L)).thenReturn(Optional.of(po));
        when(countGoodsReceiptsByPoUseCase.execute(1L)).thenReturn(3L);

        PurchaseOrderDetailResponse detail = new PurchaseOrderDetailResponse();
        detail.setId(1L);
        detail.setCode("PO-005");
        detail.setStatus(PurchaseOrderStatus.SENT);
        when(webMapper.toDetailResponse(any(PurchaseOrder.class))).thenReturn(detail);

        Model model = new ExtendedModelMap();
        String view = controller.view(1L, model, null);

        assertEquals("purchasing/purchase-orders/view", view);
        assertThat(model.getAttribute("goodsReceiptCount")).isEqualTo(3L);
        assertThat(model.getAttribute("canCreateGoodsReceipt")).isEqualTo(true);
    }

    @Test
    @DisplayName("view hides goods receipt count when no receipts")
    void view_hidesGoodsReceiptCountWhenNoReceipts() {
        PurchaseOrder po = buildSentPo();
        when(editViewUc.execute(1L)).thenReturn(Optional.of(po));
        when(countGoodsReceiptsByPoUseCase.execute(1L)).thenReturn(0L);

        PurchaseOrderDetailResponse detail = new PurchaseOrderDetailResponse();
        detail.setId(1L);
        detail.setCode("PO-005");
        detail.setStatus(PurchaseOrderStatus.SENT);
        when(webMapper.toDetailResponse(any(PurchaseOrder.class))).thenReturn(detail);

        Model model = new ExtendedModelMap();
        String view = controller.view(1L, model, null);

        assertEquals("purchasing/purchase-orders/view", view);
        assertThat(model.getAttribute("goodsReceiptCount")).isEqualTo(0L);
        assertThat(model.getAttribute("canCreateGoodsReceipt")).isEqualTo(true);
    }

    @Test
    @DisplayName("view exposes send action when PO is approved")
    void view_exposesSendActionWhenPoApproved() {
        PurchaseOrder po = buildApprovedPo();
        when(editViewUc.execute(3L)).thenReturn(Optional.of(po));
        when(countGoodsReceiptsByPoUseCase.execute(3L)).thenReturn(0L);

        PurchaseOrderDetailResponse detail = new PurchaseOrderDetailResponse();
        detail.setId(3L);
        detail.setCode("PO-003");
        detail.setStatus(PurchaseOrderStatus.APPROVED);
        when(webMapper.toDetailResponse(any(PurchaseOrder.class))).thenReturn(detail);

        Model model = new ExtendedModelMap();
        String view = controller.view(3L, model, null);

        assertEquals("purchasing/purchase-orders/view", view);
        assertThat(model.getAttribute("canSendPurchaseOrder")).isEqualTo(true);
        assertThat(model.getAttribute("canCreateGoodsReceipt")).isEqualTo(false);
    }
}
