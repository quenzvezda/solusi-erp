package com.solusi.erp.inventory.goodsreceipt.web.controller;

import com.solusi.erp.core.annotation.DefaultRedirectUrl;
import com.solusi.erp.core.domain.model.Page;
import com.solusi.erp.core.domain.model.Pageable;
import com.solusi.erp.core.dto.ApiResponse;
import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.core.infrastructure.util.PageableMapper;
import com.solusi.erp.inventory.goodsreceipt.application.usecase.command.*;
import com.solusi.erp.inventory.goodsreceipt.application.usecase.query.*;
import com.solusi.erp.inventory.goodsreceipt.domain.model.GoodsReceipt;
import com.solusi.erp.inventory.goodsreceipt.domain.model.GoodsReceiptReferenceType;
import com.solusi.erp.inventory.goodsreceipt.domain.port.GoodsReceiptReferenceLookupProvider;
import com.solusi.erp.accountspayable.vendorbill.domain.port.BillableGrQueryPort;
import com.solusi.erp.inventory.goodsreceipt.web.dto.*;
import com.solusi.erp.inventory.goodsreceipt.web.mapper.GoodsReceiptWebMapper;
import com.solusi.erp.util.HtmxResponseUtility;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/inventory/goods-receipts")
@RequiredArgsConstructor
@DefaultRedirectUrl
public class GoodsReceiptController {

    private final CreateGoodsReceiptUseCase createGoodsReceiptUseCase;
    private final UpdateGoodsReceiptUseCase updateGoodsReceiptUseCase;
    private final DeleteGoodsReceiptUseCase deleteGoodsReceiptUseCase;
    private final CompleteGoodsReceiptUseCase completeGoodsReceiptUseCase;
    private final FindGoodsReceiptsUseCase findGoodsReceiptsUseCase;
    private final GetGoodsReceiptUseCase getGoodsReceiptUseCase;
    private final GetGoodsReceiptEditViewUseCase getGoodsReceiptEditViewUseCase;
    private final GetGoodsReceiptCreateViewUseCase getGoodsReceiptCreateViewUseCase;
    private final GoodsReceiptWebMapper webMapper;
    private final MessageSource messageSource;
    private final GoodsReceiptReferenceLookupProvider referenceLookupProvider;
    private final BillableGrQueryPort billableGrQueryPort;

    @GetMapping
    @PreAuthorize("hasAuthority('GOODS-RECEIPT_READ')")
    public String list(@RequestParam(required = false) String keyword,
                       @RequestParam(required = false) GoodsReceiptReferenceType referenceType,
                       @RequestParam(required = false) Long referenceId,
                       @RequestParam(name = "poId", required = false) Long poId,
                       org.springframework.data.domain.Pageable springPageable,
                       Model model) {
        ReferenceFilter filter = canonicalizeReferenceFilter(referenceType, referenceId, poId);
        Pageable domainPageable = PageableMapper.toDomain(springPageable);
        Page<GoodsReceipt> domainPage = findGoodsReceiptsUseCase.execute(
            keyword,
            filter.referenceType(),
            filter.referenceId(),
            domainPageable
        );

        List<GoodsReceiptSummaryResponse> content = domainPage.content().stream()
            .map(webMapper::toSummaryResponse)
            .collect(Collectors.toList());

        org.springframework.data.domain.Page<GoodsReceiptSummaryResponse> springPage =
            new PageImpl<>(content, springPageable, domainPage.totalElements());
        model.addAttribute("page", springPage);
        model.addAttribute("keyword", keyword);
        model.addAttribute("activeReferenceType", filter.referenceType() != null ? filter.referenceType().name() : null);
        model.addAttribute("activeReferenceId", filter.referenceId());
        model.addAttribute("activeReferenceCode", resolveReferenceCode(filter.referenceType(), filter.referenceId()));
        return "inventory/goods-receipts/list";
    }

    @GetMapping("/create")
    @PreAuthorize("hasAuthority('GOODS-RECEIPT_CREATE')")
    public String createForm(@RequestParam(required = false) GoodsReceiptReferenceType referenceType,
                             @RequestParam(required = false) Long referenceId,
                             @RequestParam(name = "poId", required = false) Long poId,
                             Model model) {
        ReferenceFilter filter = canonicalizeReferenceFilter(referenceType, referenceId, poId);
        GoodsReceipt draftGr = getGoodsReceiptCreateViewUseCase.execute(filter.referenceType(), filter.referenceId());
        GoodsReceiptSaveRequest request = webMapper.toSaveRequest(draftGr);
        model.addAttribute("grRequest", request);
        return "inventory/goods-receipts/form";
    }

