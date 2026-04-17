package com.solusi.erp.purchasing.purchaserequisition.web.controller;

import com.solusi.erp.common.approval.application.usecase.query.FindApprovalRequestByReferenceUseCase;
import com.solusi.erp.core.domain.model.AuditMetadata;
import com.solusi.erp.inventory.facility.domain.port.FacilityLookupProvider;
import com.solusi.erp.master.currency.domain.repository.CurrencyRepository;
import com.solusi.erp.master.party.domain.port.PartyLookupProvider;
import com.solusi.erp.purchasing.purchaserequisition.application.usecase.command.*;
import com.solusi.erp.purchasing.purchaserequisition.application.usecase.query.*;
import com.solusi.erp.purchasing.purchaserequisition.domain.model.*;
import com.solusi.erp.purchasing.purchaserequisition.infrastructure.persistence.PurchaseRequisitionJpaRepository;
import com.solusi.erp.purchasing.purchaserequisition.web.dto.*;
import com.solusi.erp.purchasing.purchaserequisition.web.mapper.PurchaseRequisitionWebMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.MessageSource;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("PurchaseRequisitionController Tests")
public class PurchaseRequisitionControllerTest {

    private CreatePurchaseRequisitionUseCase createUc;
    private UpdatePurchaseRequisitionUseCase updateUc;
    private DeletePurchaseRequisitionUseCase deleteUc;
    private SubmitPurchaseRequisitionUseCase submitUc;
    private CancelPurchaseRequisitionUseCase cancelUc;
    private FindPurchaseRequisitionsUseCase findUc;
    private GetPurchaseRequisitionEditViewUseCase editViewUc;
    private FindApprovalRequestByReferenceUseCase findApprovalRequestByReferenceUseCase;
    private PurchaseRequisitionJpaRepository purchaseRequisitionJpaRepository;
    private com.solusi.erp.purchasing.supplierpricelist.domain.repository.SupplierPriceListRepository splRepository;
    private CurrencyRepository currencyRepository;
    private PurchaseRequisitionWebMapper webMapper;
    private MessageSource messageSource;
    private PartyLookupProvider partyLookupProvider;
    private FacilityLookupProvider facilityLookupProvider;
    private PurchaseRequisitionController controller;

    @BeforeEach
    void setUp() {
        createUc = mock(CreatePurchaseRequisitionUseCase.class);
        updateUc = mock(UpdatePurchaseRequisitionUseCase.class);
        deleteUc = mock(DeletePurchaseRequisitionUseCase.class);
        submitUc = mock(SubmitPurchaseRequisitionUseCase.class);
        cancelUc = mock(CancelPurchaseRequisitionUseCase.class);
        findUc = mock(FindPurchaseRequisitionsUseCase.class);
        editViewUc = mock(GetPurchaseRequisitionEditViewUseCase.class);
        findApprovalRequestByReferenceUseCase = mock(FindApprovalRequestByReferenceUseCase.class);
        purchaseRequisitionJpaRepository = mock(PurchaseRequisitionJpaRepository.class);
        splRepository = mock(com.solusi.erp.purchasing.supplierpricelist.domain.repository.SupplierPriceListRepository.class);
        currencyRepository = mock(CurrencyRepository.class);
        webMapper = mock(PurchaseRequisitionWebMapper.class);
        messageSource = mock(MessageSource.class);
        partyLookupProvider = mock(PartyLookupProvider.class);
        facilityLookupProvider = mock(FacilityLookupProvider.class);

        controller = new PurchaseRequisitionController(
            createUc, updateUc, deleteUc, submitUc, cancelUc,
            findUc, editViewUc, findApprovalRequestByReferenceUseCase,
            purchaseRequisitionJpaRepository, splRepository, currencyRepository, webMapper, messageSource,
            partyLookupProvider, facilityLookupProvider
        );
    }

    private PurchaseRequisition buildDraftPr() {
        AuditMetadata metadata = new AuditMetadata(1L, 1L, null, null, null, null);
        PurchaseRequisitionLine line = new PurchaseRequisitionLine(
            AuditMetadata.empty(), 1L, 10L,
            new BigDecimal("5.0000"), 1L,
            LocalDate.of(2026, 8, 1), new BigDecimal("100.00"),
            null, "Test line"
        );
        return new PurchaseRequisition(metadata, "PR-001",
            LocalDate.of(2026, 7, 1), 100L, 200L, "IT",
            PurchaseRequisitionPriority.NORMAL,
            PurchaseRequisitionStatus.DRAFT,
            "Test note", true, null, 1L, List.of(line));
    }

    private PurchaseRequisition buildSubmittedPr() {
        AuditMetadata metadata = new AuditMetadata(2L, 1L, null, null, null, null);
        return new PurchaseRequisition(metadata, "PR-002",
            LocalDate.of(2026, 7, 1), 100L, null, "Finance",
            PurchaseRequisitionPriority.HIGH,
            PurchaseRequisitionStatus.SUBMITTED,
            null, true, null, 1L, List.of());
    }

