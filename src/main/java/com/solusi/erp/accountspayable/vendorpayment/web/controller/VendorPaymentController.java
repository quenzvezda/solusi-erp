package com.solusi.erp.accountspayable.vendorpayment.web.controller;

import com.solusi.erp.accountspayable.vendorpayment.application.usecase.command.*;
import com.solusi.erp.accountspayable.vendorpayment.application.usecase.query.*;
import com.solusi.erp.accountspayable.vendorpayment.domain.model.VendorPayment;
import com.solusi.erp.accountspayable.vendorpayment.domain.model.VendorPaymentLine;
import com.solusi.erp.accountspayable.vendorpayment.domain.model.VendorPaymentStatus;
import com.solusi.erp.accountspayable.vendorpayment.domain.port.PayableVendorBillQueryPort.PayableVendorBillView;
import com.solusi.erp.accountspayable.vendorpayment.web.dto.*;
import com.solusi.erp.accountspayable.vendorpayment.web.mapper.VendorPaymentWebMapper;
import com.solusi.erp.core.annotation.DefaultRedirectUrl;
import com.solusi.erp.core.domain.model.Pageable;
import com.solusi.erp.core.dto.ApiResponse;
import com.solusi.erp.core.dto.LookupDto;
import com.solusi.erp.core.infrastructure.util.PageableMapper;
import com.solusi.erp.master.bankaccount.application.usecase.query.FindBankAccountsUseCase;
import com.solusi.erp.master.bankaccount.domain.model.BankAccount;
import com.solusi.erp.master.bankaccount.domain.port.BankAccountLookupProvider;
import com.solusi.erp.master.currency.application.usecase.query.GetDefaultCurrencyUseCase;
import com.solusi.erp.master.currency.domain.model.Currency;
import com.solusi.erp.master.currency.domain.port.CurrencyLookupProvider;
import com.solusi.erp.master.party.domain.port.PartyLookupProvider;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/accounts-payable/vendor-payments")
@RequiredArgsConstructor
@DefaultRedirectUrl
public class VendorPaymentController {

    private final CreateVendorPaymentUseCase createVendorPaymentUseCase;
    private final UpdateVendorPaymentUseCase updateVendorPaymentUseCase;
    private final ConfirmVendorPaymentUseCase confirmVendorPaymentUseCase;
    private final CancelVendorPaymentUseCase cancelVendorPaymentUseCase;
    private final DeleteVendorPaymentUseCase deleteVendorPaymentUseCase;
    private final GetVendorPaymentListUseCase getVendorPaymentListUseCase;
    private final GetVendorPaymentDetailUseCase getVendorPaymentDetailUseCase;
    private final GetPayableVendorBillsUseCase getPayableVendorBillsUseCase;
    private final FindBankAccountsUseCase findBankAccountsUseCase;
    private final GetDefaultCurrencyUseCase getDefaultCurrencyUseCase;
    private final PartyLookupProvider partyLookupProvider;
    private final CurrencyLookupProvider currencyLookupProvider;
    private final BankAccountLookupProvider bankAccountLookupProvider;
    private final VendorPaymentWebMapper webMapper;
    private final MessageSource messageSource;

    @GetMapping
    @PreAuthorize("hasAuthority('VENDOR-PAYMENT_READ')")
    public String list(@RequestParam(required = false) String keyword,
                       @RequestParam(required = false) Long vendorId,
                       @RequestParam(required = false) VendorPaymentStatus status,
                       org.springframework.data.domain.Pageable springPageable,
                       Model model) {
        Pageable domainPageable = PageableMapper.toDomain(springPageable);
        com.solusi.erp.core.domain.model.Page<VendorPayment> domainPage =
                getVendorPaymentListUseCase.execute(keyword, vendorId, status, domainPageable);

        List<VendorPaymentSummaryResponse> content = domainPage.content().stream()
                .map(webMapper::toSummaryResponse)
                .toList();

        model.addAttribute("page", new PageImpl<>(content, springPageable, domainPage.totalElements()));
        model.addAttribute("keyword", keyword);
        model.addAttribute("vendorId", vendorId);
        model.addAttribute("status", status);
        model.addAttribute("statuses", VendorPaymentStatus.values());
        return "accountspayable/vendor-payments/list";
    }

