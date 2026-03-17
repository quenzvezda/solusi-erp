package com.solusi.erp.inventory.controller;

import com.solusi.erp.inventory.dto.ContainerRequest;
import com.solusi.erp.inventory.service.ContainerService;
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
 * Controller for Container CRUD.
 */
@Controller
@RequestMapping("/inventory/containers")
@RequiredArgsConstructor
public class ContainerController {

    private final ContainerService service;
    private final GridService gridService;
    private final MessageSource messageSource;

    @GetMapping
    @PreAuthorize("hasAuthority('CONTAINER_READ')")
    public String list(@RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "gridId", required = false) Long gridId,
            Pageable pageable,
            Model model) {
        model.addAttribute("page", service.findAll(keyword, gridId, pageable));
        model.addAttribute("keyword", keyword);
        model.addAttribute("gridId", gridId);
        
        if (gridId != null) {
            try {
                model.addAttribute("selectedGrid", gridService.findById(gridId));
            } catch (Exception ignored) {}
        }
        
        return "inventory/containers/list";
    }

    @GetMapping("/create")
    @PreAuthorize("hasAuthority('CONTAINER_CREATE')")
    public String showCreateForm(Model model) {
        model.addAttribute("containerRequest", new ContainerRequest());
        populateSelectOptions(model);
        return "inventory/containers/form";
    }

    @PostMapping("/create")
    @PreAuthorize("hasAuthority('CONTAINER_CREATE')")
    public String create(@Valid @ModelAttribute("containerRequest") ContainerRequest request,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            populateSelectOptions(model);
            return "inventory/containers/form";
        }

        try {
            service.create(request);
            String message = messageSource.getMessage("msg.success.create", null, LocaleContextHolder.getLocale());
            redirectAttributes.addFlashAttribute("successMessage", message);
            return "redirect:/inventory/containers";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            populateSelectOptions(model);
            return "inventory/containers/form";
        }
    }

    @GetMapping("/edit/{id}")
    @PreAuthorize("hasAuthority('CONTAINER_UPDATE')")
    public String showEditForm(@PathVariable Long id, Model model) {
        try {
            model.addAttribute("containerRequest", service.getEditData(id));
            populateSelectOptions(model);
            return "inventory/containers/form";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "redirect:/inventory/containers";
        }
    }

    @PostMapping("/edit/{id}")
    @PreAuthorize("hasAuthority('CONTAINER_UPDATE')")
    public String update(@PathVariable Long id,
            @Valid @ModelAttribute("containerRequest") ContainerRequest request,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            populateSelectOptions(model);
            return "inventory/containers/form";
        }

        try {
            service.update(id, request);
            String message = messageSource.getMessage("msg.success.update", null, LocaleContextHolder.getLocale());
            redirectAttributes.addFlashAttribute("successMessage", message);
            return "redirect:/inventory/containers";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            populateSelectOptions(model);
            return "inventory/containers/form";
        }
    }

    @PostMapping("/delete/{id}")
    @PreAuthorize("hasAuthority('CONTAINER_DELETE')")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            service.delete(id);
            String message = messageSource.getMessage("msg.success.delete", null, LocaleContextHolder.getLocale());
            redirectAttributes.addFlashAttribute("successMessage", message);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/inventory/containers";
    }

    private void populateSelectOptions(Model model) {
        model.addAttribute("grids", gridService.findAll(null, null, Pageable.unpaged()).getContent());
    }
}
