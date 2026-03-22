package com.solusi.erp.inventory.controller;

import com.solusi.erp.core.annotation.DefaultRedirectUrl;
import com.solusi.erp.core.dto.ApiResponse;
import com.solusi.erp.inventory.dto.ProductUomConversionRequest;
import com.solusi.erp.inventory.dto.ProductUomConversionResponse;
import com.solusi.erp.inventory.model.UomType;
import com.solusi.erp.inventory.service.ProductUomConversionService;
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

@Controller
@RequestMapping("/inventory/uom-conversions")
@RequiredArgsConstructor
@DefaultRedirectUrl
public class ProductUomConversionController {

    private final ProductUomConversionService service;
    private final UnitOfMeasureService uomService;
    private final MessageSource messageSource;

    @GetMapping
    @PreAuthorize("hasAuthority('UOM-CONVERSION_READ')")
    public String list(@RequestParam(value = "keyword", required = false) String keyword,
            Pageable pageable,
            Model model) {
        model.addAttribute("page", service.findAll(keyword, pageable));
        model.addAttribute("keyword", keyword);
        return "inventory/uom-conversions/list";
    }

    @GetMapping("/create")
    @PreAuthorize("hasAuthority('UOM-CONVERSION_CREATE')")
    public String showCreateForm(Model model) {
        model.addAttribute("uomConversionRequest", new ProductUomConversionRequest());
        populateSelectOptions(model);
        return "inventory/uom-conversions/form";
    }

    @PostMapping("/create")
    @PreAuthorize("hasAuthority('UOM-CONVERSION_CREATE')")
    @ResponseBody
    public ResponseEntity<ApiResponse<ProductUomConversionResponse>> create(@Valid @RequestBody ProductUomConversionRequest request) {
        ProductUomConversionResponse data = service.create(request);
        String msg = messageSource.getMessage("msg.success.create", null, LocaleContextHolder.getLocale());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(msg, data));
    }

    @GetMapping("/edit/{id}")
    @PreAuthorize("hasAuthority('UOM-CONVERSION_UPDATE')")
    public String showEditForm(@PathVariable Long id, Model model) {
        var viewDto = service.getFormView(id);
        model.addAttribute("uomConversionRequest", viewDto.getRequest());
        model.addAttribute("uomUIForm", viewDto.getUi());
        model.addAttribute("auditInfo", viewDto.getAudit());
        populateSelectOptions(model);
        return "inventory/uom-conversions/form";
    }

    @PostMapping("/edit/{id}")
    @PreAuthorize("hasAuthority('UOM-CONVERSION_UPDATE')")
    @ResponseBody
    public ResponseEntity<ApiResponse<ProductUomConversionResponse>> update(@PathVariable Long id, @Valid @RequestBody ProductUomConversionRequest request) {
        ProductUomConversionResponse data = service.update(id, request);
        String msg = messageSource.getMessage("msg.success.update", null, LocaleContextHolder.getLocale());
        return ResponseEntity.ok(ApiResponse.success(msg, data));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('UOM-CONVERSION_DELETE')")
    @ResponseBody
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        String msg = messageSource.getMessage("msg.success.delete", null, LocaleContextHolder.getLocale());
        return HtmxResponseUtility.okWithRefreshTableAndSuccess(msg);
    }

    private void populateSelectOptions(Model model) {
        model.addAttribute("uoms", uomService.findByType(UomType.UNIT));
    }
}
