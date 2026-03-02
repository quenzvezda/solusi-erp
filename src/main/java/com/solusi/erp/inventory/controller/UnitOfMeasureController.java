package com.solusi.erp.inventory.controller;

import com.solusi.erp.inventory.dto.UnitOfMeasureRequest;
import com.solusi.erp.inventory.model.UomType;
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
 * Controller for Unit of Measure CRUD.
 */
@Controller
@RequestMapping("/inventory/unit-of-measures")
@RequiredArgsConstructor
public class UnitOfMeasureController {

    private final UnitOfMeasureService service;
    private final MessageSource messageSource;

    @GetMapping
    @PreAuthorize("hasAuthority('UNIT-OF-MEASURE_READ')")
    public String list(@RequestParam(value = "keyword", required = false) String keyword,
                       Pageable pageable,
                       Model model) {
        model.addAttribute("page", service.findAll(keyword, pageable));
        model.addAttribute("keyword", keyword);
        return "inventory/unit-of-measures/list";
    }

    @GetMapping("/create")
    @PreAuthorize("hasAuthority('UNIT-OF-MEASURE_CREATE')")
    public String showCreateForm(Model model) {
        model.addAttribute("unitOfMeasureRequest", new UnitOfMeasureRequest());
        model.addAttribute("types", UomType.values());
        return "inventory/unit-of-measures/form";
    }

    @PostMapping("/create")
    @PreAuthorize("hasAuthority('UNIT-OF-MEASURE_CREATE')")
    public String create(@Valid @ModelAttribute("unitOfMeasureRequest") UnitOfMeasureRequest request,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("types", UomType.values());
            return "inventory/unit-of-measures/form";
        }

        try {
            service.create(request);
            String message = messageSource.getMessage("msg.success.create", null, LocaleContextHolder.getLocale());
            redirectAttributes.addFlashAttribute("successMessage", message);
            return "redirect:/inventory/unit-of-measures";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("types", UomType.values());
            return "inventory/unit-of-measures/form";
        }
    }

    @GetMapping("/edit/{id}")
    @PreAuthorize("hasAuthority('UNIT-OF-MEASURE_UPDATE')")
    public String showEditForm(@PathVariable Long id, Model model) {
        try {
            model.addAttribute("unitOfMeasureRequest", service.getEditData(id));
            model.addAttribute("types", UomType.values());
            return "inventory/unit-of-measures/form";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "redirect:/inventory/unit-of-measures";
        }
    }

    @PostMapping("/edit/{id}")
    @PreAuthorize("hasAuthority('UNIT-OF-MEASURE_UPDATE')")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("unitOfMeasureRequest") UnitOfMeasureRequest request,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("types", UomType.values());
            return "inventory/unit-of-measures/form";
        }

        try {
            service.update(id, request);
            String message = messageSource.getMessage("msg.success.update", null, LocaleContextHolder.getLocale());
            redirectAttributes.addFlashAttribute("successMessage", message);
            return "redirect:/inventory/unit-of-measures";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("types", UomType.values());
            return "inventory/unit-of-measures/form";
        }
    }

    @PostMapping("/delete/{id}")
    @PreAuthorize("hasAuthority('UNIT-OF-MEASURE_DELETE')")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            service.delete(id);
            String message = messageSource.getMessage("msg.success.delete", null, LocaleContextHolder.getLocale());
            redirectAttributes.addFlashAttribute("successMessage", message);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/inventory/unit-of-measures";
    }
}
