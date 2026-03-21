package com.solusi.erp.inventory.controller;

import com.solusi.erp.core.dto.ApiResponse;
import com.solusi.erp.inventory.dto.ProductRequest;
import com.solusi.erp.inventory.dto.ProductResponse;
import com.solusi.erp.inventory.mapper.ProductMapper;
import com.solusi.erp.inventory.model.UomType;
import com.solusi.erp.inventory.service.BrandService;
import com.solusi.erp.inventory.service.ProductCategoryService;
import com.solusi.erp.inventory.service.ProductService;
import com.solusi.erp.inventory.service.UnitOfMeasureService;
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

import com.solusi.erp.core.annotation.DefaultRedirectUrl;

@Controller
@RequestMapping("/inventory/products")
@RequiredArgsConstructor
@DefaultRedirectUrl
public class ProductController {

    private final ProductService service;
    private final UnitOfMeasureService uomService;
    private final MessageSource messageSource;

    @GetMapping
    @PreAuthorize("hasAuthority('PRODUCT_READ')")
    public String list(
            @RequestParam(required = false) String keyword,
            Pageable pageable,
            Model model) {
        model.addAttribute("page", service.findAll(keyword, pageable));
        model.addAttribute("keyword", keyword);
        return "inventory/products/list";
    }

    @GetMapping("/create")
    @PreAuthorize("hasAuthority('PRODUCT_CREATE')")
    public String showCreateForm(Model model) {
        model.addAttribute("productRequest", new ProductRequest());
        populateSelectOptions(model);
        return "inventory/products/form";
    }

    @PostMapping("/create")
    @PreAuthorize("hasAuthority('PRODUCT_CREATE')")
    @ResponseBody
    public ResponseEntity<ApiResponse<ProductResponse>> create(@Valid @RequestBody ProductRequest request) {
        ProductResponse data = service.create(request);
        String msg = messageSource.getMessage("msg.success.create", null, LocaleContextHolder.getLocale());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(msg, data));
    }

    @GetMapping("/edit/{id}")
    @PreAuthorize("hasAuthority('PRODUCT_UPDATE')")
    public String showEditForm(@PathVariable Long id, Model model) {
        var viewDto = service.getProductEditView(id);
        model.addAttribute("productRequest", viewDto.getRequest());
        model.addAttribute("productUIForm", viewDto.getUi());
        model.addAttribute("auditInfo", viewDto.getAudit());
        populateSelectOptions(model);
        return "inventory/products/form";
    }

    @PostMapping("/edit/{id}")
    @PreAuthorize("hasAuthority('PRODUCT_UPDATE')")
    @ResponseBody
    public ResponseEntity<ApiResponse<ProductResponse>> update(@PathVariable Long id, @Valid @RequestBody ProductRequest request) {
        ProductResponse data = service.update(id, request);
        String msg = messageSource.getMessage("msg.success.update", null, LocaleContextHolder.getLocale());
        return ResponseEntity.ok(ApiResponse.success(msg, data));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('PRODUCT_DELETE')")
    @ResponseBody
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok()
                .header("HX-Trigger", "refresh-table")
                .build();
    }

    private void populateSelectOptions(Model model) {
        model.addAttribute("uoms", uomService.findByType(UomType.UNIT));
        model.addAttribute("weightUoms", uomService.findByType(UomType.WEIGHT));
        model.addAttribute("lengthUoms", uomService.findByType(UomType.LENGTH));
    }
}