    @GetMapping("/selectors/purchase-order-lines")
    @PreAuthorize("hasAnyAuthority('GOODS-RECEIPT_CREATE', 'GOODS-RECEIPT_UPDATE')")
    public String showPurchaseOrderLineSelector(@RequestParam GoodsReceiptReferenceType referenceType,
                                                 @RequestParam Long referenceId,
                                                 @RequestParam(required = false) String keyword,
                                                 @RequestParam(required = false) List<Long> excludeReferenceLineIds,
                                                 org.springframework.data.domain.Pageable springPageable,
                                                 Model model) {
        validateCreateReferenceType(referenceType);

        GoodsReceipt draft = getGoodsReceiptCreateViewUseCase.execute(referenceType, referenceId);
        Map<Long, GoodsReceiptReferenceLookupProvider.ReferenceLineSnapshot> snapshotsFromLookup =
            referenceLookupProvider.resolveReferenceLineSnapshots(referenceType, referenceId);
        final Map<Long, GoodsReceiptReferenceLookupProvider.ReferenceLineSnapshot> lineSnapshots =
            snapshotsFromLookup != null ? snapshotsFromLookup : Map.of();

        List<GoodsReceiptSaveLineRequest> filteredLines = draft.getLines().stream()
            .map(webMapper::toSaveLineRequest)
            .peek(line -> applyReferenceSnapshot(line, lineSnapshots.get(line.getReferenceLineId())))
            .filter(line -> excludeReferenceLineIds == null
                || line.getReferenceLineId() == null
                || !excludeReferenceLineIds.contains(line.getReferenceLineId()))
            .filter(line -> {
                if (keyword == null || keyword.isBlank()) {
                    return true;
                }
                String normalizedKeyword = keyword.toLowerCase();
                String productName = line.getProductName() != null ? line.getProductName().toLowerCase() : "";
                String productCode = line.getProductCode() != null ? line.getProductCode().toLowerCase() : "";
                return productName.contains(normalizedKeyword) || productCode.contains(normalizedKeyword);
            })
            .collect(Collectors.toList());

        int start = (int) springPageable.getOffset();
        int end = Math.min(start + springPageable.getPageSize(), filteredLines.size());
        List<GoodsReceiptSaveLineRequest> pageContent = start >= filteredLines.size()
            ? List.of()
            : filteredLines.subList(start, end);

        model.addAttribute("page", new PageImpl<>(pageContent, springPageable, filteredLines.size()));
        model.addAttribute("keyword", keyword);
        model.addAttribute("referenceType", referenceType);
        model.addAttribute("referenceId", referenceId);
        model.addAttribute("excludeReferenceLineIds", excludeReferenceLineIds == null ? List.of() : excludeReferenceLineIds);
        return "inventory/goods-receipts/fragments/po-line-selector-modal";
    }

    private ReferenceFilter canonicalizeReferenceFilter(GoodsReceiptReferenceType referenceType,
                                                        Long referenceId,
                                                        Long poId) {
        if (referenceType == null && referenceId == null && poId != null) {
            return new ReferenceFilter(GoodsReceiptReferenceType.PURCHASE_ORDER, poId);
        }
        return new ReferenceFilter(referenceType, referenceId);
    }

    private String resolveReferenceCode(GoodsReceiptReferenceType referenceType, Long referenceId) {
        if (referenceType == null || referenceId == null) {
            return null;
        }
        return referenceLookupProvider.resolveReferenceCode(referenceType, referenceId);
    }

