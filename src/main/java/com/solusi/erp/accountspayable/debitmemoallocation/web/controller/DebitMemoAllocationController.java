package com.solusi.erp.accountspayable.debitmemoallocation.web.controller;

import com.solusi.erp.accountspayable.debitmemoallocation.application.usecase.command.*;
import com.solusi.erp.accountspayable.debitmemoallocation.application.usecase.query.*;
import com.solusi.erp.accountspayable.debitmemoallocation.domain.model.DebitMemoAllocationStatus;
import com.solusi.erp.accountspayable.debitmemoallocation.web.dto.*;
import com.solusi.erp.accountspayable.debitmemoallocation.web.mapper.DebitMemoAllocationWebMapper;
import com.solusi.erp.core.annotation.DefaultRedirectUrl;
import com.solusi.erp.core.domain.model.Pageable;
import com.solusi.erp.core.dto.ApiResponse;
import com.solusi.erp.core.infrastructure.util.PageableMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.PageImpl;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/accounts-payable/debit-memo-allocations")
@RequiredArgsConstructor
@DefaultRedirectUrl
public class DebitMemoAllocationController {

    private final FindDebitMemoAllocationsUseCase findDebitMemoAllocationsUseCase;
    private final GetDebitMemoAllocationDetailUseCase getDebitMemoAllocationDetailUseCase;
    private final CreateDebitMemoAllocationUseCase createDebitMemoAllocationUseCase;
    private final UpdateDebitMemoAllocationUseCase updateDebitMemoAllocationUseCase;
    private final CancelDebitMemoAllocationUseCase cancelDebitMemoAllocationUseCase;
    private final ConfirmDebitMemoAllocationUseCase confirmDebitMemoAllocationUseCase;
    private final ReverseDebitMemoAllocationUseCase reverseDebitMemoAllocationUseCase;
    private final DebitMemoAllocationSelectorUseCase selectorUseCase;
    private final DebitMemoAllocationWebMapper webMapper;
    private final MessageSource messageSource;

    @GetMapping
    @PreAuthorize("hasAuthority('DEBIT-MEMO-ALLOCATION_READ')")
    public String list(@RequestParam(required = false) String keyword,
                       @RequestParam(required = false) Long debitMemoId,
                       @RequestParam(required = false) DebitMemoAllocationStatus status,
                       @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate allocationDateFrom,
                       @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate allocationDateTo,
                       org.springframework.data.domain.Pageable springPageable,
                       Model model) {
        Pageable domainPageable = PageableMapper.toDomain(springPageable);
        com.solusi.erp.core.domain.model.Page<DebitMemoAllocationSummaryView> domainPage =
                findDebitMemoAllocationsUseCase.execute(keyword, debitMemoId, status,
                        allocationDateFrom, allocationDateTo, domainPageable);
        List<DebitMemoAllocationSummaryResponse> content = domainPage.content().stream()
                .map(webMapper::toSummaryResponse)
                .toList();

        model.addAttribute("page", new PageImpl<>(content, springPageable, domainPage.totalElements()));
        model.addAttribute("keyword", keyword);
        model.addAttribute("debitMemoId", debitMemoId);
        model.addAttribute("status", status);
        model.addAttribute("allocationDateFrom", allocationDateFrom);
        model.addAttribute("allocationDateTo", allocationDateTo);
        model.addAttribute("statuses", DebitMemoAllocationStatus.values());
        return "accountspayable/debit-memo-allocations/list";
    }

    @GetMapping("/create")
    @PreAuthorize("hasAuthority('DEBIT-MEMO-ALLOCATION_CREATE')")
    public String createForm(@RequestParam(required = false) Long debitMemoId,
                             @RequestParam(required = false) String debitMemoCode,
                             @RequestParam(required = false) Long vendorBillId,
                             Model model) {
        DebitMemoAllocationSaveRequest request = webMapper.newSaveRequest(debitMemoId, debitMemoCode, vendorBillId);
        model.addAttribute("request", request);
        model.addAttribute("form", new DebitMemoAllocationFormView(request, null, false));
        return "accountspayable/debit-memo-allocations/form";
    }

    @GetMapping("/edit/{id}")
    @PreAuthorize("hasAuthority('DEBIT-MEMO-ALLOCATION_UPDATE')")
    public String editForm(@PathVariable Long id, Model model) {
        DebitMemoAllocationDetailView detail = getDebitMemoAllocationDetailUseCase.execute(id);
        DebitMemoAllocationSaveRequest request = webMapper.toSaveRequest(detail);
        model.addAttribute("request", request);
        model.addAttribute("form", new DebitMemoAllocationFormView(
                request,
                webMapper.toDetailResponse(detail),
                true
        ));
        return "accountspayable/debit-memo-allocations/form";
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('DEBIT-MEMO-ALLOCATION_READ')")
    public String detail(@PathVariable Long id, Model model) {
        DebitMemoAllocationDetailView detail = getDebitMemoAllocationDetailUseCase.execute(id);
        model.addAttribute("allocation", webMapper.toDetailResponse(detail));
        model.addAttribute("reverseRequest", new DebitMemoAllocationReverseRequest());
        return "accountspayable/debit-memo-allocations/detail";
    }

    @PostMapping
    @ResponseBody
    @PreAuthorize("hasAuthority('DEBIT-MEMO-ALLOCATION_CREATE')")
    public ResponseEntity<ApiResponse<DebitMemoAllocationDetailResponse>> create(
            @Valid @RequestBody DebitMemoAllocationSaveRequest request) {
        DebitMemoAllocationDetailView detail = createDebitMemoAllocationUseCase.execute(webMapper.toCreateCommand(request));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(message("msg.success.debit-memo-allocation.created"),
                        webMapper.toDetailResponse(detail)));
    }

