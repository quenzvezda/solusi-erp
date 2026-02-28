package com.solusi.erp.inventory.controller;

import com.solusi.erp.inventory.dto.ProductCategoryRequest;
import com.solusi.erp.inventory.model.ProductCategoryType;
import com.solusi.erp.inventory.service.ProductCategoryService;
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
 * Controller for Product Category CRUD.
 */
@Controller
@RequestMapping("/inventory/product-categories")
@RequiredArgsConstructor
public class ProductCategoryController {

    private final ProductCategoryService service;
    private final MessageSource messageSource;

    @GetMapping
    @PreAuthorize("hasAuthority('PRODUCT-CATEGORY_READ')")
    public String list(@RequestParam(value = "keyword", required = false) String keyword,
                       Pageable pageable,
                       Model model) {
        model.addAttribute("page", service.findAll(keyword, pageable));
        model.addAttribute("keyword", keyword);
        return "inventory/product-categories/list";
    }

    @GetMapping("/create")
    @PreAuthorize("hasAuthority('PRODUCT-CATEGORY_CREATE')")
    public String showCreateForm(Model model) {
        model.addAttribute("productCategoryRequest", new ProductCategoryRequest());
        model.addAttribute("types", ProductCategoryType.values());
        return "inventory/product-categories/form";
    }

    @PostMapping("/create")
    @PreAuthorize("hasAuthority('PRODUCT-CATEGORY_CREATE')")
    public String create(@Valid @ModelAttribute("productCategoryRequest") ProductCategoryRequest request,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("types", ProductCategoryType.values());
            return "inventory/product-categories/form";
        }

        try {
            service.create(request);
            String message = messageSource.getMessage("msg.success.create", null, LocaleContextHolder.getLocale());
            redirectAttributes.addFlashAttribute("successMessage", message);
            return "redirect:/inventory/product-categories";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("types", ProductCategoryType.values());
            return "inventory/product-categories/form";
        }
    }

    @GetMapping("/edit/{id}")
    @PreAuthorize("hasAuthority('PRODUCT-CATEGORY_UPDATE')")
    public String showEditForm(@PathVariable Long id, Model model) {
        try {
            model.addAttribute("productCategoryRequest", service.getEditData(id));
            model.addAttribute("types", ProductCategoryType.values());
            return "inventory/product-categories/form";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "redirect:/inventory/product-categories";
        }
    }

    @PostMapping("/edit/{id}")
    @PreAuthorize("hasAuthority('PRODUCT-CATEGORY_UPDATE')")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("productCategoryRequest") ProductCategoryRequest request,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("types", ProductCategoryType.values());
            return "inventory/product-categories/form";
        }

        try {
            service.update(id, request);
            String message = messageSource.getMessage("msg.success.update", null, LocaleContextHolder.getLocale());
            redirectAttributes.addFlashAttribute("successMessage", message);
            return "redirect:/inventory/product-categories";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("types", ProductCategoryType.values());
            return "inventory/product-categories/form";
        }
    }

    @PostMapping("/delete/{id}")
    @PreAuthorize("hasAuthority('PRODUCT-CATEGORY_DELETE')")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            service.delete(id);
            String message = messageSource.getMessage("msg.success.delete", null, LocaleContextHolder.getLocale());
            redirectAttributes.addFlashAttribute("successMessage", message);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/inventory/product-categories";
    }
}
