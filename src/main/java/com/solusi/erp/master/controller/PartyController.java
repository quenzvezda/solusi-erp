package com.solusi.erp.master.controller;

import com.solusi.erp.master.dto.PartyRequest;
import com.solusi.erp.master.model.AddressType;
import com.solusi.erp.master.model.PartyType;
import com.solusi.erp.master.service.PartyService;
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
 * Controller for Party (Business Partner) CRUD.
 */
@Controller
@RequestMapping("/master/parties")
@RequiredArgsConstructor
public class PartyController {

    private final PartyService service;
    private final MessageSource messageSource;

    @GetMapping
    @PreAuthorize("hasAuthority('PARTY_READ')")
    public String list(@RequestParam(value = "keyword", required = false) String keyword,
                       Pageable pageable,
                       Model model) {
        model.addAttribute("page", service.findAll(keyword, pageable));
        model.addAttribute("keyword", keyword);
        return "master/parties/list";
    }

    @GetMapping("/create")
    @PreAuthorize("hasAuthority('PARTY_CREATE')")
    public String showCreateForm(Model model) {
        model.addAttribute("partyRequest", new PartyRequest());
        populateFormOptions(model);
        return "master/parties/form";
    }

    @PostMapping("/create")
    @PreAuthorize("hasAuthority('PARTY_CREATE')")
    public String create(@Valid @ModelAttribute("partyRequest") PartyRequest request,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            populateFormOptions(model);
            return "master/parties/form";
        }

        try {
            service.create(request);
            String message = messageSource.getMessage("msg.success.create", null, LocaleContextHolder.getLocale());
            redirectAttributes.addFlashAttribute("successMessage", message);
            return "redirect:/master/parties";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            populateFormOptions(model);
            return "master/parties/form";
        }
    }

    @GetMapping("/edit/{id}")
    @PreAuthorize("hasAuthority('PARTY_UPDATE')")
    public String showEditForm(@PathVariable Long id, Model model) {
        try {
            model.addAttribute("partyRequest", service.getEditData(id));
            populateFormOptions(model);
            return "master/parties/form";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "redirect:/master/parties";
        }
    }

    @PostMapping("/edit/{id}")
    @PreAuthorize("hasAuthority('PARTY_UPDATE')")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("partyRequest") PartyRequest request,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            populateFormOptions(model);
            return "master/parties/form";
        }

        try {
            service.update(id, request);
            String message = messageSource.getMessage("msg.success.update", null, LocaleContextHolder.getLocale());
            redirectAttributes.addFlashAttribute("successMessage", message);
            return "redirect:/master/parties";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            populateFormOptions(model);
            return "master/parties/form";
        }
    }

    @PostMapping("/delete/{id}")
    @PreAuthorize("hasAuthority('PARTY_DELETE')")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            service.delete(id);
            String message = messageSource.getMessage("msg.success.delete", null, LocaleContextHolder.getLocale());
            redirectAttributes.addFlashAttribute("successMessage", message);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/master/parties";
    }

    private void populateFormOptions(Model model) {
        model.addAttribute("partyTypes", PartyType.values());
        model.addAttribute("addressTypes", AddressType.values());
        model.addAttribute("roleTypes", service.findAllRoleTypes());
        model.addAttribute("idTypes", service.findAllIdTypes());
    }
}
