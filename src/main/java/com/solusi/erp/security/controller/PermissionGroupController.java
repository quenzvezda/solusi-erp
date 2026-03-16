package com.solusi.erp.security.controller;

import com.solusi.erp.security.dto.PermissionGroupRequest;
import com.solusi.erp.security.service.PermissionGroupService;
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
@RequestMapping("/security/menu-groups")
@RequiredArgsConstructor
public class PermissionGroupController {

    private final PermissionGroupService service;
    private final MessageSource messageSource;

    @GetMapping
    @PreAuthorize("hasAuthority('MENU-GROUP_READ')")
    public String list(@RequestParam(value = "keyword", required = false) String keyword,
                       Pageable pageable,
                       Model model) {
        model.addAttribute("page", service.findAll(keyword, pageable));
        model.addAttribute("keyword", keyword);
        return "security/permission-groups/list";
    }

    @GetMapping("/create")
    @PreAuthorize("hasAuthority('MENU-GROUP_CREATE')")
    public String showCreateForm(Model model) {
        model.addAttribute("request", new PermissionGroupRequest());
        return "security/permission-groups/form";
    }

    @PostMapping("/create")
    @PreAuthorize("hasAuthority('MENU-GROUP_CREATE')")
    public String create(@Valid @ModelAttribute("request") PermissionGroupRequest request,
                         BindingResult bindingResult,
                         RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "security/permission-groups/form";
        }

        try {
            service.create(request);
            redirectAttributes.addFlashAttribute("successMessage", 
                messageSource.getMessage("msg.success.create", null, LocaleContextHolder.getLocale()));
            return "redirect:/security/menu-groups";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "security/permission-groups/form";
        }
    }

    @GetMapping("/edit/{id}")
    @PreAuthorize("hasAuthority('MENU-GROUP_UPDATE')")
    public String showEditForm(@PathVariable Long id, Model model) {
        try {
            model.addAttribute("request", service.getById(id));
            return "security/permission-groups/form";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "redirect:/security/menu-groups";
        }
    }

    @PostMapping("/edit/{id}")
    @PreAuthorize("hasAuthority('MENU-GROUP_UPDATE')")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("request") PermissionGroupRequest request,
                         BindingResult bindingResult,
                         RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "security/permission-groups/form";
        }

        try {
            service.update(id, request);
            redirectAttributes.addFlashAttribute("successMessage", 
                messageSource.getMessage("msg.success.update", null, LocaleContextHolder.getLocale()));
            return "redirect:/security/menu-groups";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "security/permission-groups/form";
        }
    }

    @PostMapping("/delete/{id}")
    @PreAuthorize("hasAuthority('MENU-GROUP_DELETE')")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            service.delete(id);
            redirectAttributes.addFlashAttribute("successMessage", 
                messageSource.getMessage("msg.success.delete", null, LocaleContextHolder.getLocale()));
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/security/menu-groups";
    }
}
