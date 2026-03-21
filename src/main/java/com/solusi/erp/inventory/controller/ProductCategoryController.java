package com.solusi.erp.inventory.controller;

import com.solusi.erp.core.annotation.DefaultRedirectUrl;
import com.solusi.erp.core.dto.ApiResponse;
import com.solusi.erp.inventory.dto.ProductCategoryRequest;
import com.solusi.erp.inventory.dto.ProductCategoryResponse;
import com.solusi.erp.inventory.model.ProductCategoryType;
import com.solusi.erp.inventory.service.ProductCategoryService;
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
 * Controller for Product Category CRUD.
 */
@Controller
@RequestMapping("/inventory/product-categories")
@RequiredArgsConstructor
@DefaultRedirectUrl
public class ProductCategoryController {

    private final ProductCategoryService service;
    private final MessageSource messageSource;

    @GetMapping
    @PreAuthorize("hasAuthority('PRODUCT-CATEGORY_READ')")
    public String list(@RequestParam(value = "keyword", required = false) String keyword,
                       Pageable pageable,
                       Model model) {
        model.addAttribute("page", service.findAll(keyword, pageable));
        model.addAttribute("keyword", keyword);
        return "inventory/product-categories/list";
    }

    @GetMapping("/create")
    @PreAuthorize("hasAuthority('PRODUCT-CATEGORY_CREATE')")
    public String showCreateForm(Model model) {
        model.addAttribute("productCategoryRequest", new ProductCategoryRequest());
        model.addAttribute("types", ProductCategoryType.values());
        return "inventory/product-categories/form";
    }

    @PostMapping("/create")
    @PreAuthorize("hasAuthority('PRODUCT-CATEGORY_CREATE')")
    @ResponseBody
    public ResponseEntity<ApiResponse<ProductCategoryResponse>> create(@Valid @RequestBody ProductCategoryRequest request) {
        ProductCategoryResponse data = service.create(request);
        String msg = messageSource.getMessage("msg.success.create", null, LocaleContextHolder.getLocale());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(msg, data));
    }

    @GetMapping("/edit/{id}")
    @PreAuthorize("hasAuthority('PRODUCT-CATEGORY_UPDATE')")
    public String showEditForm(@PathVariable Long id, Model model) {
        var viewDto = service.getFormView(id);
        model.addAttribute("productCategoryRequest", viewDto.getRequest());
        model.addAttribute("auditInfo", viewDto.getAudit());
        model.addAttribute("types", ProductCategoryType.values());
        return "inventory/product-categories/form";
    }

    @PostMapping("/edit/{id}")
    @PreAuthorize("hasAuthority('PRODUCT-CATEGORY_UPDATE')")
    @ResponseBody
    public ResponseEntity<ApiResponse<ProductCategoryResponse>> update(@PathVariable Long id, @Valid @RequestBody ProductCategoryRequest request) {
        ProductCategoryResponse data = service.update(id, request);
        String msg = messageSource.getMessage("msg.success.update", null, LocaleContextHolder.getLocale());
        return ResponseEntity.ok(ApiResponse.success(msg, data));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('PRODUCT-CATEGORY_DELETE')")
    @ResponseBody
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok()
                .header("HX-Trigger", "refresh-table")
                .build();
    }
}
