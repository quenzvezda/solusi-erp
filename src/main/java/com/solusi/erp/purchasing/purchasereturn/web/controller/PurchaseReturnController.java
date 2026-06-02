package com.solusi.erp.purchasing.purchasereturn.web.controller;

import com.solusi.erp.common.approval.application.usecase.query.FindApprovalRequestByReferenceUseCase;
import com.solusi.erp.common.approval.domain.model.ApprovalRequest;
import com.solusi.erp.common.approval.domain.model.ApprovalStatus;
import com.solusi.erp.core.annotation.DefaultRedirectUrl;
import com.solusi.erp.core.domain.model.Pageable;
import com.solusi.erp.core.dto.ApiResponse;
import com.solusi.erp.core.dto.LookupDto;
import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.core.infrastructure.util.PageableMapper;
import com.solusi.erp.master.party.domain.port.PartyLookupProvider;
import com.solusi.erp.purchasing.purchasereturn.application.usecase.command.CancelApprovedPurchaseReturnUseCase;
import com.solusi.erp.purchasing.purchasereturn.application.usecase.command.CancelPurchaseReturnSubmissionUseCase;
import com.solusi.erp.purchasing.purchasereturn.application.usecase.command.ConfirmPurchaseReturnUseCase;
import com.solusi.erp.purchasing.purchasereturn.application.usecase.command.CreatePurchaseReturnUseCase;
import com.solusi.erp.purchasing.purchasereturn.application.usecase.command.PurchaseReturnLineCommand;
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
import com.solusi.erp.purchasing.purchasereturn.domain.model.PurchaseReturnReason;
import com.solusi.erp.purchasing.purchasereturn.domain.model.PurchaseReturnStatus;
import com.solusi.erp.purchasing.purchasereturn.web.dto.PurchaseReturnDetailResponse;
import com.solusi.erp.purchasing.purchasereturn.web.dto.PurchaseReturnSaveRequest;
import com.solusi.erp.purchasing.purchasereturn.web.dto.PurchaseReturnSummaryResponse;
import com.solusi.erp.purchasing.purchasereturn.web.mapper.PurchaseReturnWebMapper;
import com.solusi.erp.security.shared.model.SecurityUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/purchasing/purchase-returns")
@RequiredArgsConstructor
@DefaultRedirectUrl
public class PurchaseReturnController {

    private final CreatePurchaseReturnUseCase createUseCase;
    private final UpdatePurchaseReturnUseCase updateUseCase;
    private final SubmitPurchaseReturnUseCase submitUseCase;
    private final CancelPurchaseReturnSubmissionUseCase cancelSubmissionUseCase;
    private final ConfirmPurchaseReturnUseCase confirmUseCase;
    private final CancelApprovedPurchaseReturnUseCase cancelApprovedUseCase;
    private final FindPurchaseReturnsUseCase findUseCase;
    private final GetPurchaseReturnUseCase getUseCase;
    private final GetPurchaseReturnCreateViewUseCase getCreateViewUseCase;
    private final GetPurchaseReturnEditViewUseCase getEditViewUseCase;
    private final FindEligiblePurchaseReturnGoodsReceiptsUseCase findEligibleGoodsReceiptsUseCase;
    private final FindPurchaseReturnGrLineSlicesUseCase findGrLineSlicesUseCase;
    private final FindPurchaseReturnSerialsUseCase findSerialsUseCase;
    private final GetEligiblePurchaseReturnPurchaseOrderLookupUseCase purchaseOrderLookupUseCase;
    private final FindApprovalRequestByReferenceUseCase findApprovalRequestByReferenceUseCase;
    private final PartyLookupProvider partyLookupProvider;
    private final PurchaseReturnWebMapper webMapper;
    private final MessageSource messageSource;

