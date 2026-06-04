package com.solusi.erp.inventory.goodsissue.web.controller;

import com.solusi.erp.core.annotation.DefaultRedirectUrl;
import com.solusi.erp.core.domain.model.Page;
import com.solusi.erp.core.domain.model.Pageable;
import com.solusi.erp.core.dto.ApiResponse;
import com.solusi.erp.core.infrastructure.util.PageableMapper;
import com.solusi.erp.inventory.goodsissue.application.usecase.command.CancelGoodsIssueUseCase;
import com.solusi.erp.inventory.goodsissue.application.usecase.command.CompleteGoodsIssueUseCase;
import com.solusi.erp.inventory.goodsissue.application.usecase.command.CreateGoodsIssueUseCase;
import com.solusi.erp.inventory.goodsissue.application.usecase.command.DeleteGoodsIssueUseCase;
import com.solusi.erp.inventory.goodsissue.application.usecase.command.GoodsIssueCancelCommand;
import com.solusi.erp.inventory.goodsissue.application.usecase.command.GoodsIssueCancelLineCommand;
import com.solusi.erp.inventory.goodsissue.application.usecase.command.GoodsIssueLineCommand;
import com.solusi.erp.inventory.goodsissue.application.usecase.command.UpdateGoodsIssueUseCase;
import com.solusi.erp.inventory.goodsissue.application.usecase.query.FindGoodsIssuesUseCase;
import com.solusi.erp.inventory.goodsissue.application.usecase.query.GetGoodsIssueCancelViewUseCase;
import com.solusi.erp.inventory.goodsissue.application.usecase.query.GetGoodsIssueCreateViewUseCase;
import com.solusi.erp.inventory.goodsissue.application.usecase.query.GetGoodsIssueEditViewUseCase;
import com.solusi.erp.inventory.goodsissue.application.usecase.query.GetGoodsIssueUseCase;
import com.solusi.erp.inventory.goodsissue.domain.model.GoodsIssue;
import com.solusi.erp.inventory.goodsissue.domain.model.GoodsIssueReferenceType;
import com.solusi.erp.inventory.goodsissue.domain.port.GoodsIssueReferenceLookupProvider;
import com.solusi.erp.inventory.goodsissue.web.dto.GoodsIssueCancelLineRequest;
import com.solusi.erp.inventory.goodsissue.web.dto.GoodsIssueCancelRequest;
import com.solusi.erp.inventory.goodsissue.web.dto.GoodsIssueDetailResponse;
import com.solusi.erp.inventory.goodsissue.web.dto.GoodsIssueSaveRequest;
import com.solusi.erp.inventory.goodsissue.web.dto.GoodsIssueSummaryResponse;
import com.solusi.erp.inventory.goodsissue.web.mapper.GoodsIssueWebMapper;
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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/inventory/goods-issues")
@RequiredArgsConstructor
@DefaultRedirectUrl
public class GoodsIssueController {

    private final CreateGoodsIssueUseCase createGoodsIssueUseCase;
    private final UpdateGoodsIssueUseCase updateGoodsIssueUseCase;
    private final DeleteGoodsIssueUseCase deleteGoodsIssueUseCase;
    private final CompleteGoodsIssueUseCase completeGoodsIssueUseCase;
    private final CancelGoodsIssueUseCase cancelGoodsIssueUseCase;
    private final FindGoodsIssuesUseCase findGoodsIssuesUseCase;
    private final GetGoodsIssueUseCase getGoodsIssueUseCase;
    private final GetGoodsIssueEditViewUseCase getGoodsIssueEditViewUseCase;
    private final GetGoodsIssueCreateViewUseCase getGoodsIssueCreateViewUseCase;
    private final GetGoodsIssueCancelViewUseCase getGoodsIssueCancelViewUseCase;
    private final GoodsIssueReferenceLookupProvider referenceLookupProvider;
    private final GoodsIssueWebMapper webMapper;
    private final MessageSource messageSource;

