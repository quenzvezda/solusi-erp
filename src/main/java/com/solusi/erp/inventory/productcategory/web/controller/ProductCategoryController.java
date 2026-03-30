package com.solusi.erp.inventory.productcategory.web.controller;

import com.solusi.erp.core.annotation.DefaultRedirectUrl;
import com.solusi.erp.core.domain.model.Pageable;
import com.solusi.erp.core.infrastructure.util.PageableMapper;
import com.solusi.erp.core.dto.ApiResponse;
import com.solusi.erp.core.dto.LookupDto;
import com.solusi.erp.inventory.productcategory.domain.model.ProductCategoryType;
import com.solusi.erp.inventory.productcategory.application.usecase.command.*;
import com.solusi.erp.inventory.productcategory.application.usecase.query.*;
import com.solusi.erp.inventory.productcategory.domain.model.ProductCategory;
import com.solusi.erp.inventory.productcategory.web.dto.*;
import com.solusi.erp.inventory.productcategory.web.mapper.ProductCategoryWebMapper;
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

@Controller
@RequestMapping("/inventory/product-categories")
@RequiredArgsConstructor
@DefaultRedirectUrl
public class ProductCategoryController {

    private final CreateProductCategoryUseCase createProductCategoryUseCase;
    private final UpdateProductCategoryUseCase updateProductCategoryUseCase;
    private final DeleteProductCategoryUseCase deleteProductCategoryUseCase;
    private final FindProductCategoriesUseCase findProductCategoriesUseCase;
    private final GetProductCategoryEditViewUseCase getProductCategoryEditViewUseCase;
    private final GetProductCategoryLookupUseCase getProductCategoryLookupUseCase;
    private final ProductCategoryWebMapper webMapper;
    private final MessageSource messageSource;

    @GetMapping
    @PreAuthorize("hasAuthority('PRODUCT-CATEGORY_READ')")
    public String list(@RequestParam(required = false) String keyword,
                       org.springframework.data.domain.Pageable springPageable,
                       Model model) {
        Pageable domainPageable = PageableMapper.toDomain(springPageable);
        com.solusi.erp.core.domain.model.Page<ProductCategory> domainPage =
            findProductCategoriesUseCase.execute(keyword, domainPageable);

        List<ProductCategorySummaryResponse> content = domainPage.content().stream()
            .map(webMapper::toSummaryResponse)
            .collect(Collectors.toList());

        Page<ProductCategorySummaryResponse> springPage = new PageImpl<>(content, springPageable, domainPage.totalElements());
        model.addAttribute("page", springPage);
        model.addAttribute("keyword", keyword);
        return "inventory/product-categories/list";
    }

    @GetMapping("/create")
    @PreAuthorize("hasAuthority('PRODUCT-CATEGORY_CREATE')")
    public String showCreateForm(Model model) {
        model.addAttribute("productCategoryRequest", new ProductCategorySaveRequest());
        model.addAttribute("types", ProductCategoryType.values());
        return "inventory/product-categories/form";
    }

    @PostMapping("/create")
    @PreAuthorize("hasAuthority('PRODUCT-CATEGORY_CREATE')")
    @ResponseBody
    public ResponseEntity<ApiResponse<ProductCategoryDetailResponse>> create(@Valid @RequestBody ProductCategorySaveRequest request) {
        ProductCategory domain = createProductCategoryUseCase.execute(
            request.getName(), request.getType(), request.getNote());
        ProductCategoryDetailResponse data = webMapper.toDetailResponse(domain);
        String msg = messageSource.getMessage("msg.success.create", null, LocaleContextHolder.getLocale());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(msg, data));
    }

    @GetMapping("/edit/{id}")
    @PreAuthorize("hasAuthority('PRODUCT-CATEGORY_UPDATE')")
    public String showEditForm(@PathVariable Long id, Model model) {
        ProductCategory domain = getProductCategoryEditViewUseCase.execute(id)
            .orElseThrow(() -> new RuntimeException("Product category not found"));
        model.addAttribute("productCategoryRequest", webMapper.toSaveRequest(domain));
        model.addAttribute("auditInfo", webMapper.toDetailResponse(domain));
        model.addAttribute("types", ProductCategoryType.values());
        return "inventory/product-categories/form";
    }

    @PostMapping("/edit/{id}")
    @PreAuthorize("hasAuthority('PRODUCT-CATEGORY_UPDATE')")
    @ResponseBody
    public ResponseEntity<ApiResponse<ProductCategoryDetailResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody ProductCategorySaveRequest request) {
        ProductCategory domain = updateProductCategoryUseCase.execute(
            id, request.getName(), request.getType(), request.getNote());
        ProductCategoryDetailResponse data = webMapper.toDetailResponse(domain);
        String msg = messageSource.getMessage("msg.success.update", null, LocaleContextHolder.getLocale());
        return ResponseEntity.ok(ApiResponse.success(msg, data));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('PRODUCT-CATEGORY_DELETE')")
    @ResponseBody
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        deleteProductCategoryUseCase.execute(id);
        String msg = messageSource.getMessage("msg.success.delete", null, LocaleContextHolder.getLocale());
        return HtmxResponseUtility.okWithRefreshTableAndSuccess(msg);
    }

    @GetMapping("/lookup")
    @PreAuthorize("hasAuthority('LOOKUP_PRODUCT-CATEGORY')")
    @ResponseBody
    public List<LookupDto> lookup(@RequestParam(required = false) String keyword,
                                  @RequestParam(defaultValue = "10") int limit) {
        return getProductCategoryLookupUseCase.search(keyword, limit);
    }

    @GetMapping("/lookup/{id}")
    @PreAuthorize("hasAuthority('LOOKUP_PRODUCT-CATEGORY')")
    @ResponseBody
    public LookupDto lookupById(@PathVariable Long id) {
        return getProductCategoryLookupUseCase.getById(id);
    }
}