    @GetMapping("/create")
    @PreAuthorize("hasAuthority('VENDOR-PAYMENT_CREATE')")
    public String createForm(Model model) {
        VendorPaymentSaveRequest request = new VendorPaymentSaveRequest();
        request.setPaymentDate(java.time.LocalDate.now());
        request.setExchangeRate(java.math.BigDecimal.ONE);

        Map<String, Object> vpUI = new HashMap<>();
        getDefaultCurrencyUseCase.execute().ifPresent(currency -> {
            request.setCurrencyId(currency.getId());
            LookupDto currencyLookup = currencyLookupProvider.resolve(currency.getId());
            if (currencyLookup != null) {
                vpUI.put("currencyText", currencyLookup.name());
                vpUI.put("currencySubtext", currencyLookup.subText());
                vpUI.put("currencyIsDefault", Boolean.TRUE.equals(currency.getIsDefault()));
            }
        });

        model.addAttribute("paymentRequest", request);
        model.addAttribute("vpUI", vpUI.isEmpty() ? null : vpUI);
        return "accountspayable/vendor-payments/form";
    }

    @GetMapping("/edit/{id}")
    @PreAuthorize("hasAuthority('VENDOR-PAYMENT_UPDATE')")
    public String editForm(@PathVariable Long id, Model model) {
        VendorPayment payment = getVendorPaymentDetailUseCase.execute(id)
                .orElseThrow(() -> new RuntimeException("VendorPayment not found"));
        model.addAttribute("paymentRequest", webMapper.toSaveRequest(payment));
        model.addAttribute("vpUI", buildVpUI(payment));
        return "accountspayable/vendor-payments/form";
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('VENDOR-PAYMENT_READ')")
    public String detail(@PathVariable Long id, Model model) {
        VendorPayment payment = getVendorPaymentDetailUseCase.execute(id)
                .orElseThrow(() -> new RuntimeException("VendorPayment not found"));
        model.addAttribute("payment", webMapper.toDetailResponse(payment));
        return "accountspayable/vendor-payments/detail";
    }

    @PostMapping
    @PreAuthorize("hasAuthority('VENDOR-PAYMENT_CREATE')")
    @ResponseBody
    public ResponseEntity<ApiResponse<VendorPaymentDetailResponse>> create(
            @Valid @RequestBody VendorPaymentSaveRequest request) {
        List<VendorPaymentLine> lines = webMapper.toLinesDomain(request.getLines());
        VendorPayment payment = createVendorPaymentUseCase.execute(
                request.getVendorId(), request.getCurrencyId(), request.getBankAccountId(),
                request.getPaymentDate(), request.getExchangeRate(), request.getPaymentAmount(),
                request.getReference(), request.getNotes(), lines);
        VendorPaymentDetailResponse data = webMapper.toDetailResponse(payment);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(msg("msg.success.create"), data));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('VENDOR-PAYMENT_UPDATE')")
    @ResponseBody
    public ResponseEntity<ApiResponse<VendorPaymentDetailResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody VendorPaymentSaveRequest request) {
        List<VendorPaymentLine> lines = webMapper.toLinesDomain(request.getLines());
        VendorPayment payment = updateVendorPaymentUseCase.execute(
                id, request.getVendorId(), request.getCurrencyId(), request.getBankAccountId(),
                request.getPaymentDate(), request.getExchangeRate(), request.getPaymentAmount(),
                request.getReference(), request.getNotes(), lines);
        VendorPaymentDetailResponse data = webMapper.toDetailResponse(payment);
        return ResponseEntity.ok(ApiResponse.success(msg("msg.success.update"), data));
    }