    @GetMapping
    @PreAuthorize("hasAuthority('GOODS-ISSUE_READ')")
    public String list(@RequestParam(required = false) String keyword,
                       @RequestParam(required = false) GoodsIssueReferenceType referenceType,
                       @RequestParam(required = false) Long referenceId,
                       org.springframework.data.domain.Pageable springPageable,
                       Model model) {
        Pageable domainPageable = PageableMapper.toDomain(springPageable);
        Page<GoodsIssue> domainPage = findGoodsIssuesUseCase.execute(keyword, referenceType, referenceId, domainPageable);
        List<GoodsIssueSummaryResponse> content = domainPage.content().stream()
                .map(webMapper::toSummaryResponse)
                .collect(Collectors.toList());
        model.addAttribute("page", new PageImpl<>(content, springPageable, domainPage.totalElements()));
        model.addAttribute("keyword", keyword);
        model.addAttribute("activeReferenceType", referenceType != null ? referenceType.name() : null);
        model.addAttribute("activeReferenceId", referenceId);
        model.addAttribute("activeReferenceCode", content.stream()
                .map(GoodsIssueSummaryResponse::getReferenceCode)
                .filter(code -> code != null && !code.isBlank())
                .findFirst()
                .orElse(null));
        return "inventory/goods-issues/list";
    }

    @GetMapping("/create")
    @PreAuthorize("hasAuthority('GOODS-ISSUE_CREATE')")
    public String createForm(@RequestParam(required = false) GoodsIssueReferenceType referenceType,
                             @RequestParam(required = false) Long referenceId,
                             Model model) {
        GoodsIssue draft = getGoodsIssueCreateViewUseCase.execute(referenceType, referenceId);
        model.addAttribute("giRequest", webMapper.toSaveRequest(draft));
        return "inventory/goods-issues/form";
    }

    @GetMapping("/selectors/source-lines")
    @PreAuthorize("hasAnyAuthority('GOODS-ISSUE_CREATE', 'GOODS-ISSUE_UPDATE')")
    public String showSourceLineSelector(@RequestParam(required = false) GoodsIssueReferenceType referenceType,
                                         @RequestParam(required = false) Long referenceId,
                                         @RequestParam(required = false) String keyword,
                                         @RequestParam(required = false) List<Long> excludeReferenceLineIds,
                                         org.springframework.data.domain.Pageable springPageable,
                                         Model model) {
        List<Long> excludedLineIds = excludeReferenceLineIds == null ? List.of() : excludeReferenceLineIds;
        boolean supportedReferenceType = referenceLookupProvider.supportsSourceLineSelector(referenceType);

        List<GoodsIssueReferenceLookupProvider.SourceLineSelectorRow> sourceRows = supportedReferenceType
                ? referenceLookupProvider.resolveSourceLineSelectorRows(referenceType, referenceId)
                : List.of();

        List<GoodsIssueReferenceLookupProvider.SourceLineSelectorRow> filteredRows = sourceRows.stream()
                .filter(row -> row.referenceLineId() == null || !excludedLineIds.contains(row.referenceLineId()))
                .filter(this::hasRemainingQuantity)
                .filter(row -> matchesKeyword(row, keyword))
                .collect(Collectors.toList());

        int start = (int) springPageable.getOffset();
        int end = Math.min(start + springPageable.getPageSize(), filteredRows.size());
        List<GoodsIssueReferenceLookupProvider.SourceLineSelectorRow> pageContent = start >= filteredRows.size()
                ? List.of()
                : filteredRows.subList(start, end);

        model.addAttribute("page", new PageImpl<>(pageContent, springPageable, filteredRows.size()));
        model.addAttribute("keyword", keyword);
        model.addAttribute("referenceType", referenceType);
        model.addAttribute("referenceId", referenceId);
        model.addAttribute("excludeReferenceLineIds", excludedLineIds);
        model.addAttribute("unsupportedReferenceType", !supportedReferenceType);
        return "inventory/goods-issues/fragments/source-line-selector-modal";
    }

