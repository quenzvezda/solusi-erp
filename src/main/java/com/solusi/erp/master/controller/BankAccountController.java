package com.solusi.erp.master.controller;

import com.solusi.erp.core.dto.ApiResponse;
import com.solusi.erp.master.dto.BankAccountRequest;
import com.solusi.erp.master.dto.BankAccountResponse;
import com.solusi.erp.master.model.AccountType;
import com.solusi.erp.master.service.BankAccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Controller
@RequestMapping("/master/bank-accounts")
@RequiredArgsConstructor
public class BankAccountController {

    private final BankAccountService bankAccountService;
    private final MessageSource messageSource;

    @GetMapping
    @PreAuthorize("hasAuthority('BANK-ACCOUNT_READ')")
    public String list(Model model,
            @RequestParam(value = "keyword", required = false) String keyword,
            Pageable pageable) {
        Page<BankAccountResponse> page = bankAccountService.findAll(keyword, pageable);
        model.addAttribute("page", page);
        model.addAttribute("keyword", keyword);
        return "master/bank-accounts/list";
    }

    @GetMapping("/create")
    @PreAuthorize("hasAuthority('BANK-ACCOUNT_CREATE')")
    public String showCreateForm(Model model) {
        model.addAttribute("bankAccount", new BankAccountRequest());
        model.addAttribute("accountTypes", AccountType.values());
        return "master/bank-accounts/form";
    }

    @PostMapping("/create")
    @PreAuthorize("hasAuthority('BANK-ACCOUNT_CREATE')")
    @ResponseBody
    public ResponseEntity<ApiResponse<BankAccountResponse>> create(@Valid @RequestBody BankAccountRequest request) {
        BankAccountResponse data = bankAccountService.create(request);
        String msg = messageSource.getMessage("master.bank-account.create.success", null, LocaleContextHolder.getLocale());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(msg, data));
    }

    @GetMapping("/edit/{id}")
    @PreAuthorize("hasAuthority('BANK-ACCOUNT_UPDATE')")
    public String showEditForm(@PathVariable Long id, Model model) {
        BankAccountRequest request = bankAccountService.getEditData(id);
        
        // Fetch full entity to populate the view correctly (specifically for party and city names)
        BankAccountResponse responseDto = bankAccountService.findById(id);
        
        model.addAttribute("id", id);
        model.addAttribute("bankAccount", request);
        model.addAttribute("selectedCity", responseDto.getCityName());
        model.addAttribute("selectedParty", responseDto.getPartyName());
        model.addAttribute("accountTypes", AccountType.values());
        
        // Populate auditInfo for fragments/audit-info
        model.addAttribute("auditInfo", responseDto);
        
        return "master/bank-accounts/form";
    }

    @PostMapping("/edit/{id}")
    @PreAuthorize("hasAuthority('BANK-ACCOUNT_UPDATE')")
    @ResponseBody
    public ResponseEntity<ApiResponse<BankAccountResponse>> update(@PathVariable Long id,
            @Valid @RequestBody BankAccountRequest request) {
        BankAccountResponse data = bankAccountService.update(id, request);
        String msg = messageSource.getMessage("master.bank-account.update.success", null, LocaleContextHolder.getLocale());
        return ResponseEntity.ok(ApiResponse.success(msg, data));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('BANK-ACCOUNT_DELETE')")
    @ResponseBody
    public ResponseEntity<Void> deleteHtmx(@PathVariable Long id) {
        bankAccountService.delete(id);
        return ResponseEntity.ok()
                .header("HX-Trigger", "refresh-table")
                .build();
    }
}
