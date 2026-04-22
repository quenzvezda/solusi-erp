package com.solusi.erp.purchasing.purchaserequisition.web.controller;

import com.solusi.erp.common.approval.application.usecase.query.FindApprovalRequestByReferenceUseCase;
import com.solusi.erp.common.approval.domain.model.ApprovalRequest;
import com.solusi.erp.common.approval.domain.model.ApprovalStatus;
import com.solusi.erp.core.annotation.DefaultRedirectUrl;
import com.solusi.erp.core.domain.model.Pageable;
import com.solusi.erp.core.dto.LookupDto;
import com.solusi.erp.core.infrastructure.util.PageableMapper;
import com.solusi.erp.core.dto.ApiResponse;
import com.solusi.erp.inventory.facility.domain.port.FacilityLookupProvider;
import com.solusi.erp.master.currency.domain.repository.CurrencyRepository;
import com.solusi.erp.master.party.domain.port.PartyLookupProvider;
import com.solusi.erp.purchasing.purchaserequisition.application.usecase.command.*;
import com.solusi.erp.purchasing.purchaserequisition.application.usecase.query.*;
import com.solusi.erp.purchasing.purchaserequisition.domain.model.PurchaseRequisition;
import com.solusi.erp.purchasing.purchaserequisition.domain.model.PurchaseRequisitionPriority;
import com.solusi.erp.purchasing.purchaserequisition.domain.model.PurchaseRequisitionStatus;
import com.solusi.erp.purchasing.purchaserequisition.web.dto.*;
import com.solusi.erp.purchasing.purchaserequisition.web.dto.api.SplPriceResponse;
import com.solusi.erp.purchasing.purchaserequisition.web.mapper.PurchaseRequisitionWebMapper;
import com.solusi.erp.security.shared.model.SecurityUser;
import com.solusi.erp.util.HtmxResponseUtility;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/purchasing/purchase-requisitions")
@RequiredArgsConstructor
@DefaultRedirectUrl
public class PurchaseRequisitionController {

    private final CreatePurchaseRequisitionUseCase createPurchaseRequisitionUseCase;
    private final UpdatePurchaseRequisitionUseCase updatePurchaseRequisitionUseCase;
    private final DeletePurchaseRequisitionUseCase deletePurchaseRequisitionUseCase;
    private final SubmitPurchaseRequisitionUseCase submitPurchaseRequisitionUseCase;
    private final CancelPurchaseRequisitionUseCase cancelPurchaseRequisitionUseCase;
    private final FindPurchaseRequisitionsUseCase findPurchaseRequisitionsUseCase;
    private final GetPurchaseRequisitionEditViewUseCase getPurchaseRequisitionEditViewUseCase;
    private final ResolvePurchaseRequisitionSplPriceUseCase resolvePurchaseRequisitionSplPriceUseCase;
    private final FindApprovalRequestByReferenceUseCase findApprovalRequestByReferenceUseCase;
    private final CurrencyRepository currencyRepository;
    private final PurchaseRequisitionWebMapper webMapper;
    private final MessageSource messageSource;
    private final PartyLookupProvider partyLookupProvider;
    private final FacilityLookupProvider facilityLookupProvider;

    @GetMapping
    @PreAuthorize("hasAuthority('PR_READ')")
    public String list(@RequestParam(required = false) String keyword,
                       org.springframework.data.domain.Pageable springPageable,
                       Model model) {
        Pageable domainPageable = PageableMapper.toDomain(springPageable);
        com.solusi.erp.core.domain.model.Page<PurchaseRequisition> domainPage =
            findPurchaseRequisitionsUseCase.execute(keyword, domainPageable);

        List<PurchaseRequisitionSummaryResponse> content = domainPage.content().stream()
            .map(webMapper::toSummaryResponse)
            .collect(Collectors.toList());

        Page<PurchaseRequisitionSummaryResponse> springPage =
            new PageImpl<>(content, springPageable, domainPage.totalElements());
        model.addAttribute("page", springPage);
        model.addAttribute("keyword", keyword);
        return "purchasing/purchase-requisitions/list";
    }

