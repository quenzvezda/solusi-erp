package com.solusi.erp.inventory.controller;

import com.solusi.erp.inventory.dto.FacilityRequest;
import com.solusi.erp.inventory.service.FacilityService;
import com.solusi.erp.master.service.GeographicService;
import com.solusi.erp.master.service.PartyService;
import com.solusi.erp.master.model.GeographicType;
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
 * Controller for Facility CRUD.
 */
@Controller
@RequestMapping("/inventory/facilities")
@RequiredArgsConstructor
public class FacilityController {

    private final FacilityService service;
    private final PartyService partyService;
    private final GeographicService geographicService;
    private final MessageSource messageSource;

    @GetMapping
    @PreAuthorize("hasAuthority('FACILITY_READ')")
    public String list(@RequestParam(value = "keyword", required = false) String keyword,
            Pageable pageable,
            Model model) {
        model.addAttribute("page", service.findAll(keyword, pageable));
        model.addAttribute("keyword", keyword);
        return "inventory/facilities/list";
    }

    @GetMapping("/create")
    @PreAuthorize("hasAuthority('FACILITY_CREATE')")
    public String showCreateForm(Model model) {
        model.addAttribute("facilityRequest", new FacilityRequest());
        populateSelectOptions(model);
        return "inventory/facilities/form";
    }

    @PostMapping("/create")
    @PreAuthorize("hasAuthority('FACILITY_CREATE')")
    public String create(@Valid @ModelAttribute("facilityRequest") FacilityRequest request,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            populateSelectOptions(model);
            return "inventory/facilities/form";
        }

        try {
            service.create(request);
            String message = messageSource.getMessage("msg.success.create", null, LocaleContextHolder.getLocale());
            redirectAttributes.addFlashAttribute("successMessage", message);
            return "redirect:/inventory/facilities";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            populateSelectOptions(model);
            return "inventory/facilities/form";
        }
    }

    @GetMapping("/edit/{id}")
    @PreAuthorize("hasAuthority('FACILITY_UPDATE')")
    public String showEditForm(@PathVariable Long id, Model model) {
        try {
            model.addAttribute("facilityRequest", service.getEditData(id));
            model.addAttribute("facilityResponse", service.findById(id));
            populateSelectOptions(model);
            return "inventory/facilities/form";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "redirect:/inventory/facilities";
        }
    }

    @PostMapping("/edit/{id}")
    @PreAuthorize("hasAuthority('FACILITY_UPDATE')")
    public String update(@PathVariable Long id,
            @Valid @ModelAttribute("facilityRequest") FacilityRequest request,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            populateSelectOptions(model);
            return "inventory/facilities/form";
        }

        try {
            service.update(id, request);
            String message = messageSource.getMessage("msg.success.update", null, LocaleContextHolder.getLocale());
            redirectAttributes.addFlashAttribute("successMessage", message);
            return "redirect:/inventory/facilities";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            populateSelectOptions(model);
            return "inventory/facilities/form";
        }
    }

    @PostMapping("/delete/{id}")
    @PreAuthorize("hasAuthority('FACILITY_DELETE')")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            service.delete(id);
            String message = messageSource.getMessage("msg.success.delete", null, LocaleContextHolder.getLocale());
            redirectAttributes.addFlashAttribute("successMessage", message);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/inventory/facilities";
    }

    private void populateSelectOptions(Model model) {
        model.addAttribute("owners", partyService.findAll(null, Pageable.unpaged()).getContent());
        model.addAttribute("cities", geographicService.findByType(GeographicType.CITY_MUNICIPALITY));
    }
}
