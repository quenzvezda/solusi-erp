package com.solusi.erp.master.controller;

import com.solusi.erp.master.dto.TaxRequest;
import com.solusi.erp.master.dto.TaxResponse;
import com.solusi.erp.master.service.TaxService;
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
@RequestMapping("/master/taxes")
@RequiredArgsConstructor
public class TaxController {

    private final TaxService taxService;
    private final MessageSource messageSource;

    @GetMapping
    @PreAuthorize("hasAuthority('TAX_READ')")
    public String listTaxes(Model model,
            @RequestParam(value = "keyword", required = false) String keyword,
            Pageable pageable) {
        Page<TaxResponse> taxes = taxService.getAllTaxes(keyword, pageable);
        model.addAttribute("page", taxes);
        model.addAttribute("keyword", keyword);
        return "master/tax/list";
    }

    @GetMapping("/create")
    @PreAuthorize("hasAuthority('TAX_CREATE')")
    public String showCreateForm(Model model) {
        TaxRequest request = new TaxRequest();
        request.setIsActive(true);
        request.setIsSubtract(false);
        model.addAttribute("tax", request);
        return "master/tax/form";
    }

    @PostMapping("/create")
    @PreAuthorize("hasAuthority('TAX_CREATE')")
    public String createTax(@Valid @ModelAttribute("tax") TaxRequest request,
            BindingResult result,
            RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "master/tax/form";
        }
        try {
            taxService.createTax(request);
            String message = messageSource.getMessage("tax.create.success", null, LocaleContextHolder.getLocale());
            redirectAttributes.addFlashAttribute("successMessage", message);
            return "redirect:/master/taxes";
        } catch (Exception e) {
            log.error("Failed to create Tax", e);
            result.reject("global", e.getMessage());
            return "master/tax/form";
        }
    }

    @GetMapping("/edit/{id}")
    @PreAuthorize("hasAuthority('TAX_UPDATE')")
    public String showEditForm(@PathVariable Long id, Model model) {
        TaxRequest request = taxService.getEditData(id);
        model.addAttribute("tax", request);
        return "master/tax/form";
    }

    @PostMapping("/edit/{id}")
    @PreAuthorize("hasAuthority('TAX_UPDATE')")
    public String updateTax(@PathVariable Long id,
            @Valid @ModelAttribute("tax") TaxRequest request,
            BindingResult result,
            RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "master/tax/form";
        }
        try {
            taxService.updateTax(id, request);
            String message = messageSource.getMessage("tax.update.success", null, LocaleContextHolder.getLocale());
            redirectAttributes.addFlashAttribute("successMessage", message);
            return "redirect:/master/taxes";
        } catch (Exception e) {
            log.error("Failed to update Tax", e);
            result.reject("global", e.getMessage());
            return "master/tax/form";
        }
    }

    @PostMapping("/delete/{id}")
    @PreAuthorize("hasAuthority('TAX_DELETE')")
    public String deleteTax(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            taxService.deleteTax(id);
            String message = messageSource.getMessage("tax.delete.success", null, LocaleContextHolder.getLocale());
            redirectAttributes.addFlashAttribute("successMessage", message);
        } catch (Exception e) {
            log.error("Failed to delete Tax", e);
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/master/taxes";
    }
}
