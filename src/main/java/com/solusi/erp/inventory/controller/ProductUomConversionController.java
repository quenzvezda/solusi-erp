package com.solusi.erp.inventory.controller;

import com.solusi.erp.inventory.dto.ProductUomConversionRequest;
import com.solusi.erp.inventory.model.UomType;
import com.solusi.erp.inventory.service.ProductUomConversionService;
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

@Controller
@RequestMapping("/inventory/uom-conversions")
@RequiredArgsConstructor
public class ProductUomConversionController {

    private final ProductUomConversionService service;
    private final UnitOfMeasureService uomService;
    private final MessageSource messageSource;

    @GetMapping
    @PreAuthorize("hasAuthority('UOM-CONVERSION_READ')")
    public String list(@RequestParam(value = "keyword", required = false) String keyword,
            Pageable pageable,
            Model model) {
        model.addAttribute("page", service.findAll(keyword, pageable));
        model.addAttribute("keyword", keyword);
        return "inventory/uom-conversions/list";
    }

    @GetMapping("/create")
    @PreAuthorize("hasAuthority('UOM-CONVERSION_CREATE')")
    public String showCreateForm(Model model) {
        model.addAttribute("uomConversionRequest", new ProductUomConversionRequest());
        populateSelectOptions(model);
        return "inventory/uom-conversions/form";
    }

    @PostMapping("/create")
    @PreAuthorize("hasAuthority('UOM-CONVERSION_CREATE')")
    public String create(@Valid @ModelAttribute("uomConversionRequest") ProductUomConversionRequest request,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            populateSelectOptions(model);
            return "inventory/uom-conversions/form";
        }

        try {
            service.create(request);
            String message = messageSource.getMessage("msg.success.create", null, LocaleContextHolder.getLocale());
            redirectAttributes.addFlashAttribute("successMessage", message);
            return "redirect:/inventory/uom-conversions";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            populateSelectOptions(model);
            return "inventory/uom-conversions/form";
        }
    }

    @GetMapping("/edit/{id}")
    @PreAuthorize("hasAuthority('UOM-CONVERSION_UPDATE')")
    public String showEditForm(@PathVariable Long id, Model model) {
        try {
            model.addAttribute("uomConversionRequest", service.getEditData(id));
            populateSelectOptions(model);
            return "inventory/uom-conversions/form";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "redirect:/inventory/uom-conversions";
        }
    }

    @PostMapping("/edit/{id}")
    @PreAuthorize("hasAuthority('UOM-CONVERSION_UPDATE')")
    public String update(@PathVariable Long id,
            @Valid @ModelAttribute("uomConversionRequest") ProductUomConversionRequest request,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            populateSelectOptions(model);
            return "inventory/uom-conversions/form";
        }

        try {
            service.update(id, request);
            String message = messageSource.getMessage("msg.success.update", null, LocaleContextHolder.getLocale());
            redirectAttributes.addFlashAttribute("successMessage", message);
            return "redirect:/inventory/uom-conversions";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            populateSelectOptions(model);
            return "inventory/uom-conversions/form";
        }
    }

    @PostMapping("/delete/{id}")
    @PreAuthorize("hasAuthority('UOM-CONVERSION_DELETE')")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            service.delete(id);
            String message = messageSource.getMessage("msg.success.delete", null, LocaleContextHolder.getLocale());
            redirectAttributes.addFlashAttribute("successMessage", message);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/inventory/uom-conversions";
    }

    private void populateSelectOptions(Model model) {
        model.addAttribute("uoms", uomService.findByType(UomType.UNIT));
    }
}
