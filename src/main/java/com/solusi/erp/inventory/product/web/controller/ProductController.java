package com.solusi.erp.inventory.product.web.controller;

import com.solusi.erp.core.dto.ApiResponse;
import com.solusi.erp.core.domain.model.Pageable;
import com.solusi.erp.core.infrastructure.util.PageableMapper;
import com.solusi.erp.inventory.model.UomType;
import com.solusi.erp.inventory.product.application.usecase.command.*;
import com.solusi.erp.inventory.product.application.usecase.query.*;
import com.solusi.erp.inventory.product.web.dto.*;
import com.solusi.erp.inventory.product.web.mapper.ProductWebMapper;
import com.solusi.erp.inventory.uom.application.usecase.query.GetUomLookupUseCase;
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

import com.solusi.erp.core.annotation.DefaultRedirectUrl;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/inventory/products")
@RequiredArgsConstructor
@DefaultRedirectUrl
public class ProductController {

    private final FindProductsUseCase findProductsUseCase;
    private final GetProductUseCase getProductUseCase;
    private final CreateProductUseCase createProductUseCase;
    private final UpdateProductUseCase updateProductUseCase;
    private final DeleteProductUseCase deleteProductUseCase;
    private final GetProductEditViewUseCase getProductEditViewUseCase;
    private final GetProductLookupUseCase getProductLookupUseCase;
    
    private final GetUomLookupUseCase getUomLookupUseCase;
    private final ProductWebMapper webMapper;
    private final MessageSource messageSource;

    @GetMapping
    @PreAuthorize("hasAuthority('PRODUCT_READ')")
    public String list(
            @RequestParam(required = false) String keyword,
            org.springframework.data.domain.Pageable springPageable,
            Model model) {
        
        Pageable domainPageable = PageableMapper.toDomain(springPageable);
        com.solusi.erp.core.domain.model.Page<com.solusi.erp.inventory.product.domain.model.Product> domainPage = 
            findProductsUseCase.execute(keyword, domainPageable);
            
        List<ProductSummaryResponse> content = domainPage.content().stream()
            .map(webMapper::toSummaryResponse)
            .collect(Collectors.toList());
            
        Page<ProductSummaryResponse> springPage = new PageImpl<>(content, springPageable, domainPage.totalElements());
            
        model.addAttribute("page", springPage);
        model.addAttribute("keyword", keyword);
        return "inventory/products/list";
    }

    @GetMapping("/create")
    @PreAuthorize("hasAuthority('PRODUCT_CREATE')")
    public String showCreateForm(Model model) {
        model.addAttribute("productRequest", new ProductSaveRequest());
        populateSelectOptions(model);
        return "inventory/products/form";
    }

    @PostMapping("/create")
    @PreAuthorize("hasAuthority('PRODUCT_CREATE')")
    @ResponseBody
    public ResponseEntity<ApiResponse<ProductDetailResponse>> create(@Valid @RequestBody ProductSaveRequest request) {
        com.solusi.erp.inventory.product.domain.model.Product domain = createProductUseCase.execute(
            request.getName(), request.getBarcode(), request.getNote(),
            request.getCategoryId(), request.getUomId(), request.getBrandId(),
            request.getHscode(), request.getIsActive(), request.getIsSerialized(),
            request.getMinStock(), request.getMaxStock(),
            request.getWeightNet(), request.getWeightGross(), request.getWeightUomId(),
            request.getLength(), request.getWidth(), request.getHeight(), request.getDimensionUomId()
        );
        
        ProductDetailResponse data = webMapper.toDetailResponse(domain);
        String msg = messageSource.getMessage("msg.success.create", null, LocaleContextHolder.getLocale());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(msg, data));
    }

    @GetMapping("/edit/{id}")
    @PreAuthorize("hasAuthority('PRODUCT_UPDATE')")
    public String showEditForm(@PathVariable Long id, Model model) {
        com.solusi.erp.inventory.product.domain.model.Product domain = getProductEditViewUseCase.execute(id)
            .orElseThrow(() -> new RuntimeException("Product not found"));

        ProductSaveRequest request = webMapper.toSaveRequest(domain);
        ProductDetailResponse audit = webMapper.toDetailResponse(domain);
        
        // ProductUIForm replacement logic if needed, but the original template uses productRequest
        model.addAttribute("productRequest", request);
        model.addAttribute("auditInfo", audit);
        populateSelectOptions(model);
        return "inventory/products/form";
    }

    @PostMapping("/edit/{id}")
    @PreAuthorize("hasAuthority('PRODUCT_UPDATE')")
    @ResponseBody
    public ResponseEntity<ApiResponse<ProductDetailResponse>> update(
            @PathVariable Long id, 
            @Valid @RequestBody ProductSaveRequest request) {
            
        com.solusi.erp.inventory.product.domain.model.Product domain = updateProductUseCase.execute(
            id, request.getName(), request.getBarcode(), request.getNote(),
            request.getCategoryId(), request.getUomId(), request.getBrandId(),
            request.getHscode(), request.getIsActive(), request.getIsSerialized(),
            request.getMinStock(), request.getMaxStock(),
            request.getWeightNet(), request.getWeightGross(), request.getWeightUomId(),
            request.getLength(), request.getWidth(), request.getHeight(), request.getDimensionUomId()
        );
        
        ProductDetailResponse data = webMapper.toDetailResponse(domain);
        String msg = messageSource.getMessage("msg.success.update", null, LocaleContextHolder.getLocale());
        return ResponseEntity.ok(ApiResponse.success(msg, data));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('PRODUCT_DELETE')")
    @ResponseBody
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        deleteProductUseCase.execute(id);
        String msg = messageSource.getMessage("msg.success.delete", null, LocaleContextHolder.getLocale());
        return HtmxResponseUtility.okWithRefreshTableAndSuccess(msg);
    }

    private void populateSelectOptions(Model model) {
        model.addAttribute("uoms", getUomLookupUseCase.findByType(UomType.UNIT));
        model.addAttribute("weightUoms", getUomLookupUseCase.findByType(UomType.WEIGHT));
        model.addAttribute("lengthUoms", getUomLookupUseCase.findByType(UomType.LENGTH));
    }
}
