package com.solusi.erp.master.bankaccount.web.controller;

import com.solusi.erp.core.annotation.DefaultRedirectUrl;
import com.solusi.erp.core.domain.model.DeleteResult;
import com.solusi.erp.core.domain.model.Pageable;
import com.solusi.erp.core.infrastructure.util.PageableMapper;
import com.solusi.erp.core.dto.ApiResponse;
import com.solusi.erp.accounting.coa.domain.port.CoaLookupProvider;
import com.solusi.erp.core.dto.LookupDto;
import com.solusi.erp.master.bankaccount.application.usecase.command.*;
import com.solusi.erp.master.bankaccount.application.usecase.query.*;
import com.solusi.erp.master.bankaccount.domain.model.BankAccount;
import com.solusi.erp.master.bankaccount.web.dto.*;
import com.solusi.erp.master.bankaccount.web.mapper.BankAccountWebMapper;
import com.solusi.erp.master.currency.domain.port.CurrencyLookupProvider;
import com.solusi.erp.master.geographic.domain.port.GeographicLookupProvider;
import com.solusi.erp.master.party.domain.port.PartyLookupProvider;
import com.solusi.erp.master.shared.model.PaymentType;
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
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Controller for BankAccount CRUD.
 * Replaces legacy com.solusi.erp.master.controller.BankAccountController.
 */
@Controller
@RequestMapping("/master/bank-accounts")
@RequiredArgsConstructor
@DefaultRedirectUrl
public class BankAccountController {

    private final CreateBankAccountUseCase createBankAccountUseCase;
    private final UpdateBankAccountUseCase updateBankAccountUseCase;
    private final DeleteBankAccountUseCase deleteBankAccountUseCase;
    private final FindBankAccountsUseCase findBankAccountsUseCase;
    private final GetBankAccountEditViewUseCase getBankAccountEditViewUseCase;
    private final BankAccountWebMapper webMapper;
    private final MessageSource messageSource;
    private final PartyLookupProvider partyLookupProvider;
    private final GeographicLookupProvider geographicLookupProvider;
    private final CurrencyLookupProvider currencyLookupProvider;
    private final CoaLookupProvider coaLookupProvider;

    @GetMapping
    @PreAuthorize("hasAuthority('BANK-ACCOUNT_READ')")
    public String list(@RequestParam(required = false) String keyword,
                       org.springframework.data.domain.Pageable springPageable,
                       Model model) {
        Pageable domainPageable = PageableMapper.toDomain(springPageable);
        com.solusi.erp.core.domain.model.Page<BankAccount> domainPage =
                findBankAccountsUseCase.execute(keyword, domainPageable);

        List<BankAccountSummaryResponse> content = domainPage.content().stream()
                .map(webMapper::toSummaryResponse)
                .collect(Collectors.toList());

        Page<BankAccountSummaryResponse> springPage = new PageImpl<>(content, springPageable, domainPage.totalElements());
        model.addAttribute("page", springPage);
        model.addAttribute("keyword", keyword);
        return "master/bank-accounts/list";
    }

    @GetMapping("/create")
    @PreAuthorize("hasAuthority('BANK-ACCOUNT_CREATE')")
    public String showCreateForm(Model model) {
        model.addAttribute("bankAccountRequest", new BankAccountSaveRequest());
        model.addAttribute("accountTypes", PaymentType.values());
        model.addAttribute("bankAccountUI", new HashMap<>());
        return "master/bank-accounts/form";
    }

    @PostMapping("/create")
    @PreAuthorize("hasAuthority('BANK-ACCOUNT_CREATE')")
    @ResponseBody
    public ResponseEntity<ApiResponse<BankAccountDetailResponse>> create(
            @Valid @RequestBody BankAccountSaveRequest request) {
        BankAccount domain = createBankAccountUseCase.execute(
                request.getBankName(), request.getBranch(), request.getAccountName(),
                request.getAccountNo(), PaymentType.valueOf(request.getAccountType()),
                request.getNote(), request.getCityId(), request.getPartyId(),
                request.getIsActive(), request.getCurrencyId(), request.getCoaId());
        BankAccountDetailResponse data = webMapper.toDetailResponse(domain);
        String msg = messageSource.getMessage("msg.success.create", null, LocaleContextHolder.getLocale());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(msg, data));
    }

    @GetMapping("/edit/{id}")
    @PreAuthorize("hasAuthority('BANK-ACCOUNT_UPDATE')")
    public String showEditForm(@PathVariable Long id, Model model) {
        BankAccount domain = getBankAccountEditViewUseCase.execute(id)
                .orElseThrow(() -> new RuntimeException("BankAccount not found"));
        model.addAttribute("bankAccountRequest", webMapper.toSaveRequest(domain));
        model.addAttribute("auditInfo", webMapper.toDetailResponse(domain));
        model.addAttribute("accountTypes", PaymentType.values());
        model.addAttribute("bankAccountUI", buildBankAccountUI(domain));

        LookupDto cityLookup = geographicLookupProvider.resolve(domain.getCityId());
        model.addAttribute("selectedCity", cityLookup != null ? cityLookup.name() : domain.getCityName());

        LookupDto partyLookup = partyLookupProvider.resolve(domain.getPartyId());
        model.addAttribute("selectedParty", partyLookup != null ? partyLookup.name() : domain.getPartyName());

        return "master/bank-accounts/form";
    }

    @PostMapping("/edit/{id}")
    @PreAuthorize("hasAuthority('BANK-ACCOUNT_UPDATE')")
    @ResponseBody
    public ResponseEntity<ApiResponse<BankAccountDetailResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody BankAccountSaveRequest request) {
        BankAccount domain = updateBankAccountUseCase.execute(
                id, request.getBankName(), request.getBranch(), request.getAccountName(),
                request.getAccountNo(), PaymentType.valueOf(request.getAccountType()),
                request.getNote(), request.getCityId(), request.getPartyId(),
                request.getIsActive(), request.getCurrencyId(), request.getCoaId());
        BankAccountDetailResponse data = webMapper.toDetailResponse(domain);
        String msg = messageSource.getMessage("msg.success.update", null, LocaleContextHolder.getLocale());
        return ResponseEntity.ok(ApiResponse.success(msg, data));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('BANK-ACCOUNT_DELETE')")
    @ResponseBody
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        DeleteResult result = deleteBankAccountUseCase.execute(id);
        if (result == DeleteResult.SOFT_DELETED) {
            String msg = messageSource.getMessage("msg.success.deactivated", null, LocaleContextHolder.getLocale());
            return HtmxResponseUtility.okWithRefreshTableAndWarning(msg);
        }
        String msg = messageSource.getMessage("msg.success.delete", null, LocaleContextHolder.getLocale());
        return HtmxResponseUtility.okWithRefreshTableAndSuccess(msg);
    }

    private Map<String, Object> buildBankAccountUI(BankAccount domain) {
        Map<String, Object> ui = new HashMap<>();
        LookupDto currency = currencyLookupProvider.resolve(domain.getCurrencyId());
        if (currency != null) {
            ui.put("currencyText", currency.name());
            ui.put("currencySubtext", currency.subText());
        }
        LookupDto coa = coaLookupProvider.resolve(domain.getCoaId());
        if (coa != null) {
            ui.put("coaText", coa.name());
            ui.put("coaSubtext", coa.subText());
        }
        return ui;
    }
}

