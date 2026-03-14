package com.solusi.erp.master.controller;

import com.solusi.erp.master.dto.CurrencyRequest;
import com.solusi.erp.master.dto.CurrencyResponse;
import com.solusi.erp.master.service.CurrencyService;
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
@RequestMapping("/master/currencies")
@RequiredArgsConstructor
public class CurrencyController {

    private final CurrencyService currencyService;
    private final MessageSource messageSource;

    @GetMapping
    @PreAuthorize("hasAuthority('CURRENCY_READ')")
    public String listCurrencies(Model model,
            @RequestParam(value = "keyword", required = false) String keyword,
            Pageable pageable) {
        Page<CurrencyResponse> currencies = currencyService.getAllCurrencies(keyword, pageable);
        model.addAttribute("page", currencies);
        model.addAttribute("keyword", keyword);
        return "master/currency/list";
    }

    @GetMapping("/create")
    @PreAuthorize("hasAuthority('CURRENCY_CREATE')")
    public String showCreateForm(Model model) {
        CurrencyRequest request = new CurrencyRequest();
        request.setIsActive(true);
        request.setIsDefault(false);
        model.addAttribute("currency", request);
        return "master/currency/form";
    }

    @PostMapping("/create")
    @PreAuthorize("hasAuthority('CURRENCY_CREATE')")
    public String createCurrency(@Valid @ModelAttribute("currency") CurrencyRequest request,
            BindingResult result,
            RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "master/currency/form";
        }
        try {
            currencyService.createCurrency(request);
            String message = messageSource.getMessage("currency.create.success", null, LocaleContextHolder.getLocale());
            redirectAttributes.addFlashAttribute("successMessage", message);
            return "redirect:/master/currencies";
        } catch (Exception e) {
            log.error("Failed to create Currency", e);
            result.reject("global", e.getMessage());
            return "master/currency/form";
        }
    }

    @GetMapping("/edit/{id}")
    @PreAuthorize("hasAuthority('CURRENCY_UPDATE')")
    public String showEditForm(@PathVariable Long id, Model model) {
        CurrencyRequest request = currencyService.getEditData(id);
        model.addAttribute("currency", request);
        return "master/currency/form";
    }

    @PostMapping("/edit/{id}")
    @PreAuthorize("hasAuthority('CURRENCY_UPDATE')")
    public String updateCurrency(@PathVariable Long id,
            @Valid @ModelAttribute("currency") CurrencyRequest request,
            BindingResult result,
            RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "master/currency/form";
        }
        try {
            currencyService.updateCurrency(id, request);
            String message = messageSource.getMessage("currency.update.success", null, LocaleContextHolder.getLocale());
            redirectAttributes.addFlashAttribute("successMessage", message);
            return "redirect:/master/currencies";
        } catch (Exception e) {
            log.error("Failed to update Currency", e);
            result.reject("global", e.getMessage());
            return "master/currency/form";
        }
    }

    @PostMapping("/delete/{id}")
    @PreAuthorize("hasAuthority('CURRENCY_DELETE')")
    public String deleteCurrency(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            currencyService.deleteCurrency(id);
            String message = messageSource.getMessage("currency.delete.success", null, LocaleContextHolder.getLocale());
            redirectAttributes.addFlashAttribute("successMessage", message);
        } catch (Exception e) {
            log.error("Failed to delete Currency", e);
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/master/currencies";
    }
}
