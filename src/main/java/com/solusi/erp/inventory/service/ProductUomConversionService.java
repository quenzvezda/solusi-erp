package com.solusi.erp.inventory.service;

import com.solusi.erp.core.dto.FormViewDto;
import com.solusi.erp.inventory.dto.ProductUomConversionRequest;
import com.solusi.erp.inventory.dto.ProductUomConversionResponse;
import com.solusi.erp.inventory.form.ProductUomUIForm;
import com.solusi.erp.inventory.dto.UomConversionLookupDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface ProductUomConversionService {
    Page<ProductUomConversionResponse> findAll(String keyword, Pageable pageable);
    ProductUomConversionResponse findById(Long id);
    ProductUomConversionRequest getEditData(Long id);
    FormViewDto<ProductUomConversionRequest, ProductUomUIForm, ProductUomConversionResponse> getFormView(Long id);
    ProductUomConversionResponse create(ProductUomConversionRequest request);
    ProductUomConversionResponse update(Long id, ProductUomConversionRequest request);
    void delete(Long id);
    List<UomConversionLookupDto> getConversions(Long productId);
}
