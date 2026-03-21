package com.solusi.erp.inventory.service;

import com.solusi.erp.core.dto.FormViewDto;
import com.solusi.erp.core.dto.LookupDto;
import com.solusi.erp.inventory.dto.ProductRequest;
import com.solusi.erp.inventory.dto.ProductResponse;
import com.solusi.erp.inventory.form.ProductUIForm;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Service interface for Product.
 */
public interface ProductService {
    LookupDto getLookupProduct(Long id);
    List<LookupDto> lookupProducts(String keyword, int limit);
    List<ProductResponse> findAll();
    Page<ProductResponse> findAll(String keyword, Pageable pageable);
    ProductResponse findById(Long id);
    com.solusi.erp.inventory.model.Product getEntityById(Long id);
    ProductRequest getEditData(Long id);
    FormViewDto<ProductRequest, ProductUIForm, ProductResponse> getProductEditView(Long id);
    ProductResponse create(ProductRequest request);
    ProductResponse update(Long id, ProductRequest request);
    void delete(Long id);
}
