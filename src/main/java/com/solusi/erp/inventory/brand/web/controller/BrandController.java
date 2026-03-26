package com.solusi.erp.inventory.brand.web.controller;

import com.solusi.erp.core.annotation.DefaultRedirectUrl;
import com.solusi.erp.core.domain.model.Pageable;
import com.solusi.erp.core.dto.ApiResponse;
import com.solusi.erp.core.dto.LookupDto;
import com.solusi.erp.inventory.brand.application.usecase.command.*;
import com.solusi.erp.inventory.brand.application.usecase.query.*;
import com.solusi.erp.inventory.brand.domain.model.Brand;
import com.solusi.erp.inventory.brand.web.dto.*;
import com.solusi.erp.inventory.brand.web.mapper.BrandWebMapper;
import com.solusi.erp.util.HtmxResponseUtility;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller for Brand CRUD.
 * Replaces legacy com.solusi.erp.inventory.controller.BrandController.
 */
@Controller
@RequestMapping("/inventory/brands")
@RequiredArgsConstructor
@DefaultRedirectUrl
public class BrandController {

    private final CreateBrandUseCase createBrandUseCase;
    private final UpdateBrandUseCase updateBrandUseCase;
    private final DeleteBrandUseCase deleteBrandUseCase;
    private final FindBrandsUseCase findBrandsUseCase;
    private final GetBrandEditViewUseCase getBrandEditViewUseCase;
    private final GetBrandLookupUseCase getBrandLookupUseCase;
    private final BrandWebMapper webMapper;
    private final MessageSource messageSource;

    @GetMapping
    @PreAuthorize("hasAuthority('BRAND_READ')")
    public String list(@RequestParam(required = false) String keyword,
                       org.springframework.data.domain.Pageable springPageable,
                       Model model) {
        Pageable domainPageable = Pageable.of(springPageable.getPageNumber(), springPageable.getPageSize());
        com.solusi.erp.core.domain.model.Page<Brand> domainPage =
            findBrandsUseCase.execute(keyword, domainPageable);

        List<BrandSummaryResponse> content = domainPage.content().stream()
            .map(webMapper::toSummaryResponse)
            .collect(Collectors.toList());

        Page<BrandSummaryResponse> springPage = new PageImpl<>(content, springPageable, domainPage.totalElements());
        model.addAttribute("page", springPage);
        model.addAttribute("keyword", keyword);
        return "inventory/brands/list";
    }

    @GetMapping("/create")
    @PreAuthorize("hasAuthority('BRAND_CREATE')")
    public String showCreateForm(Model model) {
        model.addAttribute("brandRequest", new BrandSaveRequest());
        return "inventory/brands/form";
    }

    @PostMapping("/create")
    @PreAuthorize("hasAuthority('BRAND_CREATE')")
    @ResponseBody
    public ResponseEntity<ApiResponse<BrandDetailResponse>> create(@Valid @RequestBody BrandSaveRequest request) {
        Brand domain = createBrandUseCase.execute(request.getName(), request.getNote());
        BrandDetailResponse data = webMapper.toDetailResponse(domain);
        String msg = messageSource.getMessage("msg.success.create", null, LocaleContextHolder.getLocale());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(msg, data));
    }

    @GetMapping("/edit/{id}")
    @PreAuthorize("hasAuthority('BRAND_UPDATE')")
    public String showEditForm(@PathVariable Long id, Model model) {
        Brand domain = getBrandEditViewUseCase.execute(id)
            .orElseThrow(() -> new RuntimeException("Brand not found"));
        model.addAttribute("brandRequest", webMapper.toSaveRequest(domain));
        model.addAttribute("auditInfo", webMapper.toDetailResponse(domain));
        return "inventory/brands/form";
    }

    @PostMapping("/edit/{id}")
    @PreAuthorize("hasAuthority('BRAND_UPDATE')")
    @ResponseBody
    public ResponseEntity<ApiResponse<BrandDetailResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody BrandSaveRequest request) {
        Brand domain = updateBrandUseCase.execute(id, request.getName(), request.getNote());
        BrandDetailResponse data = webMapper.toDetailResponse(domain);
        String msg = messageSource.getMessage("msg.success.update", null, LocaleContextHolder.getLocale());
        return ResponseEntity.ok(ApiResponse.success(msg, data));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('BRAND_DELETE')")
    @ResponseBody
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        deleteBrandUseCase.execute(id);
        String msg = messageSource.getMessage("msg.success.delete", null, LocaleContextHolder.getLocale());
        return HtmxResponseUtility.okWithRefreshTableAndSuccess(msg);
    }

    // Lookup endpoints (used by TomSelect autocomplete)
    @GetMapping("/lookup")
    @PreAuthorize("hasAuthority('LOOKUP_BRAND')")
    @ResponseBody
    public List<LookupDto> lookup(@RequestParam(required = false) String keyword,
                                  @RequestParam(defaultValue = "10") int limit) {
        return getBrandLookupUseCase.search(keyword, limit);
    }

    @GetMapping("/lookup/{id}")
    @PreAuthorize("hasAuthority('LOOKUP_BRAND')")
    @ResponseBody
    public LookupDto lookupById(@PathVariable Long id) {
        return getBrandLookupUseCase.getById(id);
    }
}
