package com.solusi.erp.inventory.controller;

import com.solusi.erp.inventory.dto.ProductRequest;
import com.solusi.erp.inventory.model.UomType;
import com.solusi.erp.inventory.service.BrandService;
import com.solusi.erp.inventory.service.ProductCategoryService;
import com.solusi.erp.inventory.service.ProductService;
import com.solusi.erp.inventory.service.UnitOfMeasureService;
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
 * Controller for Product CRUD.
 */
@Controller
@RequestMapping("/inventory/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService service;
    private final ProductCategoryService categoryService;
    private final UnitOfMeasureService uomService;
    private final BrandService brandService;
    private final MessageSource messageSource;

    @GetMapping
    @PreAuthorize("hasAuthority('PRODUCT_READ')")
    public String list(@RequestParam(value = "keyword", required = false) String keyword,
                       Pageable pageable,
                       Model model) {
        model.addAttribute("page", service.findAll(keyword, pageable));
        model.addAttribute("keyword", keyword);
        return "inventory/products/list";
    }

    @GetMapping("/create")
    @PreAuthorize("hasAuthority('PRODUCT_CREATE')")
    public String showCreateForm(Model model) {
        model.addAttribute("productRequest", new ProductRequest());
        populateSelectOptions(model);
        return "inventory/products/form";
    }

    @PostMapping("/create")
    @PreAuthorize("hasAuthority('PRODUCT_CREATE')")
    public String create(@Valid @ModelAttribute("productRequest") ProductRequest request,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            populateSelectOptions(model);
            return "inventory/products/form";
        }

        try {
            service.create(request);
            String message = messageSource.getMessage("msg.success.create", null, LocaleContextHolder.getLocale());
            redirectAttributes.addFlashAttribute("successMessage", message);
            return "redirect:/inventory/products";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            populateSelectOptions(model);
            return "inventory/products/form";
        }
    }

    @GetMapping("/edit/{id}")
    @PreAuthorize("hasAuthority('PRODUCT_UPDATE')")
    public String showEditForm(@PathVariable Long id, Model model) {
        try {
            model.addAttribute("productRequest", service.getEditData(id));
            populateSelectOptions(model);
            return "inventory/products/form";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "redirect:/inventory/products";
        }
    }

    @PostMapping("/edit/{id}")
    @PreAuthorize("hasAuthority('PRODUCT_UPDATE')")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("productRequest") ProductRequest request,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            populateSelectOptions(model);
            return "inventory/products/form";
        }

        try {
            service.update(id, request);
            String message = messageSource.getMessage("msg.success.update", null, LocaleContextHolder.getLocale());
            redirectAttributes.addFlashAttribute("successMessage", message);
            return "redirect:/inventory/products";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            populateSelectOptions(model);
            return "inventory/products/form";
        }
    }

    @PostMapping("/delete/{id}")
    @PreAuthorize("hasAuthority('PRODUCT_DELETE')")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            service.delete(id);
            String message = messageSource.getMessage("msg.success.delete", null, LocaleContextHolder.getLocale());
            redirectAttributes.addFlashAttribute("successMessage", message);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/inventory/products";
    }

    private void populateSelectOptions(Model model) {
        model.addAttribute("categories", categoryService.findAll(null, Pageable.unpaged()).getContent());
        model.addAttribute("uoms", uomService.findByType(UomType.UNIT));
        model.addAttribute("weightUoms", uomService.findByType(UomType.WEIGHT));
        model.addAttribute("lengthUoms", uomService.findByType(UomType.LENGTH));
        model.addAttribute("brands", brandService.findAll(null, Pageable.unpaged()).getContent());
    }
}