    @GetMapping("/create")
    @PreAuthorize("hasAuthority('PR_CREATE')")
    public String showCreateForm(Model model) {
        PurchaseRequisitionSaveRequest request = new PurchaseRequisitionSaveRequest();
        request.setRequestDate(LocalDate.now());
        request.setPriority(PurchaseRequisitionPriority.NORMAL);
        model.addAttribute("prRequest", request);
        model.addAttribute("currencies", currencyRepository.findByIsActiveTrue());
        return "purchasing/purchase-requisitions/form";
    }

    @PostMapping("/create")
    @PreAuthorize("hasAuthority('PR_CREATE')")
    @ResponseBody
    public ResponseEntity<ApiResponse<PurchaseRequisitionDetailResponse>> create(
            @Valid @RequestBody PurchaseRequisitionSaveRequest request) {
        List<LineInput> lines = webMapper.toLineInputs(request.getLines());
        PurchaseRequisition domain = createPurchaseRequisitionUseCase.execute(
            request.getRequestDate(), request.getRequesterId(), request.getFacilityId(),
            request.getDepartment(), request.getPriority(),
            request.getNote(), request.getSuggestedSupplierId(), request.getCurrencyId(), lines
        );
        PurchaseRequisitionDetailResponse data = webMapper.toDetailResponse(domain);
        String msg = messageSource.getMessage("msg.success.create", null, LocaleContextHolder.getLocale());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(msg, data));
    }

    @GetMapping("/edit/{id}")
    @PreAuthorize("hasAuthority('PR_UPDATE')")
    public String showEditForm(@PathVariable Long id, Model model) {
        PurchaseRequisition domain = getPurchaseRequisitionEditViewUseCase.execute(id)
            .orElseThrow(() -> new RuntimeException("Purchase requisition not found"));
        if (domain.getStatus() != PurchaseRequisitionStatus.DRAFT) {
            return "redirect:/purchasing/purchase-requisitions/view/" + id;
        }
        model.addAttribute("prRequest", webMapper.toSaveRequest(domain));
        model.addAttribute("auditInfo", webMapper.toDetailResponse(domain));
        model.addAttribute("prUI", buildPRUI(domain));
        model.addAttribute("currencies", currencyRepository.findByIsActiveTrue());
        return "purchasing/purchase-requisitions/form";
    }

    @PostMapping("/edit/{id}")
    @PreAuthorize("hasAuthority('PR_UPDATE')")
    @ResponseBody
    public ResponseEntity<ApiResponse<PurchaseRequisitionDetailResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody PurchaseRequisitionSaveRequest request) {
        List<LineInput> lines = webMapper.toLineInputs(request.getLines());
        PurchaseRequisition domain = updatePurchaseRequisitionUseCase.execute(
            id, request.getRequestDate(), request.getFacilityId(),
            request.getDepartment(), request.getPriority(),
            request.getNote(), request.getSuggestedSupplierId(), request.getCurrencyId(), lines
        );
        PurchaseRequisitionDetailResponse data = webMapper.toDetailResponse(domain);
        String msg = messageSource.getMessage("msg.success.update", null, LocaleContextHolder.getLocale());
        return ResponseEntity.ok(ApiResponse.success(msg, data));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('PR_DELETE')")
    @ResponseBody
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        deletePurchaseRequisitionUseCase.execute(id);
        String msg = messageSource.getMessage("msg.success.delete", null, LocaleContextHolder.getLocale());
        return HtmxResponseUtility.okWithRefreshTableAndSuccess(msg);
    }

    @PostMapping("/{id}/submit")
    @PreAuthorize("hasAuthority('PR_SUBMIT')")
    @ResponseBody
    public ResponseEntity<ApiResponse<PurchaseRequisitionDetailResponse>> submit(
            @PathVariable Long id,
            @RequestParam Long approverId) {
        PurchaseRequisition domain = submitPurchaseRequisitionUseCase.execute(id, approverId);
        PurchaseRequisitionDetailResponse data = webMapper.toDetailResponse(domain);
        String msg = messageSource.getMessage("msg.success.pr.submitted", null, LocaleContextHolder.getLocale());
        return ResponseEntity.ok(ApiResponse.success(msg, data));
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAuthority('PR_UPDATE')")
    @ResponseBody
    public ResponseEntity<ApiResponse<PurchaseRequisitionDetailResponse>> cancel(@PathVariable Long id) {
        PurchaseRequisition domain = cancelPurchaseRequisitionUseCase.execute(id);
        PurchaseRequisitionDetailResponse data = webMapper.toDetailResponse(domain);
        String msg = messageSource.getMessage("msg.success.pr.cancelled", null, LocaleContextHolder.getLocale());
        return ResponseEntity.ok(ApiResponse.success(msg, data));
    }

    @GetMapping("/view/{id}")
    @PreAuthorize("hasAuthority('PR_READ')")
    public String view(@PathVariable Long id, Model model,
                       @AuthenticationPrincipal UserDetails principal) {
        PurchaseRequisition domain = getPurchaseRequisitionEditViewUseCase.execute(id)
            .orElseThrow(() -> new RuntimeException("Purchase requisition not found"));
        model.addAttribute("pr", webMapper.toDetailResponse(domain));

        Optional<ApprovalRequest> approvalRequest =
            findApprovalRequestByReferenceUseCase.execute("PURCHASE_REQUISITION", id);
        approvalRequest.ifPresent(req -> {
            model.addAttribute("approvalRequestId", req.getId());
            model.addAttribute("approvalStatus", req.getStatus());
            boolean isCurrentApprover = false;
            if (principal instanceof SecurityUser securityUser) {
                Long partyId = securityUser.user().getPartyId();
                isCurrentApprover = partyId != null && partyId.equals(req.getCurrentApproverId())
                        && req.getStatus() == ApprovalStatus.PENDING;
                model.addAttribute("currentPartyId", partyId);
            }
            model.addAttribute("isCurrentApprover", isCurrentApprover);
        });

        return "purchasing/purchase-requisitions/view";
    }

    /**
     * SPL auto-fill endpoint: returns matching SPL price for given supplier+product+uom+currency combo.
     * Used by PR lines to auto-fill unitPrice when supplier and product are selected.
     */
    @GetMapping("/api/spl-price")
    @PreAuthorize("hasAuthority('LOOKUP_SUPPLIER-PRICE-LIST')")
    @ResponseBody
    public ResponseEntity<?> getSplPrice(
            @RequestParam Long supplierId,
            @RequestParam Long productId,
            @RequestParam Long uomId,
            @RequestParam Long currencyId,
            @RequestParam(required = false) LocalDate requiredDate) {
        return resolvePurchaseRequisitionSplPriceUseCase.execute(
                supplierId, productId, uomId, currencyId, requiredDate
        ).<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    /**
     * Build Trinity Data for PR header autocompletes (requester, facility, suggestedSupplier).
     * Used by edit form to hydrate initialText and initialSubtext for SSR.
     */
    private Map<String, Object> buildPRUI(PurchaseRequisition domain) {
        Map<String, Object> ui = new HashMap<>();

        LookupDto requester = partyLookupProvider.resolve(domain.getRequesterId());
        if (requester != null) {
            ui.put("requesterText", requester.name());
            ui.put("requesterSubtext", requester.subText());
        }

        if (domain.getFacilityId() != null) {
            LookupDto facility = facilityLookupProvider.resolve(domain.getFacilityId());
            if (facility != null) {
                ui.put("facilityText", facility.name());
                ui.put("facilitySubtext", facility.subText());
            }
        }

        if (domain.getSuggestedSupplierId() != null) {
            LookupDto supplier = partyLookupProvider.resolve(domain.getSuggestedSupplierId());
            if (supplier != null) {
                ui.put("supplierText", supplier.name());
                ui.put("supplierSubtext", supplier.subText());
            }
        }

        return ui;
    }
}
