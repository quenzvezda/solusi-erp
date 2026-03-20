package com.solusi.erp.inventory.service;

import com.solusi.erp.core.dto.LookupDto;
import com.solusi.erp.inventory.dto.BrandRequest;
import com.solusi.erp.inventory.dto.BrandResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Service interface for Brand.
 */
public interface BrandService {
    Page<BrandResponse> findAll(String keyword, Pageable pageable);
    BrandResponse findById(Long id);
    BrandRequest getEditData(Long id);
    void create(BrandRequest request);
    void update(Long id, BrandRequest request);
    void delete(Long id);
    List<LookupDto> lookupBrands(String keyword, int limit);
    LookupDto getLookupBrand(Long id);
}
