package com.solusi.erp.master.geographic.domain.repository;

import com.solusi.erp.core.domain.model.Page;
import com.solusi.erp.core.domain.model.Pageable;
import com.solusi.erp.master.geographic.domain.model.Geographic;

import java.util.List;
import java.util.Optional;

/**
 * Domain Repository interface for Geographic.
 * Pure Java — no framework dependency.
 */
public interface GeographicRepository {
    Geographic save(Geographic geographic);
    Optional<Geographic> findById(Long id);
    Page<Geographic> findAll(String keyword, Long parentId, Pageable pageable);
    void delete(Long id);
    boolean existsByCode(String code);
    List<Geographic> findCountries(String keyword, int limit);
    List<Geographic> findProvincesByCountry(Long countryId, String keyword, int limit);
    List<Geographic> findCitiesByProvince(Long provinceId, String keyword, int limit);
}
