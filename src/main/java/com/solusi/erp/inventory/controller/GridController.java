package com.solusi.erp.inventory.controller;

import com.solusi.erp.core.dto.ApiResponse;
import com.solusi.erp.inventory.dto.GridRequest;
import com.solusi.erp.inventory.dto.GridResponse;
import com.solusi.erp.inventory.service.FacilityService;
import com.solusi.erp.inventory.service.GridService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

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
        
        if (facilityId != null) {
            try {
                // Pastikan menggunakan findById dari service yang mengembalikan DTO Response
                var facility = facilityService.findById(facilityId);
                model.addAttribute("selectedFacility", facility);
            } catch (Exception ignored) {
                // Jika tidak ditemukan, abaikan saja filternya di UI
            }
        }
        
        return "inventory/grids/list";
    }

    @GetMapping("/create")
    @PreAuthorize("hasAuthority('GRID_CREATE')")
    public String showCreateForm(@RequestParam(required = false) Long facilityId, Model model) {
        GridRequest request = new GridRequest();
        if (facilityId != null) {
            request.setFacilityId(facilityId);
        }
        model.addAttribute("gridRequest", request);
        populateSelectOptions(model);
        return "inventory/grids/form";
    }

    @PostMapping("/create")
    @PreAuthorize("hasAuthority('GRID_CREATE')")
    @ResponseBody
    public ResponseEntity<ApiResponse<GridResponse>> create(@Valid @RequestBody GridRequest request) {
        GridResponse data = service.create(request);
        String msg = messageSource.getMessage("msg.success.create", null, LocaleContextHolder.getLocale());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(msg, data));
    }

    @GetMapping("/edit/{id}")
    @PreAuthorize("hasAuthority('GRID_UPDATE')")
    public String showEditForm(@PathVariable Long id, Model model) {
        try {
            var viewDto = service.getFormView(id);
            model.addAttribute("gridRequest", viewDto.getRequest());
            model.addAttribute("auditInfo", viewDto.getAudit());
            populateSelectOptions(model);
            return "inventory/grids/form";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "redirect:/inventory/grids";
        }
    }

    @PostMapping("/edit/{id}")
    @PreAuthorize("hasAuthority('GRID_UPDATE')")
    @ResponseBody
    public ResponseEntity<ApiResponse<GridResponse>> update(@PathVariable Long id, @Valid @RequestBody GridRequest request) {
        GridResponse data = service.update(id, request);
        String msg = messageSource.getMessage("msg.success.update", null, LocaleContextHolder.getLocale());
        return ResponseEntity.ok(ApiResponse.success(msg, data));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('GRID_DELETE')")
    @ResponseBody
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok()
                .header("HX-Trigger", "refresh-table")
                .build();
    }

    private void populateSelectOptions(Model model) {
        model.addAttribute("facilities", facilityService.findAll(null, Pageable.unpaged()).getContent());
    }
}
