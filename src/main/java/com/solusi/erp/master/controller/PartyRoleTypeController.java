package com.solusi.erp.master.controller;

import com.solusi.erp.master.dto.PartyRoleTypeRequest;
import com.solusi.erp.master.service.PartyRoleTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller for PartyRoleType (Party Role Type) CRUD Management.
 */
@Controller
@RequestMapping("/master/party-role-types")
@RequiredArgsConstructor
public class PartyRoleTypeController {

    private final PartyRoleTypeService service;
    private final MessageSource messageSource;

    @GetMapping
    @PreAuthorize("hasAuthority('PARTY-ROLE-TYPE_READ')")
    public String list(@RequestParam(value = "keyword", required = false) String keyword,
            Pageable pageable,
            Model model) {
        model.addAttribute("page", service.findAll(keyword, pageable));
        model.addAttribute("keyword", keyword);
        return "master/party-role-types/list";
    }

    @GetMapping("/create")
    @PreAuthorize("hasAuthority('PARTY-ROLE-TYPE_CREATE')")
    public String showCreateForm(Model model) {
        model.addAttribute("roleTypeRequest", new PartyRoleTypeRequest());
        return "master/party-role-types/form";
    }

    @PostMapping("/create")
    @PreAuthorize("hasAuthority('PARTY-ROLE-TYPE_CREATE')")
    public String create(@ModelAttribute("roleTypeRequest") PartyRoleTypeRequest request,
            Model model,
            RedirectAttributes redirectAttributes) {
        try {
            service.create(request);
            redirectAttributes.addFlashAttribute("successMessage",
                    msg("msg.success.create"));
            return "redirect:/master/party-role-types";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "master/party-role-types/form";
        }
    }

    @GetMapping("/edit/{id}")
    @PreAuthorize("hasAuthority('PARTY-ROLE-TYPE_UPDATE')")
    public String showEditForm(@PathVariable Long id, Model model) {
        try {
            model.addAttribute("roleTypeRequest", service.getEditData(id));
            return "master/party-role-types/form";
        } catch (Exception e) {
            return "redirect:/master/party-role-types";
        }
    }

    @PostMapping("/edit/{id}")
    @PreAuthorize("hasAuthority('PARTY-ROLE-TYPE_UPDATE')")
    public String update(@PathVariable Long id,
            @ModelAttribute("roleTypeRequest") PartyRoleTypeRequest request,
            Model model,
            RedirectAttributes redirectAttributes) {
        try {
            service.update(id, request);
            redirectAttributes.addFlashAttribute("successMessage",
                    msg("msg.success.update"));
            return "redirect:/master/party-role-types";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "master/party-role-types/form";
        }
    }

    @PostMapping("/delete/{id}")
    @PreAuthorize("hasAuthority('PARTY-ROLE-TYPE_DELETE')")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            service.delete(id);
            redirectAttributes.addFlashAttribute("successMessage",
                    msg("msg.success.delete"));
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/master/party-role-types";
    }

    private String msg(String key) {
        return messageSource.getMessage(key, null, LocaleContextHolder.getLocale());
    }
}
