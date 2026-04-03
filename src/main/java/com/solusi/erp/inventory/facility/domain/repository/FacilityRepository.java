package com.solusi.erp.inventory.facility.domain.repository;

import com.solusi.erp.core.domain.model.Page;
import com.solusi.erp.core.domain.model.Pageable;
import com.solusi.erp.inventory.facility.domain.model.Facility;

import java.util.List;
import java.util.Optional;

public interface FacilityRepository {
    Facility save(Facility facility);
    Optional<Facility> findById(Long id);
    Page<Facility> findAll(String keyword, Pageable pageable);
    List<Facility> search(String keyword, int limit);
    void delete(Long id);
    boolean existsByCode(String code);
    boolean existsByCodeAndIdNot(String code, Long id);
}
