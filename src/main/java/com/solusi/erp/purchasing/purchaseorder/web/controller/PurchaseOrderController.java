package com.solusi.erp.purchasing.purchaseorder.web.controller;

import com.solusi.erp.common.approval.application.usecase.query.FindApprovalRequestByReferenceUseCase;
import com.solusi.erp.common.approval.domain.model.ApprovalRequest;
import com.solusi.erp.common.approval.domain.model.ApprovalStatus;
import com.solusi.erp.core.annotation.DefaultRedirectUrl;
import com.solusi.erp.core.domain.model.Pageable;
import com.solusi.erp.core.infrastructure.util.PageableMapper;
import com.solusi.erp.core.dto.ApiResponse;
import com.solusi.erp.core.dto.LookupDto;
import com.solusi.erp.inventory.facility.domain.port.FacilityLookupProvider;
import com.solusi.erp.inventory.product.domain.port.ProductLookupProvider;
import com.solusi.erp.inventory.uom.domain.port.UomLookupProvider;
import com.solusi.erp.master.currency.domain.port.CurrencyLookupProvider;
import com.solusi.erp.master.party.domain.port.PartyLookupProvider;
import com.solusi.erp.purchasing.purchaseorder.application.usecase.command.*;
import com.solusi.erp.purchasing.purchaseorder.application.usecase.query.*;
import com.solusi.erp.purchasing.purchaseorder.domain.model.PurchaseOrder;
import com.solusi.erp.purchasing.purchaseorder.domain.model.PurchaseOrderStatus;
import com.solusi.erp.purchasing.purchaseorder.domain.model.PurchaseOrderType;
import com.solusi.erp.purchasing.purchaserequisition.domain.repository.PurchaseRequisitionRepository;
import com.solusi.erp.purchasing.purchaseorder.web.dto.*;
import com.solusi.erp.purchasing.purchaseorder.web.mapper.PurchaseOrderWebMapper;
import com.solusi.erp.security.shared.model.SecurityUser;
import com.solusi.erp.util.HtmxResponseUtility;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/purchasing/purchase-orders")
@RequiredArgsConstructor
@DefaultRedirectUrl
public class PurchaseOrderController {

    private final CreatePurchaseOrderUseCase createPurchaseOrderUseCase;
    private final UpdatePurchaseOrderUseCase updatePurchaseOrderUseCase;
    private final DeletePurchaseOrderUseCase deletePurchaseOrderUseCase;
    private final SubmitPurchaseOrderUseCase submitPurchaseOrderUseCase;
    private final SendPurchaseOrderUseCase sendPurchaseOrderUseCase;
    private final CancelPurchaseOrderUseCase cancelPurchaseOrderUseCase;
    private final FindPurchaseOrdersUseCase findPurchaseOrdersUseCase;
    private final GetPurchaseOrderEditViewUseCase getPurchaseOrderEditViewUseCase;
    private final FindPurchaseOrderPrSelectorUseCase findPurchaseOrderPrSelectorUseCase;
    private final FindPurchaseOrderPrLineSelectorUseCase findPurchaseOrderPrLineSelectorUseCase;
    private final FindApprovalRequestByReferenceUseCase findApprovalRequestByReferenceUseCase;
    private final PurchaseOrderWebMapper webMapper;
    private final MessageSource messageSource;
    private final PartyLookupProvider partyLookupProvider;
    private final FacilityLookupProvider facilityLookupProvider;
    private final CurrencyLookupProvider currencyLookupProvider;
    private final PurchaseRequisitionRepository purchaseRequisitionRepository;

    @GetMapping
    @PreAuthorize("hasAuthority('PO_READ')")
    public String list(@RequestParam(required = false) String keyword,
                       org.springframework.data.domain.Pageable springPageable,
                       Model model) {
        Pageable domainPageable = PageableMapper.toDomain(springPageable);
        com.solusi.erp.core.domain.model.Page<PurchaseOrder> domainPage =
            findPurchaseOrdersUseCase.execute(keyword, domainPageable);

        List<PurchaseOrderSummaryResponse> content = domainPage.content().stream()
            .map(webMapper::toSummaryResponse)
            .collect(Collectors.toList());

        Page<PurchaseOrderSummaryResponse> springPage =
            new PageImpl<>(content, springPageable, domainPage.totalElements());
        model.addAttribute("page", springPage);
        model.addAttribute("keyword", keyword);
        return "purchasing/purchase-orders/list";
    }

    @GetMapping("/create")
    @PreAuthorize("hasAuthority('PO_CREATE')")
    public String showCreateForm(Model model) {
        PurchaseOrderSaveRequest request = new PurchaseOrderSaveRequest();
        request.setOrderDate(LocalDate.now());
        request.setExchangeRate(BigDecimal.ONE);
        request.setPaymentTermDays(30);
        request.setTaxRate(BigDecimal.ZERO);
        model.addAttribute("poRequest", request);
        return "purchasing/purchase-orders/form";
    }

