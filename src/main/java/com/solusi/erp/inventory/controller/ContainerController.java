package com.solusi.erp.inventory.controller;

import com.solusi.erp.core.annotation.DefaultRedirectUrl;
import com.solusi.erp.core.dto.ApiResponse;
import com.solusi.erp.inventory.dto.ContainerRequest;
import com.solusi.erp.inventory.dto.ContainerResponse;
import com.solusi.erp.inventory.service.ContainerService;
import com.solusi.erp.inventory.service.GridService;
import com.solusi.erp.util.HtmxResponseUtility;
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
 * Controller for Container CRUD.
 */
@Controller
@RequestMapping("/inventory/containers")
@RequiredArgsConstructor
@DefaultRedirectUrl
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
    @ResponseBody
    public ResponseEntity<ApiResponse<ContainerResponse>> create(@Valid @RequestBody ContainerRequest request) {
        ContainerResponse data = service.create(request);
        String msg = messageSource.getMessage("msg.success.create", null, LocaleContextHolder.getLocale());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(msg, data));
    }

    @GetMapping("/edit/{id}")
    @PreAuthorize("hasAuthority('CONTAINER_UPDATE')")
    public String showEditForm(@PathVariable Long id, Model model) {
        var viewDto = service.getFormView(id);
        model.addAttribute("containerRequest", viewDto.getRequest());
        model.addAttribute("containerUI", viewDto.getUi());
        model.addAttribute("auditInfo", viewDto.getAudit());
        populateSelectOptions(model);
        return "inventory/containers/form";
    }

    @PostMapping("/edit/{id}")
    @PreAuthorize("hasAuthority('CONTAINER_UPDATE')")
    @ResponseBody
    public ResponseEntity<ApiResponse<ContainerResponse>> update(@PathVariable Long id, @Valid @RequestBody ContainerRequest request) {
        ContainerResponse data = service.update(id, request);
        String msg = messageSource.getMessage("msg.success.update", null, LocaleContextHolder.getLocale());
        return ResponseEntity.ok(ApiResponse.success(msg, data));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('CONTAINER_DELETE')")
    @ResponseBody
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        String msg = messageSource.getMessage("msg.success.delete", null, LocaleContextHolder.getLocale());
        return HtmxResponseUtility.okWithRefreshTableAndSuccess(msg);
    }

    private void populateSelectOptions(Model model) {
        model.addAttribute("grids", gridService.findAll(null, null, Pageable.unpaged()).getContent());
    }
}
