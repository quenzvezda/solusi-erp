package com.solusi.erp.inventory.service;

import com.solusi.erp.inventory.dto.InventoryLookupDto;
import com.solusi.erp.inventory.dto.ContainerRequest;
import com.solusi.erp.inventory.dto.ContainerResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Service for Container.
 */
public interface ContainerService {
    InventoryLookupDto getLookupContainer(Long id);
    List<InventoryLookupDto> lookupContainers(String keyword, Long facilityId, Long gridId, int limit);
    List<ContainerResponse> findAll();
    Page<ContainerResponse> findAll(String keyword, Long gridId, Pageable pageable);
    ContainerResponse findById(Long id);
    ContainerRequest getEditData(Long id);
    void create(ContainerRequest request);
    void update(Long id, ContainerRequest request);
    void delete(Long id);
}
