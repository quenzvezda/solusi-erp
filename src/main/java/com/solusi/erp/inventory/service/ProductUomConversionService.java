package com.solusi.erp.inventory.service;

import com.solusi.erp.inventory.dto.ProductUomConversionRequest;
import com.solusi.erp.inventory.dto.ProductUomConversionResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductUomConversionService {
    Page<ProductUomConversionResponse> findAll(String keyword, Pageable pageable);
    ProductUomConversionResponse findById(Long id);
    ProductUomConversionRequest getEditData(Long id);
    ProductUomConversionResponse create(ProductUomConversionRequest request);
    ProductUomConversionResponse update(Long id, ProductUomConversionRequest request);
    void delete(Long id);
}