    @GetMapping("/edit/{id}")
    @PreAuthorize("hasAuthority('GOODS-ISSUE_UPDATE')")
    public String editForm(@PathVariable Long id, Model model) {
        GoodsIssue domain = getGoodsIssueEditViewUseCase.execute(id)
                .orElseThrow(() -> new RuntimeException("Goods issue not found"));
        model.addAttribute("giRequest", webMapper.toSaveRequest(domain));
        return "inventory/goods-issues/form";
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('GOODS-ISSUE_READ')")
    public String view(@PathVariable Long id, Model model) {
        GoodsIssue domain = getGoodsIssueUseCase.execute(id)
                .orElseThrow(() -> new RuntimeException("Goods issue not found"));
        model.addAttribute("gi", webMapper.toDetailResponse(domain));
        return "inventory/goods-issues/view";
    }

    @GetMapping("/{id}/cancel")
    @PreAuthorize("hasAuthority('GOODS-ISSUE_CANCEL')")
    public String cancelForm(@PathVariable Long id, Model model) {
        var cancelView = getGoodsIssueCancelViewUseCase.execute(id);
        model.addAttribute("cancelView", cancelView);
        model.addAttribute("cancelRequest", GoodsIssueCancelRequest.from(cancelView));
        return "inventory/goods-issues/cancel";
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('GOODS-ISSUE_CREATE', 'GOODS-ISSUE_UPDATE')")
    @ResponseBody
    public ResponseEntity<ApiResponse<GoodsIssueDetailResponse>> save(@Valid @RequestBody GoodsIssueSaveRequest request) {
        List<GoodsIssueLineCommand> lines = webMapper.toLineCommands(request.getLines());
        GoodsIssue domain;
        if (request.getId() == null) {
            domain = createGoodsIssueUseCase.execute(
                    request.getIssueDate(),
                    request.getReferenceType(),
                    request.getReferenceId(),
                    request.getReferenceCode(),
                    request.getPartyId(),
                    request.getPartyType(),
                    request.getFacilityId(),
                    request.getCurrencyId(),
                    request.getExchangeRate(),
                    request.getNotes(),
                    lines
            );
            String msg = messageSource.getMessage("msg.success.gi.created", null, LocaleContextHolder.getLocale());
            return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(msg, webMapper.toDetailResponse(domain)));
        }
        domain = updateGoodsIssueUseCase.execute(request.getId(), request.getIssueDate(), request.getNotes(), lines);
        String msg = messageSource.getMessage("msg.success.gi.updated", null, LocaleContextHolder.getLocale());
        return ResponseEntity.ok(ApiResponse.success(msg, webMapper.toDetailResponse(domain)));
    }

    @PostMapping("/{id}/complete")
    @PreAuthorize("hasAuthority('GOODS-ISSUE_COMPLETE')")
    @ResponseBody
    public ResponseEntity<ApiResponse<GoodsIssueDetailResponse>> complete(@PathVariable Long id) {
        completeGoodsIssueUseCase.execute(id);
        GoodsIssue domain = getGoodsIssueUseCase.execute(id)
                .orElseThrow(() -> new RuntimeException("Goods issue not found"));
        String msg = messageSource.getMessage("msg.success.gi.completed", null, LocaleContextHolder.getLocale());
        return ResponseEntity.ok(ApiResponse.success(msg, webMapper.toDetailResponse(domain)));
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAuthority('GOODS-ISSUE_CANCEL')")
    @ResponseBody
    public ResponseEntity<ApiResponse<GoodsIssueDetailResponse>> cancel(@PathVariable Long id,
                                                                        @Valid @RequestBody GoodsIssueCancelRequest request) {
        cancelGoodsIssueUseCase.execute(new GoodsIssueCancelCommand(
                id,
                request.getReversalDate(),
                request.getReason(),
                toCancelLineCommands(request.getLines())
        ));
        GoodsIssue domain = getGoodsIssueUseCase.execute(id)
                .orElseThrow(() -> new RuntimeException("Goods issue not found"));
        String msg = messageSource.getMessage("msg.success.gi.cancelled", null, LocaleContextHolder.getLocale());
        return ResponseEntity.ok(ApiResponse.success(msg, webMapper.toDetailResponse(domain)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('GOODS-ISSUE_DELETE')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        deleteGoodsIssueUseCase.execute(id);
        String msg = messageSource.getMessage("msg.success.gi.deleted", null, LocaleContextHolder.getLocale());
        return HtmxResponseUtility.okWithRefreshTableAndSuccess(msg);
    }

    private List<GoodsIssueCancelLineCommand> toCancelLineCommands(List<GoodsIssueCancelLineRequest> lines) {
        if (lines == null) {
            return List.of();
        }
        return lines.stream()
                .map(line -> new GoodsIssueCancelLineCommand(
                        line.getGoodsIssueLineId(),
                        line.getOriginalMovementId(),
                        line.getTargetContainerId(),
                        line.getProductLabel(),
                        line.getSerialNumber(),
                        line.getQuantityIssued()))
                .toList();
    }

    private boolean hasRemainingQuantity(GoodsIssueReferenceLookupProvider.SourceLineSelectorRow row) {
        return row.remainingQuantity() != null && row.remainingQuantity().compareTo(BigDecimal.ZERO) > 0;
    }

    private boolean matchesKeyword(GoodsIssueReferenceLookupProvider.SourceLineSelectorRow row, String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return true;
        }
        String normalizedKeyword = keyword.toLowerCase();
        String productName = row.productName() != null ? row.productName().toLowerCase() : "";
        String productCode = row.productCode() != null ? row.productCode().toLowerCase() : "";
        return productName.contains(normalizedKeyword) || productCode.contains(normalizedKeyword);
    }
}
