package com.solusi.erp.accountspayable.debitmemo.web.controller;

import com.solusi.erp.accountspayable.debitmemo.application.usecase.command.CancelDebitMemoUseCase;
import com.solusi.erp.accountspayable.debitmemo.application.usecase.command.UpdateDebitMemoMetadataUseCase;
import com.solusi.erp.accountspayable.debitmemo.application.usecase.query.DebitMemoDetailView;
import com.solusi.erp.accountspayable.debitmemo.application.usecase.query.DebitMemoSummaryView;
import com.solusi.erp.accountspayable.debitmemo.application.usecase.query.FindDebitMemosUseCase;
import com.solusi.erp.accountspayable.debitmemo.application.usecase.query.GetDebitMemoDetailUseCase;
import com.solusi.erp.accountspayable.debitmemo.domain.model.DebitMemoSettlementStatus;
import com.solusi.erp.accountspayable.debitmemoallocation.application.usecase.query.FindDebitMemoAllocationHistoryUseCase;
import com.solusi.erp.accountspayable.debitmemo.web.dto.DebitMemoDetailResponse;
import com.solusi.erp.accountspayable.debitmemo.web.dto.DebitMemoMetadataRequest;
import com.solusi.erp.accountspayable.debitmemo.web.dto.DebitMemoSummaryResponse;
import com.solusi.erp.accountspayable.debitmemo.web.mapper.DebitMemoWebMapper;
import com.solusi.erp.core.annotation.DefaultRedirectUrl;
import com.solusi.erp.core.domain.model.Pageable;
import com.solusi.erp.core.dto.ApiResponse;
import com.solusi.erp.core.dto.LookupDto;
import com.solusi.erp.core.infrastructure.util.PageableMapper;
import com.solusi.erp.master.currency.domain.port.CurrencyLookupProvider;
import com.solusi.erp.master.party.domain.port.PartyLookupProvider;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.PageImpl;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/accounts-payable/debit-memos")
@RequiredArgsConstructor
@DefaultRedirectUrl
public class DebitMemoController {

    private final FindDebitMemosUseCase findDebitMemosUseCase;
    private final GetDebitMemoDetailUseCase getDebitMemoDetailUseCase;
    private final UpdateDebitMemoMetadataUseCase updateDebitMemoMetadataUseCase;
    private final CancelDebitMemoUseCase cancelDebitMemoUseCase;
    private final FindDebitMemoAllocationHistoryUseCase findDebitMemoAllocationHistoryUseCase;
    private final DebitMemoWebMapper webMapper;
    private final PartyLookupProvider partyLookupProvider;
    private final CurrencyLookupProvider currencyLookupProvider;
    private final MessageSource messageSource;

    @GetMapping
    @PreAuthorize("hasAuthority('DEBIT-MEMO_READ')")
    public String list(@RequestParam(required = false) String keyword,
                       @RequestParam(required = false) Long vendorId,
                       @RequestParam(required = false) DebitMemoSettlementStatus settlementStatus,
                       @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate memoDateFrom,
                       @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate memoDateTo,
                       org.springframework.data.domain.Pageable springPageable,
                       Model model) {
        Pageable domainPageable = PageableMapper.toDomain(springPageable);
        com.solusi.erp.core.domain.model.Page<DebitMemoSummaryView> domainPage =
                findDebitMemosUseCase.execute(keyword, vendorId, settlementStatus, memoDateFrom, memoDateTo, domainPageable);
        List<DebitMemoSummaryResponse> content = domainPage.content().stream()
                .map(webMapper::toSummaryResponse)
                .peek(this::enrichSummaryDisplay)
                .toList();

        model.addAttribute("page", new PageImpl<>(content, springPageable, domainPage.totalElements()));
        model.addAttribute("keyword", keyword);
        model.addAttribute("vendorId", vendorId);
        addVendorFilterDisplay(model, vendorId);
        model.addAttribute("settlementStatus", settlementStatus);
        model.addAttribute("memoDateFrom", memoDateFrom);
        model.addAttribute("memoDateTo", memoDateTo);
        model.addAttribute("settlementStatuses", DebitMemoSettlementStatus.values());
        return "accountspayable/debit-memos/list";
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('DEBIT-MEMO_READ')")
    public String detail(@PathVariable Long id, Model model) {
        DebitMemoDetailResponse response = webMapper.toDetailResponse(getDebitMemoDetailUseCase.execute(id));
        model.addAttribute("debitMemo", response);
        model.addAttribute("metadataRequest", toMetadataRequest(response));
        model.addAttribute("debitMemoAllocationHistory", findDebitMemoAllocationHistoryUseCase.byDebitMemoId(id));
        return "accountspayable/debit-memos/detail";
    }

    @PostMapping("/{id}/metadata")
    @ResponseBody
    @PreAuthorize("hasAuthority('DEBIT-MEMO_UPDATE-METADATA')")
    public ResponseEntity<ApiResponse<DebitMemoDetailResponse>> updateMetadata(
            @PathVariable Long id,
            @Valid @RequestBody DebitMemoMetadataRequest request) {
        updateDebitMemoMetadataUseCase.execute(
                id,
                request.getSupplierMemoNumber(),
                request.getSupplierMemoDate(),
                request.getTaxDocumentNumber(),
                request.getTaxDocumentDate(),
                request.getNotes()
        );
        DebitMemoDetailView detail = getDebitMemoDetailUseCase.execute(id);
        return ResponseEntity.ok(ApiResponse.success(
                message("msg.success.debit-memo.metadata-updated"),
                webMapper.toDetailResponse(detail)
        ));
    }

    @PostMapping("/{id}/cancel")
    @ResponseBody
    @PreAuthorize("hasAuthority('DEBIT-MEMO_CANCEL')")
    public ResponseEntity<ApiResponse<DebitMemoDetailResponse>> cancel(@PathVariable Long id) {
        cancelDebitMemoUseCase.execute(id);
        DebitMemoDetailView detail = getDebitMemoDetailUseCase.execute(id);
        return ResponseEntity.ok(ApiResponse.success(
                message("msg.success.debit-memo.cancelled"),
                webMapper.toDetailResponse(detail)
        ));
    }

    private DebitMemoMetadataRequest toMetadataRequest(DebitMemoDetailResponse response) {
        DebitMemoMetadataRequest request = new DebitMemoMetadataRequest();
        request.setSupplierMemoNumber(response.getSupplierMemoNumber());
        request.setSupplierMemoDate(response.getSupplierMemoDate());
        request.setTaxDocumentNumber(response.getTaxDocumentNumber());
        request.setTaxDocumentDate(response.getTaxDocumentDate());
        request.setNotes(response.getNotes());
        return request;
    }

    private void enrichSummaryDisplay(DebitMemoSummaryResponse response) {
        LookupDto vendor = partyLookupProvider.resolve(response.getVendorId());
        if (vendor != null) {
            response.setVendorName(vendor.name());
            response.setVendorCode(vendor.subText());
        }
        LookupDto currency = currencyLookupProvider.resolve(response.getCurrencyId());
        if (currency != null) {
            response.setCurrencyCode(currency.subText());
        }
    }

    private void addVendorFilterDisplay(Model model, Long vendorId) {
        LookupDto vendor = partyLookupProvider.resolve(vendorId);
        if (vendor != null) {
            model.addAttribute("vendorText", vendor.name());
            model.addAttribute("vendorSubtext", vendor.subText());
        }
    }

    private String message(String key) {
        return messageSource.getMessage(key, null, LocaleContextHolder.getLocale());
    }
}
