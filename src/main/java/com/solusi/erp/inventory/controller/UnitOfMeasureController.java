package com.solusi.erp.inventory.controller;

import com.solusi.erp.core.annotation.DefaultRedirectUrl;
import com.solusi.erp.core.dto.ApiResponse;
import com.solusi.erp.inventory.dto.UnitOfMeasureRequest;
import com.solusi.erp.inventory.dto.UnitOfMeasureResponse;
import com.solusi.erp.inventory.model.UomType;
import com.solusi.erp.inventory.service.UnitOfMeasureService;
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
 * Controller for Unit of Measure CRUD.
 */
@Controller
@RequestMapping("/inventory/unit-of-measures")
@RequiredArgsConstructor
@DefaultRedirectUrl
public class UnitOfMeasureController {

    private final UnitOfMeasureService service;
    private final MessageSource messageSource;

    @GetMapping
    @PreAuthorize("hasAuthority('UNIT-OF-MEASURE_READ')")
    public String list(@RequestParam(value = "keyword", required = false) String keyword,
                       Pageable pageable,
                       Model model) {
        model.addAttribute("page", service.findAll(keyword, pageable));
        model.addAttribute("keyword", keyword);
        return "inventory/unit-of-measures/list";
    }

    @GetMapping("/create")
    @PreAuthorize("hasAuthority('UNIT-OF-MEASURE_CREATE')")
    public String showCreateForm(Model model) {
        model.addAttribute("unitOfMeasureRequest", new UnitOfMeasureRequest());
        model.addAttribute("types", UomType.values());
        return "inventory/unit-of-measures/form";
    }

    @PostMapping("/create")
    @PreAuthorize("hasAuthority('UNIT-OF-MEASURE_CREATE')")
    @ResponseBody
    public ResponseEntity<ApiResponse<UnitOfMeasureResponse>> create(@Valid @RequestBody UnitOfMeasureRequest request) {
        UnitOfMeasureResponse data = service.create(request);
        String msg = messageSource.getMessage("msg.success.create", null, LocaleContextHolder.getLocale());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(msg, data));
    }

    @GetMapping("/edit/{id}")
    @PreAuthorize("hasAuthority('UNIT-OF-MEASURE_UPDATE')")
    public String showEditForm(@PathVariable Long id, Model model) {
        var viewDto = service.getFormView(id);
        model.addAttribute("unitOfMeasureRequest", viewDto.getRequest());
        model.addAttribute("auditInfo", viewDto.getAudit());
        model.addAttribute("types", UomType.values());
        return "inventory/unit-of-measures/form";
    }

    @PostMapping("/edit/{id}")
    @PreAuthorize("hasAuthority('UNIT-OF-MEASURE_UPDATE')")
    @ResponseBody
    public ResponseEntity<ApiResponse<UnitOfMeasureResponse>> update(@PathVariable Long id, @Valid @RequestBody UnitOfMeasureRequest request) {
        UnitOfMeasureResponse data = service.update(id, request);
        String msg = messageSource.getMessage("msg.success.update", null, LocaleContextHolder.getLocale());
        return ResponseEntity.ok(ApiResponse.success(msg, data));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('UNIT-OF-MEASURE_DELETE')")
    @ResponseBody
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        String msg = messageSource.getMessage("msg.success.delete", null, LocaleContextHolder.getLocale());
        return HtmxResponseUtility.okWithRefreshTableAndSuccess(msg);
    }
}