    @Test
    @DisplayName("list returns list view with page model")
    void listShouldReturnListViewAndModel() {
        PurchaseRequisition pr = buildDraftPr();
        com.solusi.erp.core.domain.model.Page<PurchaseRequisition> domainPage =
            new com.solusi.erp.core.domain.model.Page<>(List.of(pr), 0, 20, 1L);
        when(findUc.execute(any(), any())).thenReturn(domainPage);

        PurchaseRequisitionSummaryResponse summary = new PurchaseRequisitionSummaryResponse();
        summary.setId(1L);
        summary.setCode("PR-001");
        summary.setStatus(PurchaseRequisitionStatus.DRAFT);
        summary.setPriority(PurchaseRequisitionPriority.NORMAL);
        when(webMapper.toSummaryResponse(any(PurchaseRequisition.class))).thenReturn(summary);

        org.springframework.data.domain.Pageable springPageable =
            org.springframework.data.domain.PageRequest.of(0, 20);
        Model model = new ExtendedModelMap();

        String view = controller.list(null, springPageable, model);

        assertEquals("purchasing/purchase-requisitions/list", view);
        Object pageObj = model.getAttribute("page");
        assertThat(pageObj).isInstanceOf(org.springframework.data.domain.Page.class);
        org.springframework.data.domain.Page<?> springPage = (org.springframework.data.domain.Page<?>) pageObj;
        assertEquals(1, springPage.getTotalElements());
        Object first = springPage.getContent().get(0);
        assertThat(first).isInstanceOf(PurchaseRequisitionSummaryResponse.class);
        assertEquals("PR-001", ((PurchaseRequisitionSummaryResponse) first).getCode());
    }

    @Test
    @DisplayName("showCreateForm returns form view with defaults")
    void showCreateFormShouldReturnFormViewWithDefaults() {
        Model model = new ExtendedModelMap();
        String view = controller.showCreateForm(model);

        assertEquals("purchasing/purchase-requisitions/form", view);
        Object req = model.getAttribute("prRequest");
        assertThat(req).isNotNull().isInstanceOf(PurchaseRequisitionSaveRequest.class);
        PurchaseRequisitionSaveRequest saveReq = (PurchaseRequisitionSaveRequest) req;
        assertEquals(LocalDate.now(), saveReq.getRequestDate());
        assertEquals(PurchaseRequisitionPriority.NORMAL, saveReq.getPriority());
    }

    @Test
    @DisplayName("showEditForm returns form view for DRAFT PR")
    void showEditFormShouldReturnFormForDraft() {
        PurchaseRequisition pr = buildDraftPr();
        when(editViewUc.execute(1L)).thenReturn(Optional.of(pr));

        PurchaseRequisitionSaveRequest saveReq = new PurchaseRequisitionSaveRequest();
        saveReq.setId(1L);
        saveReq.setCode("PR-001");
        saveReq.setStatus(PurchaseRequisitionStatus.DRAFT);
        when(webMapper.toSaveRequest(any(PurchaseRequisition.class))).thenReturn(saveReq);

        PurchaseRequisitionDetailResponse detail = new PurchaseRequisitionDetailResponse();
        detail.setId(1L);
        when(webMapper.toDetailResponse(any(PurchaseRequisition.class))).thenReturn(detail);

        Model model = new ExtendedModelMap();
        String view = controller.showEditForm(1L, model);

        assertEquals("purchasing/purchase-requisitions/form", view);
        assertThat(model.getAttribute("prRequest")).isNotNull();
        assertThat(model.getAttribute("auditInfo")).isNotNull();
        // Assert prUI is present with Trinity Data for header autocompletes
        assertThat(model.getAttribute("prUI")).isNotNull();
    }

    @Test
    @DisplayName("showEditForm redirects to view for non-DRAFT PR")
    void showEditFormShouldRedirectForNonDraft() {
        PurchaseRequisition pr = buildSubmittedPr();
        when(editViewUc.execute(2L)).thenReturn(Optional.of(pr));

        Model model = new ExtendedModelMap();
        String view = controller.showEditForm(2L, model);

        assertEquals("redirect:/purchasing/purchase-requisitions/view/2", view);
    }

    @Test
    @DisplayName("view returns view template with PR detail")
    void viewShouldReturnViewTemplate() {
        PurchaseRequisition pr = buildSubmittedPr();
        when(editViewUc.execute(2L)).thenReturn(Optional.of(pr));

        PurchaseRequisitionDetailResponse detail = new PurchaseRequisitionDetailResponse();
        detail.setId(2L);
        detail.setCode("PR-002");
        detail.setStatus(PurchaseRequisitionStatus.SUBMITTED);
        when(webMapper.toDetailResponse(any(PurchaseRequisition.class))).thenReturn(detail);

        Model model = new ExtendedModelMap();
        String view = controller.view(2L, model, null);

        assertEquals("purchasing/purchase-requisitions/view", view);
        Object prObj = model.getAttribute("pr");
        assertThat(prObj).isNotNull().isInstanceOf(PurchaseRequisitionDetailResponse.class);
        assertEquals("PR-002", ((PurchaseRequisitionDetailResponse) prObj).getCode());
    }

    @Test
    @DisplayName("list with keyword passes keyword to model")
    void listWithKeywordShouldPassToModel() {
        com.solusi.erp.core.domain.model.Page<PurchaseRequisition> domainPage =
            new com.solusi.erp.core.domain.model.Page<>(List.of(), 0, 20, 0L);
        when(findUc.execute(any(), any())).thenReturn(domainPage);

        org.springframework.data.domain.Pageable springPageable =
            org.springframework.data.domain.PageRequest.of(0, 20);
        Model model = new ExtendedModelMap();

        String view = controller.list("test", springPageable, model);

        assertEquals("purchasing/purchase-requisitions/list", view);
        assertEquals("test", model.getAttribute("keyword"));
        org.springframework.data.domain.Page<?> springPage =
            (org.springframework.data.domain.Page<?>) model.getAttribute("page");
        assertThat(springPage).isNotNull();
        assertEquals(0, springPage.getTotalElements());
    }
}