    @GetMapping("/selectors/purchase-requisitions")
    @PreAuthorize("hasAuthority('PO_CREATE')")
    public String showPrSelector(@RequestParam(required = false) String keyword,
                                 @RequestParam(required = false) Long supplierId,
                                 org.springframework.data.domain.Pageable pageable,
                                 Model model) {
        model.addAttribute("page", findPurchaseOrderPrSelectorUseCase.execute(keyword, supplierId, pageable));
        model.addAttribute("keyword", keyword);
        model.addAttribute("supplierId", supplierId);
        return "purchasing/purchase-orders/fragments/pr-selector-modal";
    }

    @GetMapping("/selectors/purchase-requisition-lines")
    @PreAuthorize("hasAnyAuthority('PO_CREATE', 'PO_UPDATE')")
    public String showPrLineSelector(@RequestParam Long prId,
                                     @RequestParam(required = false) String keyword,
                                     @RequestParam(required = false) List<Long> excludePrLineIds,
                                     org.springframework.data.domain.Pageable pageable,
                                     Model model) {
        model.addAttribute("page", findPurchaseOrderPrLineSelectorUseCase.execute(prId, keyword, excludePrLineIds, pageable));
        model.addAttribute("prId", prId);
        model.addAttribute("keyword", keyword);
        model.addAttribute("excludePrLineIds", excludePrLineIds);
        return "purchasing/purchase-orders/fragments/pr-line-selector-modal";
    }