    @GetMapping("/edit/{id}")
    @PreAuthorize("hasAuthority('GOODS-RECEIPT_UPDATE')")
    public String editForm(@PathVariable Long id, Model model) {
        GoodsReceipt domain = getGoodsReceiptEditViewUseCase.execute(id)
            .orElseThrow(() -> new RuntimeException("Goods receipt not found"));
        GoodsReceiptSaveRequest saveRequest = webMapper.toSaveRequest(domain);
        model.addAttribute("grRequest", saveRequest);
        return "inventory/goods-receipts/form";
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('GOODS-RECEIPT_READ')")
    public String view(@PathVariable Long id, Model model) {
        GoodsReceipt domain = getGoodsReceiptUseCase.execute(id)
            .orElseThrow(() -> new RuntimeException("Goods receipt not found"));
        GoodsReceiptDetailResponse response = webMapper.toDetailResponse(domain);
        enrichBillingStatus(id, response);
        model.addAttribute("gr", response);
        return "inventory/goods-receipts/view";
    }

    private void enrichBillingStatus(Long goodsReceiptId, GoodsReceiptDetailResponse response) {
        if (response == null || response.getLines() == null || response.getLines().isEmpty()) {
            return;
        }
        Map<Long, BigDecimal> billedQtyMap = billableGrQueryPort.sumConfirmedBilledQtyByGrId(goodsReceiptId);
        response.getLines().forEach(line -> {
            BigDecimal billedQty = billedQtyMap.getOrDefault(line.getId(), BigDecimal.ZERO);
            line.setBilledQuantity(billedQty);
            line.setBillingStatus(resolveBillingStatus(line.getQuantityReceived(), billedQty));
        });
    }

    private String resolveBillingStatus(BigDecimal receivedQty, BigDecimal billedQty) {
        if (billedQty == null || billedQty.compareTo(BigDecimal.ZERO) <= 0) {
            return "UNBILLED";
        }
        if (receivedQty != null && billedQty.compareTo(receivedQty) >= 0) {
            return "FULLY_BILLED";
        }
        return "PARTIAL_BILLED";
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('GOODS-RECEIPT_CREATE', 'GOODS-RECEIPT_UPDATE')")
    @ResponseBody
    public ResponseEntity<ApiResponse<GoodsReceiptDetailResponse>> save(
            @Valid @RequestBody GoodsReceiptSaveRequest request) {
        List<GoodsReceiptLineCommand> lines = webMapper.toLineCommands(request.getLines());

        GoodsReceipt domain;
        if (request.getId() == null) {
            validateCreateReferenceType(request.getReferenceType());
            domain = createGoodsReceiptUseCase.execute(
                request.getReceiptDate(),
                request.getReferenceId(),
                request.getNotes(),
                lines
            );
            GoodsReceiptDetailResponse data = webMapper.toDetailResponse(domain);
            String msg = messageSource.getMessage("msg.success.create", null, LocaleContextHolder.getLocale());
            return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(msg, data));
        } else {
            domain = updateGoodsReceiptUseCase.execute(
                request.getId(),
                request.getReceiptDate(),
                request.getNotes(),
                lines
            );
            GoodsReceiptDetailResponse data = webMapper.toDetailResponse(domain);
            String msg = messageSource.getMessage("msg.success.update", null, LocaleContextHolder.getLocale());
            return ResponseEntity.ok(ApiResponse.success(msg, data));
        }
    }

    private void applyReferenceSnapshot(
        GoodsReceiptSaveLineRequest line,
        GoodsReceiptReferenceLookupProvider.ReferenceLineSnapshot snapshot
    ) {
        if (line == null || snapshot == null) {
            return;
        }
        line.setOrderedQuantity(snapshot.orderedQuantity());
        line.setReceivedToDateQuantity(snapshot.receivedToDateQuantity());
        line.setRemainingQuantity(snapshot.remainingQuantity());
        if (line.getUnitPrice() == null) {
            line.setUnitPrice(snapshot.unitPrice());
        }
    }

    private void validateCreateReferenceType(GoodsReceiptReferenceType referenceType) {
        if (referenceType != GoodsReceiptReferenceType.PURCHASE_ORDER) {
            throw new DomainException("msg.error.gr.reference.unsupported");
        }
    }

    @PostMapping("/{id}/complete")
    @PreAuthorize("hasAuthority('GOODS-RECEIPT_COMPLETE')")
    @ResponseBody
    public ResponseEntity<ApiResponse<GoodsReceiptDetailResponse>> complete(@PathVariable Long id) {
        completeGoodsReceiptUseCase.execute(id);
        GoodsReceipt domain = getGoodsReceiptUseCase.execute(id)
            .orElseThrow(() -> new RuntimeException("Goods receipt not found"));
        GoodsReceiptDetailResponse data = webMapper.toDetailResponse(domain);
        String msg = messageSource.getMessage("msg.success.gr.completed", null, LocaleContextHolder.getLocale());
        return ResponseEntity.ok(ApiResponse.success(msg, data));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('GOODS-RECEIPT_DELETE')")
    public org.springframework.http.ResponseEntity<Void> delete(@PathVariable Long id) {
        deleteGoodsReceiptUseCase.execute(id);
        String msg = messageSource.getMessage("msg.success.delete", null, LocaleContextHolder.getLocale());
        return HtmxResponseUtility.okWithRefreshTableAndSuccess(msg);
    }

    private record ReferenceFilter(GoodsReceiptReferenceType referenceType, Long referenceId) {
    }
}
