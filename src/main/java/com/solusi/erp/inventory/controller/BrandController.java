package com.solusi.erp.inventory.controller;

import com.solusi.erp.core.dto.ApiResponse;
import com.solusi.erp.inventory.dto.BrandRequest;
import com.solusi.erp.inventory.dto.BrandResponse;
import com.solusi.erp.inventory.service.BrandService;
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
 * Controller for Brand CRUD.
 */
@Controller
@RequestMapping("/inventory/brands")
@RequiredArgsConstructor
public class BrandController {

    private final BrandService service;
    private final MessageSource messageSource;

    @GetMapping
    @PreAuthorize("hasAuthority('BRAND_READ')")
    public String list(@RequestParam(value = "keyword", required = false) String keyword,
                       Pageable pageable,
                       Model model) {
        model.addAttribute("page", service.findAll(keyword, pageable));
        model.addAttribute("keyword", keyword);
        return "inventory/brands/list";
    }

    @GetMapping("/create")
    @PreAuthorize("hasAuthority('BRAND_CREATE')")
    public String showCreateForm(Model model) {
        model.addAttribute("brandRequest", new BrandRequest());
        return "inventory/brands/form";
    }

    @PostMapping("/create")
    @PreAuthorize("hasAuthority('BRAND_CREATE')")
    @ResponseBody
    public ResponseEntity<ApiResponse<BrandResponse>> create(@Valid @RequestBody BrandRequest request) {
        BrandResponse data = service.create(request);
        String msg = messageSource.getMessage("msg.success.create", null, LocaleContextHolder.getLocale());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(msg, data));
    }

    @GetMapping("/edit/{id}")
    @PreAuthorize("hasAuthority('BRAND_UPDATE')")
    public String showEditForm(@PathVariable Long id, Model model) {
        try {
            var viewDto = service.getFormView(id);
            model.addAttribute("brandRequest", viewDto.getRequest());
            model.addAttribute("auditInfo", viewDto.getAudit());
            return "inventory/brands/form";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "redirect:/inventory/brands";
        }
    }

    @PostMapping("/edit/{id}")
    @PreAuthorize("hasAuthority('BRAND_UPDATE')")
    @ResponseBody
    public ResponseEntity<ApiResponse<BrandResponse>> update(@PathVariable Long id, @Valid @RequestBody BrandRequest request) {
        BrandResponse data = service.update(id, request);
        String msg = messageSource.getMessage("msg.success.update", null, LocaleContextHolder.getLocale());
        return ResponseEntity.ok(ApiResponse.success(msg, data));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('BRAND_DELETE')")
    @ResponseBody
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok()
                .header("HX-Trigger", "refresh-table")
                .build();
    }
}
