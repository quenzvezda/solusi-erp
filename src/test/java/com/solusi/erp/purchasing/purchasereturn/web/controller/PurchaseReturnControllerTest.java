package com.solusi.erp.purchasing.purchasereturn.web.controller;

import com.solusi.erp.common.approval.application.usecase.query.FindApprovalRequestByReferenceUseCase;
import com.solusi.erp.accountspayable.debitmemo.application.usecase.query.FindDebitMemoByPurchaseReturnUseCase;
import com.solusi.erp.common.approval.domain.model.ApprovalRequest;
import com.solusi.erp.common.approval.domain.model.ApprovalStatus;
import com.solusi.erp.core.domain.model.AuditMetadata;
import com.solusi.erp.core.domain.model.Page;
import com.solusi.erp.core.dto.LookupDto;
import com.solusi.erp.master.party.domain.port.PartyLookupProvider;
import com.solusi.erp.purchasing.purchasereturn.application.usecase.command.CancelApprovedPurchaseReturnUseCase;
import com.solusi.erp.purchasing.purchasereturn.application.usecase.command.CancelPurchaseReturnSubmissionUseCase;
import com.solusi.erp.purchasing.purchasereturn.application.usecase.command.ConfirmPurchaseReturnUseCase;
import com.solusi.erp.purchasing.purchasereturn.application.usecase.command.CreatePurchaseReturnUseCase;
import com.solusi.erp.purchasing.purchasereturn.application.usecase.command.SubmitPurchaseReturnUseCase;
import com.solusi.erp.purchasing.purchasereturn.application.usecase.command.UpdatePurchaseReturnUseCase;
import com.solusi.erp.purchasing.purchasereturn.application.usecase.query.FindEligiblePurchaseReturnGoodsReceiptsUseCase;
import com.solusi.erp.purchasing.purchasereturn.application.usecase.query.FindPurchaseReturnGrLineSlicesUseCase;
import com.solusi.erp.purchasing.purchasereturn.application.usecase.query.FindPurchaseReturnSerialsUseCase;
import com.solusi.erp.purchasing.purchasereturn.application.usecase.query.FindPurchaseReturnsUseCase;
import com.solusi.erp.purchasing.purchasereturn.application.usecase.query.GetEligiblePurchaseReturnPurchaseOrderLookupUseCase;
import com.solusi.erp.purchasing.purchasereturn.application.usecase.query.GetPurchaseReturnCreateViewUseCase;
import com.solusi.erp.purchasing.purchasereturn.application.usecase.query.GetPurchaseReturnEditViewUseCase;
import com.solusi.erp.purchasing.purchasereturn.application.usecase.query.GetPurchaseReturnUseCase;
import com.solusi.erp.purchasing.purchasereturn.domain.model.PurchaseReturn;
import com.solusi.erp.purchasing.purchasereturn.domain.model.PurchaseReturnStatus;
import com.solusi.erp.purchasing.purchasereturn.web.dto.PurchaseReturnDetailResponse;
import com.solusi.erp.purchasing.purchasereturn.web.dto.PurchaseReturnSaveRequest;
import com.solusi.erp.purchasing.purchasereturn.web.dto.PurchaseReturnSummaryResponse;
import com.solusi.erp.purchasing.purchasereturn.web.mapper.PurchaseReturnWebMapper;
import com.solusi.erp.security.shared.model.SecurityUser;
import com.solusi.erp.security.user.infrastructure.persistence.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import java.lang.reflect.Method;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PurchaseReturnControllerTest {

    private CreatePurchaseReturnUseCase createUseCase;
    private UpdatePurchaseReturnUseCase updateUseCase;
    private SubmitPurchaseReturnUseCase submitUseCase;
    private CancelPurchaseReturnSubmissionUseCase cancelSubmissionUseCase;
    private ConfirmPurchaseReturnUseCase confirmUseCase;
    private CancelApprovedPurchaseReturnUseCase cancelApprovedUseCase;
    private FindPurchaseReturnsUseCase findUseCase;
    private GetPurchaseReturnUseCase getUseCase;
    private GetPurchaseReturnCreateViewUseCase getCreateViewUseCase;
    private GetPurchaseReturnEditViewUseCase getEditViewUseCase;
    private FindEligiblePurchaseReturnGoodsReceiptsUseCase findEligibleUseCase;
    private FindPurchaseReturnGrLineSlicesUseCase findSlicesUseCase;
    private FindPurchaseReturnSerialsUseCase findSerialsUseCase;
    private GetEligiblePurchaseReturnPurchaseOrderLookupUseCase poLookupUseCase;
    private FindDebitMemoByPurchaseReturnUseCase findDebitMemoByPurchaseReturnUseCase;
    private FindApprovalRequestByReferenceUseCase findApprovalUseCase;
    private PartyLookupProvider partyLookupProvider;
    private PurchaseReturnWebMapper webMapper;
    private MessageSource messageSource;
    private PurchaseReturnController controller;

    @BeforeEach
    void setUp() {
        createUseCase = mock(CreatePurchaseReturnUseCase.class);
        updateUseCase = mock(UpdatePurchaseReturnUseCase.class);
        submitUseCase = mock(SubmitPurchaseReturnUseCase.class);
        cancelSubmissionUseCase = mock(CancelPurchaseReturnSubmissionUseCase.class);
        confirmUseCase = mock(ConfirmPurchaseReturnUseCase.class);
        cancelApprovedUseCase = mock(CancelApprovedPurchaseReturnUseCase.class);
        findUseCase = mock(FindPurchaseReturnsUseCase.class);
        getUseCase = mock(GetPurchaseReturnUseCase.class);
        getCreateViewUseCase = mock(GetPurchaseReturnCreateViewUseCase.class);
        getEditViewUseCase = mock(GetPurchaseReturnEditViewUseCase.class);
        findEligibleUseCase = mock(FindEligiblePurchaseReturnGoodsReceiptsUseCase.class);
        findSlicesUseCase = mock(FindPurchaseReturnGrLineSlicesUseCase.class);
        findSerialsUseCase = mock(FindPurchaseReturnSerialsUseCase.class);
        poLookupUseCase = mock(GetEligiblePurchaseReturnPurchaseOrderLookupUseCase.class);
        findDebitMemoByPurchaseReturnUseCase = mock(FindDebitMemoByPurchaseReturnUseCase.class);
        findApprovalUseCase = mock(FindApprovalRequestByReferenceUseCase.class);
        partyLookupProvider = mock(PartyLookupProvider.class);
        webMapper = mock(PurchaseReturnWebMapper.class);
        messageSource = mock(MessageSource.class);
        controller = new PurchaseReturnController(
                createUseCase, updateUseCase, submitUseCase, cancelSubmissionUseCase, confirmUseCase,
                cancelApprovedUseCase, findUseCase, getUseCase, getCreateViewUseCase, getEditViewUseCase,
                findEligibleUseCase, findSlicesUseCase, findSerialsUseCase, poLookupUseCase,
                findDebitMemoByPurchaseReturnUseCase, findApprovalUseCase, partyLookupProvider, webMapper, messageSource);
    }

    @Test
    void list_retainsKeywordStatusAndSpringPage() {
        when(findUseCase.execute(eq("PRT"), eq(PurchaseReturnStatus.DRAFT), any()))
                .thenReturn(new Page<>(List.of(), 0, 10, 0));
        Model model = new ExtendedModelMap();

        String view = controller.list("PRT", PurchaseReturnStatus.DRAFT, PageRequest.of(0, 10), model);

        assertThat(view).isEqualTo("purchasing/purchase-returns/list");
        assertThat(model.getAttribute("keyword")).isEqualTo("PRT");
        assertThat(model.getAttribute("status")).isEqualTo(PurchaseReturnStatus.DRAFT);
        assertThat(model.getAttribute("page")).isInstanceOf(PageImpl.class);
    }

    @Test
    void selectSource_retainsFiltersAndLookupTrinity() {
        org.springframework.data.domain.Page<com.solusi.erp.purchasing.purchasereturn.application.usecase.query.EligibleGoodsReceiptRow> page =
                new PageImpl<>(List.of());
        when(findEligibleUseCase.execute(eq("GR"), eq(3L), eq(2L), any(), any(), any())).thenReturn(page);
        when(partyLookupProvider.resolve(3L)).thenReturn(new LookupDto(3L, "Supplier", "SUP"));
        when(poLookupUseCase.execute("", 100)).thenReturn(List.of(new LookupDto(2L, "PO-001", "PO-001")));
        Model model = new ExtendedModelMap();

        String view = controller.selectSource(
                "GR", 3L, 2L, LocalDate.of(2026, 6, 1), LocalDate.of(2026, 6, 2),
                PageRequest.of(0, 10), model);

        assertThat(view).isEqualTo("purchasing/purchase-returns/select-source");
        assertThat(model.getAttribute("supplierLookup")).isEqualTo(new LookupDto(3L, "Supplier", "SUP"));
        assertThat(model.getAttribute("purchaseOrderLookup")).isEqualTo(new LookupDto(2L, "PO-001", "PO-001"));
    }

    @Test
    void createFromReference_redirectsWithGoodsReceiptId() {
        RedirectAttributesModelMap redirect = new RedirectAttributesModelMap();

        String result = controller.createFromReference(1L, redirect);

        assertThat(result).isEqualTo("redirect:/purchasing/purchase-returns/create");
        assertThat(redirect.getAttribute("goodsReceiptId")).isEqualTo("1");
    }

    @Test
    void goodsReceiptLineSelector_retainsExclusions() {
        org.springframework.data.domain.Page<com.solusi.erp.purchasing.purchasereturn.application.usecase.query.ReturnableGrLineSlice> page =
                new PageImpl<>(List.of());
        when(findSlicesUseCase.execute(eq(1L), eq("product"), eq(List.of("11:40")), any())).thenReturn(page);
        Model model = new ExtendedModelMap();

        String view = controller.goodsReceiptLineSelector(
                1L, "product", List.of("11:40"), PageRequest.of(0, 10), model);

        assertThat(view).isEqualTo("purchasing/purchase-returns/fragments/gr-line-selector-modal");
        assertThat(model.getAttribute("excludedSelectionKeys")).isEqualTo(List.of("11:40"));
    }

    @Test
    void submit_passesAuthenticatedUserAndPartySeparately() {
        PurchaseReturn domain = mock(PurchaseReturn.class);
        PurchaseReturnDetailResponse response = new PurchaseReturnDetailResponse();
        when(submitUseCase.execute(1L, 99L, 77L, 55L)).thenReturn(domain);
        when(webMapper.toDetailResponse(domain)).thenReturn(response);
        when(messageSource.getMessage(any(), any(), any())).thenReturn("ok");

        controller.submit(1L, 55L, securityUser(99L, 77L));

        verify(submitUseCase).execute(1L, 99L, 77L, 55L);
    }

    @Test
    void view_addsApprovalPanelAttributesForCurrentApprover() {
        PurchaseReturn domain = mock(PurchaseReturn.class);
        when(getUseCase.execute(1L)).thenReturn(Optional.of(domain));
        when(webMapper.toDetailResponse(domain)).thenReturn(new PurchaseReturnDetailResponse());
        when(findDebitMemoByPurchaseReturnUseCase.execute(1L)).thenReturn(Optional.empty());
        ApprovalRequest approval = new ApprovalRequest(
                new AuditMetadata(10L, 0L, null, null, null, null),
                "PURCHASE_RETURN", 1L, "PRT-001", ApprovalStatus.PENDING, 77L);
        when(findApprovalUseCase.execute("PURCHASE_RETURN", 1L)).thenReturn(Optional.of(approval));
        Model model = new ExtendedModelMap();

        String view = controller.view(1L, model, securityUser(99L, 77L));

        assertThat(view).isEqualTo("purchasing/purchase-returns/view");
        assertThat(model.getAttribute("approvalRequestId")).isEqualTo(10L);
        assertThat(model.getAttribute("isCurrentApprover")).isEqualTo(true);
    }

    @Test
    void lookupController_delegatesWithoutRepositoryDependency() {
        when(poLookupUseCase.execute("PO", 10)).thenReturn(List.of(new LookupDto(2L, "PO-001", "PO-001")));

        List<LookupDto> result = new PurchaseReturnLookupController(poLookupUseCase).search("PO", 10);

        assertThat(result).containsExactly(new LookupDto(2L, "PO-001", "PO-001"));
    }

    @Test
    void submitRoute_requiresSubmitPermission() throws Exception {
        Method method = PurchaseReturnController.class.getMethod("submit", Long.class, Long.class, SecurityUser.class);

        assertThat(method.getAnnotation(PreAuthorize.class).value())
                .isEqualTo("hasAuthority('PURCHASE-RETURN_SUBMIT')");
    }

    @Test
    void routesDeclareStatusSpecificPermissions() throws Exception {
        assertPermission("list", "hasAuthority('PURCHASE-RETURN_READ')",
                String.class, PurchaseReturnStatus.class, org.springframework.data.domain.Pageable.class, Model.class);
        assertPermission("selectSource", "hasAuthority('PURCHASE-RETURN_CREATE')",
                String.class, Long.class, Long.class, LocalDate.class, LocalDate.class,
                org.springframework.data.domain.Pageable.class, Model.class);
        assertPermission("createForm", "hasAuthority('PURCHASE-RETURN_CREATE')", Long.class, Model.class);
        assertPermission("create", "hasAuthority('PURCHASE-RETURN_CREATE')", PurchaseReturnSaveRequest.class);
        assertPermission("editForm", "hasAuthority('PURCHASE-RETURN_UPDATE')", Long.class, Model.class);
        assertPermission("update", "hasAuthority('PURCHASE-RETURN_UPDATE')", Long.class, PurchaseReturnSaveRequest.class);
        assertPermission("goodsReceiptLineSelector", "hasAnyAuthority('PURCHASE-RETURN_CREATE', 'PURCHASE-RETURN_UPDATE')",
                Long.class, String.class, List.class, org.springframework.data.domain.Pageable.class, Model.class);
        assertPermission("serialSelector", "hasAnyAuthority('PURCHASE-RETURN_CREATE', 'PURCHASE-RETURN_UPDATE')",
                Long.class, Long.class, String.class, List.class, org.springframework.data.domain.Pageable.class, Model.class);
        assertPermission("cancelSubmission", "hasAuthority('PURCHASE-RETURN_CANCEL')",
                Long.class, String.class, SecurityUser.class);
        assertPermission("confirm", "hasAuthority('PURCHASE-RETURN_CONFIRM')", Long.class);
        assertPermission("cancelApproved", "hasAuthority('PURCHASE-RETURN_CANCEL')", Long.class);
        assertPermission("view", "hasAuthority('PURCHASE-RETURN_READ')", Long.class, Model.class, SecurityUser.class);
    }

    private SecurityUser securityUser(Long userId, Long partyId) {
        User user = new User();
        user.setId(userId);
        user.setPartyId(partyId);
        return new SecurityUser(user);
    }

    private void assertPermission(String methodName, String expected, Class<?>... parameterTypes) throws Exception {
        Method method = PurchaseReturnController.class.getMethod(methodName, parameterTypes);
        assertThat(method.getAnnotation(PreAuthorize.class).value()).isEqualTo(expected);
    }
}
