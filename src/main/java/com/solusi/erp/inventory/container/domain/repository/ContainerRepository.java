package com.solusi.erp.inventory.container.domain.repository;

import com.solusi.erp.core.domain.model.Page;
import com.solusi.erp.core.domain.model.Pageable;
import com.solusi.erp.inventory.container.domain.model.Container;

import java.util.List;
import java.util.Optional;

public interface ContainerRepository {
    Container save(Container container);
    Optional<Container> findById(Long id);
    Page<Container> findAll(String keyword, Long gridId, Pageable pageable);
    List<Container> search(String keyword, int limit);
    List<Container> search(String keyword, Long gridId, Long facilityId, int limit);
    void delete(Long id);
    boolean existsByBarcode(String barcode);
    boolean existsByBarcodeAndIdNot(String barcode, Long id);
}
