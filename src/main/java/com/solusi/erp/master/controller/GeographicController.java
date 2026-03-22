package com.solusi.erp.master.controller;

import com.solusi.erp.core.annotation.DefaultRedirectUrl;
import com.solusi.erp.core.dto.ApiResponse;
import com.solusi.erp.master.dto.GeographicRequest;
import com.solusi.erp.master.dto.GeographicResponse;
import com.solusi.erp.master.model.GeographicType;
import com.solusi.erp.master.service.GeographicService;
import com.solusi.erp.util.HtmxResponseUtility;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
@Controller
@RequestMapping("/master/geographics")
@RequiredArgsConstructor
@DefaultRedirectUrl
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

        Page<GeographicResponse> geographics;
        if (parentId != null) {
            geographics = geographicService.getByParent(parentId, pageable);
            model.addAttribute("currentParent", geographicService.getById(parentId));
        } else {
            geographics = geographicService.getAllGeographics(keyword, pageable);
        }

        model.addAttribute("page", geographics);
        model.addAttribute("parentId", parentId);
        return "master/geographic/list";
    }

    @GetMapping("/create")
    @PreAuthorize("hasAuthority('GEOGRAPHIC_CREATE')")
    public String showCreateForm(Model model, @RequestParam(value = "parentId", required = false) Long parentId) {
        GeographicRequest request = new GeographicRequest();
        request.setIsActive(true);
        if (parentId != null) {
            GeographicResponse parent = geographicService.getById(parentId);
            request.setParentId(parentId);
            model.addAttribute("parentName", parent.getName());

            // Auto-set type based on parent
            if (parent.getType() == GeographicType.COUNTRY) {
                request.setType(GeographicType.STATE_PROVINCE);
            } else if (parent.getType() == GeographicType.STATE_PROVINCE) {
                request.setType(GeographicType.CITY_MUNICIPALITY);
            }
        }

        model.addAttribute("geographic", request);
        model.addAttribute("types", GeographicType.values());
        return "master/geographic/form";
    }

    @PostMapping("/create")
    @PreAuthorize("hasAuthority('GEOGRAPHIC_CREATE')")
    @ResponseBody
    public ResponseEntity<ApiResponse<GeographicResponse>> create(@Valid @RequestBody GeographicRequest request) {
        GeographicResponse data = geographicService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(getMessage("msg.success.create"), data));
    }

    @GetMapping("/edit/{id}")
    @PreAuthorize("hasAuthority('GEOGRAPHIC_UPDATE')")
    public String showEditForm(@PathVariable Long id, Model model) {
        var viewDto = geographicService.getGeographicEditView(id);
        model.addAttribute("geographic", viewDto.getRequest());
        model.addAttribute("geographicUI", viewDto.getUi());
        model.addAttribute("auditInfo", viewDto.getAudit());
        model.addAttribute("types", GeographicType.values());
        return "master/geographic/form";
    }

    @PostMapping("/edit/{id}")
    @PreAuthorize("hasAuthority('GEOGRAPHIC_UPDATE')")
    @ResponseBody
    public ResponseEntity<ApiResponse<GeographicResponse>> update(@PathVariable Long id, @Valid @RequestBody GeographicRequest request) {
        GeographicResponse data = geographicService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success(getMessage("msg.success.update"), data));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('GEOGRAPHIC_DELETE')")
    @ResponseBody
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        geographicService.delete(id);
        return HtmxResponseUtility.okWithRefreshTableAndSuccess(getMessage("msg.success.delete"));
    }

    // ===================================================================
    // API Endpoints for Dynamic UI
    // ===================================================================

    @GetMapping("/api/hierarchy/{id}")
    @ResponseBody
    @PreAuthorize("hasAuthority('GEOGRAPHIC_READ')")
    public List<GeographicResponse> getHierarchy(@PathVariable Long id) {
        List<GeographicResponse> hierarchy = new ArrayList<>();
        GeographicResponse current = geographicService.getById(id);
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