    @PutMapping("/{id}")
    @ResponseBody
    @PreAuthorize("hasAuthority('DEBIT-MEMO-ALLOCATION_UPDATE')")
    public ResponseEntity<ApiResponse<DebitMemoAllocationDetailResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody DebitMemoAllocationSaveRequest request) {
        DebitMemoAllocationDetailView detail =
                updateDebitMemoAllocationUseCase.execute(webMapper.toUpdateCommand(id, request));
        return ResponseEntity.ok(ApiResponse.success(message("msg.success.debit-memo-allocation.updated"),
                webMapper.toDetailResponse(detail)));
    }

    @PostMapping("/{id}/confirm")
    @ResponseBody
    @PreAuthorize("hasAuthority('DEBIT-MEMO-ALLOCATION_CONFIRM')")
    public ResponseEntity<ApiResponse<DebitMemoAllocationDetailResponse>> confirm(@PathVariable Long id) {
        confirmDebitMemoAllocationUseCase.execute(id);
        DebitMemoAllocationDetailView detail = getDebitMemoAllocationDetailUseCase.execute(id);
        return ResponseEntity.ok(ApiResponse.success(message("msg.success.debit-memo-allocation.confirmed"),
                webMapper.toDetailResponse(detail)));
    }

    @PostMapping("/{id}/cancel")
    @ResponseBody
    @PreAuthorize("hasAuthority('DEBIT-MEMO-ALLOCATION_CANCEL')")
    public ResponseEntity<ApiResponse<Void>> cancel(@PathVariable Long id) {
        cancelDebitMemoAllocationUseCase.execute(id);
        return ResponseEntity.ok(ApiResponse.success(message("msg.success.debit-memo-allocation.cancelled"), null));
    }

    @PostMapping("/{id}/reverse")
    @ResponseBody
    @PreAuthorize("hasAuthority('DEBIT-MEMO-ALLOCATION_REVERSE')")
    public ResponseEntity<ApiResponse<DebitMemoAllocationDetailResponse>> reverse(
            @PathVariable Long id,
            @Valid @RequestBody DebitMemoAllocationReverseRequest request) {
        reverseDebitMemoAllocationUseCase.execute(webMapper.toReverseCommand(id, request));
        DebitMemoAllocationDetailView detail = getDebitMemoAllocationDetailUseCase.execute(id);
        return ResponseEntity.ok(ApiResponse.success(message("msg.success.debit-memo-allocation.reversed"),
                webMapper.toDetailResponse(detail)));
    }

    @GetMapping("/selectors/vendor-bills")
    @PreAuthorize("hasAuthority('DEBIT-MEMO-ALLOCATION_CREATE')")
    public String eligibleVendorBills(@RequestParam Long debitMemoId,
                                      @RequestParam(required = false, name = "q") String keyword,
                                      org.springframework.data.domain.Pageable springPageable,
                                      Model model) {
        Pageable domainPageable = PageableMapper.toDomain(springPageable);
        var domainPage = selectorUseCase.eligibleVendorBills(debitMemoId, keyword, domainPageable);
        List<EligibleVendorBillSelectorRow> rows = domainPage.content().stream()
                .map(webMapper::toVendorBillSelectorRow)
                .toList();
        model.addAttribute("page", new PageImpl<>(rows, springPageable, domainPage.totalElements()));
        model.addAttribute("q", keyword);
        model.addAttribute("debitMemoId", debitMemoId);
        return "accountspayable/debit-memo-allocations/fragments/vendor-bill-selector";
    }

    @GetMapping("/selectors/debit-memos")
    @PreAuthorize("hasAuthority('DEBIT-MEMO-ALLOCATION_CREATE')")
    public String eligibleDebitMemos(@RequestParam Long vendorBillId,
                                     @RequestParam(required = false, name = "q") String keyword,
                                     org.springframework.data.domain.Pageable springPageable,
                                     Model model) {
        Pageable domainPageable = PageableMapper.toDomain(springPageable);
        var domainPage = selectorUseCase.eligibleDebitMemos(vendorBillId, keyword, domainPageable);
        List<EligibleDebitMemoSelectorRow> rows = domainPage.content().stream()
                .map(webMapper::toDebitMemoSelectorRow)
                .toList();
        model.addAttribute("page", new PageImpl<>(rows, springPageable, domainPage.totalElements()));
        model.addAttribute("q", keyword);
        model.addAttribute("vendorBillId", vendorBillId);
        return "accountspayable/debit-memo-allocations/fragments/debit-memo-selector";
    }

    private String message(String key) {
        return messageSource.getMessage(key, null, LocaleContextHolder.getLocale());
    }
}
