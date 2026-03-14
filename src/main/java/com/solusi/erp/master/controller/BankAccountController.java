package com.solusi.erp.master.controller;

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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
    public String create(@Valid @ModelAttribute("bankAccount") BankAccountRequest request,
            BindingResult result,
            RedirectAttributes redirectAttributes, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("accountTypes", AccountType.values());
            return "master/bank-accounts/form";
        }
        try {
            bankAccountService.create(request);
            String message = messageSource.getMessage("master.bank-account.create.success", null, LocaleContextHolder.getLocale());
            redirectAttributes.addFlashAttribute("successMessage", message);
            return "redirect:/master/bank-accounts";
        } catch (Exception e) {
            log.error("Failed to create Bank Account", e);
            model.addAttribute("accountTypes", AccountType.values());
            result.reject("global", e.getMessage());
            return "master/bank-accounts/form";
        }
    }

    @GetMapping("/edit/{id}")
    @PreAuthorize("hasAuthority('BANK-ACCOUNT_UPDATE')")
    public String showEditForm(@PathVariable Long id, Model model) {
        BankAccountRequest request = bankAccountService.getEditData(id);
        
        // Fetch full entity to populate the view correctly (specifically for party and city names)
        BankAccountResponse responseDto = bankAccountService.findById(id);
        
        model.addAttribute("bankAccount", request);
        model.addAttribute("selectedCity", responseDto.getCityName());
        model.addAttribute("selectedParty", responseDto.getPartyName());
        model.addAttribute("accountTypes", AccountType.values());
        return "master/bank-accounts/form";
    }

    @PostMapping("/edit/{id}")
    @PreAuthorize("hasAuthority('BANK-ACCOUNT_UPDATE')")
    public String update(@PathVariable Long id,
            @Valid @ModelAttribute("bankAccount") BankAccountRequest request,
            BindingResult result,
            RedirectAttributes redirectAttributes, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("accountTypes", AccountType.values());
            // Need to repopulate selected names
            try {
                BankAccountResponse responseDto = bankAccountService.findById(id);
                model.addAttribute("selectedCity", responseDto.getCityName());
                model.addAttribute("selectedParty", responseDto.getPartyName());
            } catch (Exception ignored) {}
            return "master/bank-accounts/form";
        }
        try {
            bankAccountService.update(id, request);
            String message = messageSource.getMessage("master.bank-account.update.success", null, LocaleContextHolder.getLocale());
            redirectAttributes.addFlashAttribute("successMessage", message);
            return "redirect:/master/bank-accounts";
        } catch (Exception e) {
            log.error("Failed to update Bank Account", e);
            model.addAttribute("accountTypes", AccountType.values());
            result.reject("global", e.getMessage());
            return "master/bank-accounts/form";
        }
    }

    @PostMapping("/delete/{id}")
    @PreAuthorize("hasAuthority('BANK-ACCOUNT_DELETE')")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            bankAccountService.delete(id);
            String message = messageSource.getMessage("master.bank-account.delete.success", null, LocaleContextHolder.getLocale());
            redirectAttributes.addFlashAttribute("successMessage", message);
        } catch (Exception e) {
            log.error("Failed to delete Bank Account", e);
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/master/bank-accounts";
    }
}
