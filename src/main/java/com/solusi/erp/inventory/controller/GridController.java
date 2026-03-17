package com.solusi.erp.inventory.controller;

import com.solusi.erp.inventory.dto.GridRequest;
import com.solusi.erp.inventory.service.FacilityService;
import com.solusi.erp.inventory.service.GridService;
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
 * Controller for Grid CRUD.
 */
@Controller
@RequestMapping("/inventory/grids")
@RequiredArgsConstructor
public class GridController {

    private final GridService service;
    private final FacilityService facilityService;
    private final MessageSource messageSource;

    @GetMapping
    @PreAuthorize("hasAuthority('GRID_READ')")
    public String list(@RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "facilityId", required = false) Long facilityId,
            Pageable pageable,
            Model model) {
        model.addAttribute("page", service.findAll(keyword, facilityId, pageable));
        model.addAttribute("keyword", keyword);
        model.addAttribute("facilityId", facilityId);
        return "inventory/grids/list";
    }

    @GetMapping("/create")
    @PreAuthorize("hasAuthority('GRID_CREATE')")
    public String showCreateForm(Model model) {
        model.addAttribute("gridRequest", new GridRequest());
        populateSelectOptions(model);
        return "inventory/grids/form";
    }

    @PostMapping("/create")
    @PreAuthorize("hasAuthority('GRID_CREATE')")
    public String create(@Valid @ModelAttribute("gridRequest") GridRequest request,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            populateSelectOptions(model);
            return "inventory/grids/form";
        }

        try {
            service.create(request);
            String message = messageSource.getMessage("msg.success.create", null, LocaleContextHolder.getLocale());
            redirectAttributes.addFlashAttribute("successMessage", message);
            return "redirect:/inventory/grids";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            populateSelectOptions(model);
            return "inventory/grids/form";
        }
    }

    @GetMapping("/edit/{id}")
    @PreAuthorize("hasAuthority('GRID_UPDATE')")
    public String showEditForm(@PathVariable Long id, Model model) {
        try {
            model.addAttribute("gridRequest", service.getEditData(id));
            populateSelectOptions(model);
            return "inventory/grids/form";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "redirect:/inventory/grids";
        }
    }

    @PostMapping("/edit/{id}")
    @PreAuthorize("hasAuthority('GRID_UPDATE')")
    public String update(@PathVariable Long id,
            @Valid @ModelAttribute("gridRequest") GridRequest request,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            populateSelectOptions(model);
            return "inventory/grids/form";
        }

        try {
            service.update(id, request);
            String message = messageSource.getMessage("msg.success.update", null, LocaleContextHolder.getLocale());
            redirectAttributes.addFlashAttribute("successMessage", message);
            return "redirect:/inventory/grids";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            populateSelectOptions(model);
            return "inventory/grids/form";
        }
    }

    @PostMapping("/delete/{id}")
    @PreAuthorize("hasAuthority('GRID_DELETE')")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            service.delete(id);
            String message = messageSource.getMessage("msg.success.delete", null, LocaleContextHolder.getLocale());
            redirectAttributes.addFlashAttribute("successMessage", message);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/inventory/grids";
    }

    private void populateSelectOptions(Model model) {
        model.addAttribute("facilities", facilityService.findAll(null, Pageable.unpaged()).getContent());
    }
}
