package com.solusi.erp.inventory.controller;

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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller for Product Category CRUD.
 */
@Controller
@RequestMapping("/inventory/product-categories")
@RequiredArgsConstructor
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
        try {
            var viewDto = service.getFormView(id);
            model.addAttribute("productCategoryRequest", viewDto.getRequest());
            model.addAttribute("auditInfo", viewDto.getAudit());
            model.addAttribute("types", ProductCategoryType.values());
            return "inventory/product-categories/form";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "redirect:/inventory/product-categories";
        }
    }

    @PostMapping("/edit/{id}")
    @PreAuthorize("hasAuthority('PRODUCT-CATEGORY_UPDATE')")
    @ResponseBody
    public ResponseEntity<ApiResponse<ProductCategoryResponse>> update(@PathVariable Long id, @Valid @RequestBody ProductCategoryRequest request) {
        ProductCategoryResponse data = service.update(id, request);
        String msg = messageSource.getMessage("msg.success.update", null, LocaleContextHolder.getLocale());
        return ResponseEntity.ok(ApiResponse.success(msg, data));
    }

    @PostMapping("/delete/{id}")
    @PreAuthorize("hasAuthority('PRODUCT-CATEGORY_DELETE')")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            service.delete(id);
            String message = messageSource.getMessage("msg.success.delete", null, LocaleContextHolder.getLocale());
            redirectAttributes.addFlashAttribute("successMessage", message);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/inventory/product-categories";
    }
}