    @PostMapping("/create")
    @PreAuthorize("hasAuthority('PO_CREATE')")
    @ResponseBody
    public ResponseEntity<ApiResponse<PurchaseOrderDetailResponse>> create(
            @Valid @RequestBody PurchaseOrderSaveRequest request) {
        List<PoLineInput> lines = webMapper.toLineInputs(request.getLines());
        PurchaseOrder domain = createPurchaseOrderUseCase.execute(
            request.getOrderDate(), request.getExpectedDate(),
            request.getSupplierId(), request.getFacilityId(),
            request.getCurrencyId(), request.getExchangeRate(),
            request.getPaymentTermDays(), request.getPrId(),
            request.getPoType(),
            request.getTaxId(), request.getTaxCode(), request.getTaxName(), request.getTaxRate(),
            request.getTaxCalculationMode(),
            request.getNote(), lines
        );
        PurchaseOrderDetailResponse data = webMapper.toDetailResponse(domain);
        String msg = messageSource.getMessage("msg.success.create", null, LocaleContextHolder.getLocale());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(msg, data));
    }

    @GetMapping("/edit/{id}")
    @PreAuthorize("hasAuthority('PO_UPDATE')")
    public String showEditForm(@PathVariable Long id, Model model) {
        PurchaseOrder domain = getPurchaseOrderEditViewUseCase.execute(id)
            .orElseThrow(() -> new RuntimeException("Purchase order not found"));
        if (domain.getStatus() != PurchaseOrderStatus.DRAFT) {
            return "redirect:/purchasing/purchase-orders/view/" + id;
        }
        PurchaseOrderSaveRequest saveRequest = webMapper.toSaveRequest(domain);
        enrichStandardLineMaxQuantities(domain, saveRequest);
        model.addAttribute("poRequest", saveRequest);
        model.addAttribute("auditInfo", webMapper.toDetailResponse(domain));
        model.addAttribute("poUI", buildPOUI(domain));
        return "purchasing/purchase-orders/form";
    }

    @PostMapping("/edit/{id}")
    @PreAuthorize("hasAuthority('PO_UPDATE')")
    @ResponseBody
    public ResponseEntity<ApiResponse<PurchaseOrderDetailResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody PurchaseOrderSaveRequest request) {
        List<PoLineInput> lines = webMapper.toLineInputs(request.getLines());
        PurchaseOrder domain = updatePurchaseOrderUseCase.execute(
            id, request.getOrderDate(), request.getExpectedDate(),
            request.getFacilityId(), request.getCurrencyId(),
            request.getExchangeRate(), request.getPaymentTermDays(),
            request.getTaxId(), request.getTaxCode(), request.getTaxName(), request.getTaxRate(),
            request.getTaxCalculationMode(),
            request.getNote(), lines
        );
        PurchaseOrderDetailResponse data = webMapper.toDetailResponse(domain);
        String msg = messageSource.getMessage("msg.success.update", null, LocaleContextHolder.getLocale());
        return ResponseEntity.ok(ApiResponse.success(msg, data));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('PO_DELETE')")
    @ResponseBody
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        deletePurchaseOrderUseCase.execute(id);
        String msg = messageSource.getMessage("msg.success.delete", null, LocaleContextHolder.getLocale());
        return HtmxResponseUtility.okWithRefreshTableAndSuccess(msg);
    }

    @PostMapping("/{id}/submit")
    @PreAuthorize("hasAuthority('PO_SUBMIT')")
    @ResponseBody
    public ResponseEntity<ApiResponse<PurchaseOrderDetailResponse>> submit(
            @PathVariable Long id,
            @RequestParam Long approverId) {
        PurchaseOrder domain = submitPurchaseOrderUseCase.execute(id, approverId);
        PurchaseOrderDetailResponse data = webMapper.toDetailResponse(domain);
        String msg = messageSource.getMessage("msg.success.po.submitted", null, LocaleContextHolder.getLocale());
        return ResponseEntity.ok(ApiResponse.success(msg, data));
    }

    @PostMapping("/{id}/send")
    @PreAuthorize("hasAuthority('PO_SEND')")
    @ResponseBody
    public ResponseEntity<ApiResponse<PurchaseOrderDetailResponse>> send(@PathVariable Long id) {
        PurchaseOrder domain = sendPurchaseOrderUseCase.execute(id);
        PurchaseOrderDetailResponse data = webMapper.toDetailResponse(domain);
        String msg = messageSource.getMessage("msg.success.po.sent", null, LocaleContextHolder.getLocale());
        return ResponseEntity.ok(ApiResponse.success(msg, data));
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAuthority('PO_UPDATE')")
    @ResponseBody
    public ResponseEntity<ApiResponse<PurchaseOrderDetailResponse>> cancel(@PathVariable Long id) {
        PurchaseOrder domain = cancelPurchaseOrderUseCase.execute(id);
        PurchaseOrderDetailResponse data = webMapper.toDetailResponse(domain);
        String msg = messageSource.getMessage("msg.success.po.cancelled", null, LocaleContextHolder.getLocale());
        return ResponseEntity.ok(ApiResponse.success(msg, data));
    }

    @GetMapping("/view/{id}")
    @PreAuthorize("hasAuthority('PO_READ')")
    public String view(@PathVariable Long id, Model model,
                       @AuthenticationPrincipal UserDetails principal) {
        PurchaseOrder domain = getPurchaseOrderEditViewUseCase.execute(id)
            .orElseThrow(() -> new RuntimeException("Purchase order not found"));
        model.addAttribute("po", webMapper.toDetailResponse(domain));

        Optional<ApprovalRequest> approvalRequest =
            findApprovalRequestByReferenceUseCase.execute("PURCHASE_ORDER", id);
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

        return "purchasing/purchase-orders/view";
    }

    private Map<String, Object> buildPOUI(PurchaseOrder domain) {
        Map<String, Object> ui = new HashMap<>();

        LookupDto supplier = partyLookupProvider.resolve(domain.getSupplierId());
        if (supplier != null) {
            ui.put("supplierText", supplier.name());
            ui.put("supplierSubtext", supplier.subText());
        }

        if (domain.getFacilityId() != null) {
            LookupDto facility = facilityLookupProvider.resolve(domain.getFacilityId());
            if (facility != null) {
                ui.put("facilityText", facility.name());
                ui.put("facilitySubtext", facility.subText());
            }
        }

        LookupDto currency = currencyLookupProvider.resolve(domain.getCurrencyId());
        if (currency != null) {
            ui.put("currencyText", currency.name());
            ui.put("currencySubtext", currency.subText());
        }

        if (domain.getPrId() != null) {
            purchaseRequisitionRepository.findById(domain.getPrId())
                .ifPresent(pr -> ui.put("prDisplay", pr.getCode()));
        }

        if (domain.getTaxId() != null || domain.getTaxCode() != null || domain.getTaxName() != null) {
            ui.put("taxText", domain.getTaxName() != null ? domain.getTaxName() : domain.getTaxCode());
            String subText = (domain.getTaxCode() != null ? domain.getTaxCode() : "")
                    + (domain.getTaxCalculationMode() != null ? " - " + domain.getTaxCalculationMode().name() : "");
            ui.put("taxSubtext", subText.strip());
        }

        return ui;
    }

    private void enrichStandardLineMaxQuantities(PurchaseOrder domain, PurchaseOrderSaveRequest saveRequest) {
        if (domain.getPoType() != PurchaseOrderType.STANDARD
                || domain.getPrId() == null
                || saveRequest.getLines() == null
                || saveRequest.getLines().isEmpty()) {
            return;
        }

        Map<Long, java.math.BigDecimal> remainingByPrLineId = findPurchaseOrderPrLineSelectorUseCase
                .execute(domain.getPrId(), null, null, PageRequest.of(0, 1000))
                .getContent().stream()
                .collect(Collectors.toMap(
                        PurchaseOrderPrLineSelectorRow::prLineId,
                        PurchaseOrderPrLineSelectorRow::remainingQuantity
                ));

        saveRequest.getLines().forEach(line -> {
            if (line.getPrLineId() != null) {
                line.setMaxQuantity(remainingByPrLineId.get(line.getPrLineId()));
            }
        });
    }
}
