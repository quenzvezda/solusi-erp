package com.solusi.erp.inventory.controller;

import com.solusi.erp.inventory.dto.BrandRequest;
import com.solusi.erp.inventory.service.BrandService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller for Brand CRUD.
 */
@Controller
@RequestMapping("/inventory/brands")
@RequiredArgsConstructor
public class BrandController {

    private final BrandService service;
    private final MessageSource messageSource;

    @GetMapping
    @PreAuthorize("hasAuthority('BRAND_READ')")
    public String list(@RequestParam(value = "keyword", required = false) String keyword,
                       Pageable pageable,
                       Model model) {
        model.addAttribute("page", service.findAll(keyword, pageable));
        model.addAttribute("keyword", keyword);
        return "inventory/brands/list";
    }

    @GetMapping("/create")
    @PreAuthorize("hasAuthority('BRAND_CREATE')")
    public String showCreateForm(Model model) {
        model.addAttribute("brandRequest", new BrandRequest());
        return "inventory/brands/form";
    }

    @PostMapping("/create")
    @PreAuthorize("hasAuthority('BRAND_CREATE')")
    public String create(@Valid @ModelAttribute("brandRequest") BrandRequest request,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "inventory/brands/form";
        }

        try {
            service.create(request);
            String message = messageSource.getMessage("msg.success.create", null, LocaleContextHolder.getLocale());
            redirectAttributes.addFlashAttribute("successMessage", message);
            return "redirect:/inventory/brands";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "inventory/brands/form";
        }
    }

    @GetMapping("/edit/{id}")
    @PreAuthorize("hasAuthority('BRAND_UPDATE')")
    public String showEditForm(@PathVariable Long id, Model model) {
        try {
            model.addAttribute("brandRequest", service.getEditData(id));
            return "inventory/brands/form";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "redirect:/inventory/brands";
        }
    }

    @PostMapping("/edit/{id}")
    @PreAuthorize("hasAuthority('BRAND_UPDATE')")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("brandRequest") BrandRequest request,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "inventory/brands/form";
        }

        try {
            service.update(id, request);
            String message = messageSource.getMessage("msg.success.update", null, LocaleContextHolder.getLocale());
            redirectAttributes.addFlashAttribute("successMessage", message);
            return "redirect:/inventory/brands";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "inventory/brands/form";
        }
    }

    @PostMapping("/delete/{id}")
    @PreAuthorize("hasAuthority('BRAND_DELETE')")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            service.delete(id);
            String message = messageSource.getMessage("msg.success.delete", null, LocaleContextHolder.getLocale());
            redirectAttributes.addFlashAttribute("successMessage", message);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/inventory/brands";
    }
}