    @GetMapping
    @PreAuthorize("hasAuthority('PURCHASE-RETURN_READ')")
    public String list(@RequestParam(required = false) String keyword,
                       @RequestParam(required = false) PurchaseReturnStatus status,
                       org.springframework.data.domain.Pageable springPageable,
                       Model model) {
        Pageable pageable = PageableMapper.toDomain(springPageable);
        com.solusi.erp.core.domain.model.Page<PurchaseReturn> domainPage = findUseCase.execute(keyword, status, pageable);
        List<PurchaseReturnSummaryResponse> content = domainPage.content().stream()
                .map(webMapper::toSummaryResponse)
                .toList();
        model.addAttribute("page", new PageImpl<>(content, springPageable, domainPage.totalElements()));
        model.addAttribute("keyword", keyword);
        model.addAttribute("status", status);
        model.addAttribute("statuses", PurchaseReturnStatus.values());
        return "purchasing/purchase-returns/list";
    }

    @GetMapping("/select-source")
    @PreAuthorize("hasAuthority('PURCHASE-RETURN_CREATE')")
    public String selectSource(@RequestParam(required = false) String keyword,
                               @RequestParam(required = false) Long supplierId,
                               @RequestParam(required = false) Long purchaseOrderId,
                               @RequestParam(required = false) LocalDate receiptDateFrom,
                               @RequestParam(required = false) LocalDate receiptDateTo,
                               org.springframework.data.domain.Pageable pageable,
                               Model model) {
        model.addAttribute("page", findEligibleGoodsReceiptsUseCase.execute(
                keyword, supplierId, purchaseOrderId, receiptDateFrom, receiptDateTo, pageable));
        model.addAttribute("keyword", keyword);
        model.addAttribute("supplierId", supplierId);
        model.addAttribute("purchaseOrderId", purchaseOrderId);
        model.addAttribute("receiptDateFrom", receiptDateFrom);
        model.addAttribute("receiptDateTo", receiptDateTo);
        model.addAttribute("supplierLookup", supplierId == null ? null : partyLookupProvider.resolve(supplierId));
        model.addAttribute("purchaseOrderLookup", resolvePurchaseOrderLookup(purchaseOrderId));
        return "purchasing/purchase-returns/select-source";
    }

    @GetMapping("/create-from-reference")
    @PreAuthorize("hasAuthority('PURCHASE-RETURN_CREATE')")
    public String createFromReference(@RequestParam Long goodsReceiptId, RedirectAttributes redirectAttributes) {
        redirectAttributes.addAttribute("goodsReceiptId", goodsReceiptId);
        return "redirect:/purchasing/purchase-returns/create";
    }

    @GetMapping("/create")
    @PreAuthorize("hasAuthority('PURCHASE-RETURN_CREATE')")
    public String createForm(@RequestParam Long goodsReceiptId, Model model) {
        PurchaseReturnSaveRequest request = webMapper.toCreateRequest(getCreateViewUseCase.execute(goodsReceiptId));
        request.setReturnDate(LocalDate.now());
        model.addAttribute("purchaseReturnRequest", request);
        model.addAttribute("reasons", PurchaseReturnReason.values());
        return "purchasing/purchase-returns/form";
    }

