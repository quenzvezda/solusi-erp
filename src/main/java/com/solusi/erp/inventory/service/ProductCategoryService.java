package com.solusi.erp.inventory.service;

import com.solusi.erp.core.dto.FormViewDto;
import com.solusi.erp.core.dto.LookupDto;
import com.solusi.erp.inventory.dto.ProductCategoryRequest;
import com.solusi.erp.inventory.dto.ProductCategoryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Service interface for Product Category.
 */
public interface ProductCategoryService {
    Page<ProductCategoryResponse> findAll(String keyword, Pageable pageable);
    ProductCategoryResponse findById(Long id);
    ProductCategoryRequest getEditData(Long id);
    FormViewDto<ProductCategoryRequest, Void, ProductCategoryResponse> getFormView(Long id);
    ProductCategoryResponse create(ProductCategoryRequest request);
    ProductCategoryResponse update(Long id, ProductCategoryRequest request);
    void delete(Long id);
    List<LookupDto> lookupCategories(String keyword, int limit);
    LookupDto getLookupCategory(Long id);
}
