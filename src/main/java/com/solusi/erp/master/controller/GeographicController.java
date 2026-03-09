package com.solusi.erp.master.controller;

import com.solusi.erp.master.dto.GeographicDto;
import com.solusi.erp.master.model.GeographicType;
import com.solusi.erp.master.service.GeographicService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
@Controller
@RequestMapping("/master/geographics")
@RequiredArgsConstructor
public class GeographicController {

    private final GeographicService geographicService;
    private final MessageSource messageSource;

    private String getMessage(String key, Object... args) {
        return messageSource.getMessage(key, args, LocaleContextHolder.getLocale());
    }

    @GetMapping
    @PreAuthorize("hasAuthority('GEOGRAPHIC_READ')")
    public String list(Model model,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "parentId", required = false) Long parentId,
            Pageable pageable) {

        Page<GeographicDto> geographics;
        if (parentId != null) {
            geographics = geographicService.getByParent(parentId, pageable);
            model.addAttribute("currentParent", geographicService.getById(parentId));
        } else {
            geographics = geographicService.getAllGeographics(keyword, pageable);
        }

        model.addAttribute("page", geographics);
        model.addAttribute("keyword", keyword);
        model.addAttribute("parentId", parentId);
        return "master/geographic/list";
    }

    @GetMapping("/create")
    @PreAuthorize("hasAuthority('GEOGRAPHIC_CREATE')")
    public String showCreateForm(Model model, @RequestParam(value = "parentId", required = false) Long parentId) {
        GeographicDto dto = new GeographicDto();
        dto.setIsActive(true);
        if (parentId != null) {
            GeographicDto parent = geographicService.getById(parentId);
            dto.setParentId(parentId);
            dto.setParentName(parent.getName());

            // Auto-set type based on parent
            if (parent.getType() == GeographicType.COUNTRY) {
                dto.setType(GeographicType.STATE_PROVINCE);
            } else if (parent.getType() == GeographicType.STATE_PROVINCE) {
                dto.setType(GeographicType.CITY_MUNICIPALITY);
            }
        }

        model.addAttribute("geographic", dto);
        model.addAttribute("types", GeographicType.values());
        return "master/geographic/form";
    }

    @PostMapping("/create")
    @PreAuthorize("hasAuthority('GEOGRAPHIC_CREATE')")
    public String create(@Valid @ModelAttribute("geographic") GeographicDto dto,
            BindingResult bindingResult, Model model, RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("types", GeographicType.values());
            return "master/geographic/form";
        }

        try {
            geographicService.create(dto);
            redirectAttributes.addFlashAttribute("successMessage", getMessage("msg.success.create"));
            return "redirect:/master/geographics" + (dto.getParentId() != null ? "?parentId=" + dto.getParentId() : "");
        } catch (Exception e) {
            log.error("Error creating geographic", e);
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("types", GeographicType.values());
            return "master/geographic/form";
        }
    }

    @GetMapping("/edit/{id}")
    @PreAuthorize("hasAuthority('GEOGRAPHIC_UPDATE')")
    public String showEditForm(@PathVariable Long id, Model model) {
        GeographicDto dto = geographicService.getById(id);
        model.addAttribute("geographic", dto);
        model.addAttribute("types", GeographicType.values());
        return "master/geographic/form";
    }

    @PostMapping("/edit/{id}")
    @PreAuthorize("hasAuthority('GEOGRAPHIC_UPDATE')")
    public String update(@PathVariable Long id, @Valid @ModelAttribute("geographic") GeographicDto dto,
            BindingResult bindingResult, Model model, RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("types", GeographicType.values());
            return "master/geographic/form";
        }

        try {
            geographicService.update(id, dto);
            redirectAttributes.addFlashAttribute("successMessage", getMessage("msg.success.update"));
            return "redirect:/master/geographics" + (dto.getParentId() != null ? "?parentId=" + dto.getParentId() : "");
        } catch (Exception e) {
            log.error("Error updating geographic", e);
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("types", GeographicType.values());
            return "master/geographic/form";
        }
    }

    @GetMapping("/delete/{id}")
    @PreAuthorize("hasAuthority('GEOGRAPHIC_DELETE')")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            GeographicDto dto = geographicService.getById(id);
            Long parentId = dto.getParentId();
            geographicService.delete(id);
            redirectAttributes.addFlashAttribute("successMessage", getMessage("msg.success.delete"));
            return "redirect:/master/geographics" + (parentId != null ? "?parentId=" + parentId : "");
        } catch (Exception e) {
            log.error("Error deleting geographic", e);
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/master/geographics";
        }
    }

    // ===================================================================
    // API Endpoints for Dynamic UI
    // ===================================================================

    @GetMapping("/api/hierarchy/{id}")
    @ResponseBody
    @PreAuthorize("hasAuthority('GEOGRAPHIC_READ')")
    public List<GeographicDto> getHierarchy(@PathVariable Long id) {
        List<GeographicDto> hierarchy = new ArrayList<>();
        GeographicDto current = geographicService.getById(id);
        while (current != null) {
            hierarchy.add(current);
            if (current.getParentId() != null) {
                current = geographicService.getById(current.getParentId());
            } else {
                current = null;
            }
        }
        Collections.reverse(hierarchy);
        return hierarchy;
    }

}