    @PostMapping("/create")
    @ResponseBody
    @PreAuthorize("hasAuthority('PURCHASE-RETURN_CREATE')")
    public ResponseEntity<ApiResponse<PurchaseReturnDetailResponse>> create(
            @Valid @RequestBody PurchaseReturnSaveRequest request) {
        PurchaseReturn domain = createUseCase.execute(
                request.getGoodsReceiptId(), request.getReturnDate(), request.getReason(),
                request.getNote(), webMapper.toLineCommands(request.getLines()));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(message("msg.success.purchase-return.created"),
                        webMapper.toDetailResponse(domain)));
    }

    @GetMapping("/edit/{id}")
    @PreAuthorize("hasAuthority('PURCHASE-RETURN_UPDATE')")
    public String editForm(@PathVariable Long id, Model model) {
        GetPurchaseReturnEditViewUseCase.PurchaseReturnEditView view = getEditViewUseCase.execute(id);
        if (!view.purchaseReturn().getStatus().canEdit()) {
            return "redirect:/purchasing/purchase-returns/view/" + id;
        }
        model.addAttribute("purchaseReturnRequest", webMapper.toSaveRequest(view.purchaseReturn()));
        model.addAttribute("availableSlices", view.availableSlices());
        model.addAttribute("auditInfo", webMapper.toDetailResponse(view.purchaseReturn()));
        model.addAttribute("reasons", PurchaseReturnReason.values());
        return "purchasing/purchase-returns/form";
    }

    @PostMapping("/edit/{id}")
    @ResponseBody
    @PreAuthorize("hasAuthority('PURCHASE-RETURN_UPDATE')")
    public ResponseEntity<ApiResponse<PurchaseReturnDetailResponse>> update(
            @PathVariable Long id, @Valid @RequestBody PurchaseReturnSaveRequest request) {
        PurchaseReturn domain = updateUseCase.execute(
                id, request.getReturnDate(), request.getReason(), request.getNote(),
                webMapper.toLineCommands(request.getLines()));
        return ResponseEntity.ok(ApiResponse.success(
                message("msg.success.purchase-return.updated"), webMapper.toDetailResponse(domain)));
    }

    @GetMapping("/selectors/goods-receipt-lines")
    @PreAuthorize("hasAnyAuthority('PURCHASE-RETURN_CREATE', 'PURCHASE-RETURN_UPDATE')")
    public String goodsReceiptLineSelector(@RequestParam Long goodsReceiptId,
                                           @RequestParam(required = false) String keyword,
                                           @RequestParam(required = false) List<String> excludedSelectionKeys,
                                           org.springframework.data.domain.Pageable pageable,
                                           Model model) {
        List<String> exclusions = excludedSelectionKeys == null ? List.of() : excludedSelectionKeys;
        model.addAttribute("page", findGrLineSlicesUseCase.execute(goodsReceiptId, keyword, exclusions, pageable));
        model.addAttribute("goodsReceiptId", goodsReceiptId);
        model.addAttribute("keyword", keyword);
        model.addAttribute("excludedSelectionKeys", exclusions);
        return "purchasing/purchase-returns/fragments/gr-line-selector-modal";
    }

    @GetMapping("/selectors/serials")
    @PreAuthorize("hasAnyAuthority('PURCHASE-RETURN_CREATE', 'PURCHASE-RETURN_UPDATE')")
    public String serialSelector(@RequestParam Long goodsReceiptId,
                                 @RequestParam Long goodsReceiptLineId,
                                 @RequestParam(required = false) String keyword,
                                 @RequestParam(required = false) List<String> excludedSelectionKeys,
                                 org.springframework.data.domain.Pageable pageable,
                                 Model model) {
        List<String> exclusions = excludedSelectionKeys == null ? List.of() : excludedSelectionKeys;
        model.addAttribute("page", findSerialsUseCase.execute(
                goodsReceiptId, goodsReceiptLineId, keyword, exclusions, pageable));
        model.addAttribute("goodsReceiptId", goodsReceiptId);
        model.addAttribute("goodsReceiptLineId", goodsReceiptLineId);
        model.addAttribute("keyword", keyword);
        model.addAttribute("excludedSelectionKeys", exclusions);
        return "purchasing/purchase-returns/fragments/serial-selector-modal";
    }

    @PostMapping("/{id}/submit")
    @ResponseBody
    @PreAuthorize("hasAuthority('PURCHASE-RETURN_SUBMIT')")
    public ResponseEntity<ApiResponse<PurchaseReturnDetailResponse>> submit(
            @PathVariable Long id,
            @RequestParam Long approverId,
            @AuthenticationPrincipal SecurityUser securityUser) {
        PurchaseReturn domain = submitUseCase.execute(
                id, userId(securityUser), partyId(securityUser), approverId);
        return ResponseEntity.ok(ApiResponse.success(
                message("msg.success.purchase-return.submitted"), webMapper.toDetailResponse(domain)));
    }

    @PostMapping("/{id}/cancel-submission")
    @ResponseBody
    @PreAuthorize("hasAuthority('PURCHASE-RETURN_CANCEL')")
    public ResponseEntity<ApiResponse<PurchaseReturnDetailResponse>> cancelSubmission(
            @PathVariable Long id,
            @RequestParam String notes,
            @AuthenticationPrincipal SecurityUser securityUser) {
        PurchaseReturn domain = cancelSubmissionUseCase.execute(id, userId(securityUser), notes);
        return ResponseEntity.ok(ApiResponse.success(
                message("msg.success.purchase-return.cancelled"), webMapper.toDetailResponse(domain)));
    }

    @PostMapping("/{id}/confirm")
    @ResponseBody
    @PreAuthorize("hasAuthority('PURCHASE-RETURN_CONFIRM')")
    public ResponseEntity<ApiResponse<PurchaseReturnDetailResponse>> confirm(@PathVariable Long id) {
        PurchaseReturn domain = confirmUseCase.execute(id);
        return ResponseEntity.ok(ApiResponse.success(
                message("msg.success.purchase-return.confirmed"), webMapper.toDetailResponse(domain)));
    }

    @PostMapping("/{id}/cancel")
    @ResponseBody
    @PreAuthorize("hasAuthority('PURCHASE-RETURN_CANCEL')")
    public ResponseEntity<ApiResponse<PurchaseReturnDetailResponse>> cancelApproved(@PathVariable Long id) {
        PurchaseReturn domain = cancelApprovedUseCase.execute(id);
        return ResponseEntity.ok(ApiResponse.success(
                message("msg.success.purchase-return.cancelled"), webMapper.toDetailResponse(domain)));
    }

    @GetMapping("/view/{id}")
    @PreAuthorize("hasAuthority('PURCHASE-RETURN_READ')")
    public String view(@PathVariable Long id,
                       Model model,
                       @AuthenticationPrincipal SecurityUser securityUser) {
        PurchaseReturn domain = getUseCase.execute(id)
                .orElseThrow(() -> new DomainException("msg.error.purchase-return.not-found"));
        model.addAttribute("purchaseReturn", webMapper.toDetailResponse(domain));
        addApprovalAttributes(id, securityUser, model);
        return "purchasing/purchase-returns/view";
    }

    private void addApprovalAttributes(Long id, SecurityUser securityUser, Model model) {
        Optional<ApprovalRequest> approval = findApprovalRequestByReferenceUseCase.execute("PURCHASE_RETURN", id);
        approval.ifPresent(request -> {
            Long currentPartyId = securityUser == null ? null : securityUser.user().getPartyId();
            model.addAttribute("approvalRequestId", request.getId());
            model.addAttribute("approvalStatus", request.getStatus());
            model.addAttribute("currentPartyId", currentPartyId);
            model.addAttribute("isCurrentApprover",
                    currentPartyId != null
                            && currentPartyId.equals(request.getCurrentApproverId())
                            && request.getStatus() == ApprovalStatus.PENDING);
        });
    }

    private LookupDto resolvePurchaseOrderLookup(Long purchaseOrderId) {
        if (purchaseOrderId == null) {
            return null;
        }
        return purchaseOrderLookupUseCase.execute("", 100).stream()
                .filter(lookup -> purchaseOrderId.equals(lookup.id()))
                .findFirst()
                .orElse(null);
    }

    private Long userId(SecurityUser securityUser) {
        if (securityUser == null || securityUser.user() == null) {
            throw new DomainException("msg.error.purchase-return.auth-required");
        }
        return securityUser.user().getId();
    }

    private Long partyId(SecurityUser securityUser) {
        Long partyId = securityUser == null || securityUser.user() == null
                ? null
                : securityUser.user().getPartyId();
        if (partyId == null) {
            throw new DomainException("msg.error.purchase-return.requester-party-required");
        }
        return partyId;
    }

    private String message(String key) {
        return messageSource.getMessage(key, null, LocaleContextHolder.getLocale());
    }
}