    @PostMapping("/{id}/confirm")
    @PreAuthorize("hasAuthority('VENDOR-PAYMENT_CONFIRM')")
    @ResponseBody
    public ResponseEntity<ApiResponse<VendorPaymentDetailResponse>> confirm(@PathVariable Long id) {
        confirmVendorPaymentUseCase.execute(id);
        VendorPayment payment = getVendorPaymentDetailUseCase.execute(id).orElseThrow();
        VendorPaymentDetailResponse data = webMapper.toDetailResponse(payment);
        return ResponseEntity.ok(ApiResponse.success(msg("msg.success.vp.confirmed"), data));
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAuthority('VENDOR-PAYMENT_CANCEL')")
    @ResponseBody
    public ResponseEntity<ApiResponse<Void>> cancel(@PathVariable Long id) {
        cancelVendorPaymentUseCase.execute(id);
        return ResponseEntity.ok(ApiResponse.success(msg("msg.success.vp.cancelled"), null));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('VENDOR-PAYMENT_DELETE')")
    @ResponseBody
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        deleteVendorPaymentUseCase.execute(id);
        return HtmxResponseUtility.okWithRefreshTableAndSuccess(msg("msg.success.delete"));
    }


    @GetMapping("/selectors/bank-accounts")
    @PreAuthorize("hasAuthority('VENDOR-PAYMENT_CREATE')")
    public String bankAccountSelector(@RequestParam(required = false) String q,
                                      @RequestParam Long currencyId,
                                      org.springframework.data.domain.Pageable pageable,
                                      Model model) {
        Pageable domainPageable = PageableMapper.toDomain(pageable);
        com.solusi.erp.core.domain.model.Page<BankAccount> page = findBankAccountsUseCase.execute(q, domainPageable);

        List<BankAccountSelectorRow> rows = page.content().stream()
                .filter(ba -> currencyId.equals(ba.getCurrencyId()))
                .filter(ba -> ba.getCoaId() != null)
                .map(ba -> new BankAccountSelectorRow(
                        ba.getId(),
                        ba.getAccountName(),
                        ba.getAccountNo(),
                        ba.getBankName(),
                        ba.getAccountType() != null ? ba.getAccountType().name() : "",
                        "",
                        ""
                ))
                .toList();

        model.addAttribute("page", new PageImpl<>(rows, pageable, page.totalElements()));
        model.addAttribute("q", q);
        model.addAttribute("currencyId", currencyId);
        return "accountspayable/vendor-payments/fragments/bank-account-selector";
    }

    @GetMapping("/payable-bills")
    @PreAuthorize("hasAuthority('VENDOR-PAYMENT_CREATE')")
    @ResponseBody
    public ResponseEntity<ApiResponse<List<PayableVendorBillResponse>>> payableBills(
            @RequestParam Long vendorId,
            @RequestParam Long currencyId) {
        List<PayableVendorBillView> views = getPayableVendorBillsUseCase.execute(vendorId, currencyId);
        List<PayableVendorBillResponse> data = views.stream()
                .map(webMapper::toPayableResponse)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(null, data));
    }

    private Map<String, Object> buildVpUI(VendorPayment payment) {
        Map<String, Object> ui = new HashMap<>();
        LookupDto vendor = partyLookupProvider.resolve(payment.getVendorId());
        if (vendor != null) {
            ui.put("vendorText", vendor.name());
            ui.put("vendorSubtext", vendor.subText());
        }
        LookupDto currency = currencyLookupProvider.resolve(payment.getCurrencyId());
        if (currency != null) {
            ui.put("currencyText", currency.name());
            ui.put("currencySubtext", currency.subText());
            Object isDefault = currency.payload() != null ? currency.payload().get("isDefault") : null;
            ui.put("currencyIsDefault", Boolean.TRUE.equals(isDefault));
        }
        LookupDto bankAccount = bankAccountLookupProvider.resolve(payment.getBankAccountId());
        if (bankAccount != null) {
            ui.put("bankAccountText", bankAccount.name());
            ui.put("bankAccountSubtext", bankAccount.subText());
        }
        return ui;
    }

    private String msg(String messageKey) {
        return messageSource.getMessage(messageKey, null, LocaleContextHolder.getLocale());
    }
}
