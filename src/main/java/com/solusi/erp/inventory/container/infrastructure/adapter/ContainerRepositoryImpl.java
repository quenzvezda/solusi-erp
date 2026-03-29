package com.solusi.erp.inventory.container.infrastructure.adapter;

import com.solusi.erp.core.domain.model.Page;
import com.solusi.erp.core.domain.model.Pageable;
import com.solusi.erp.core.infrastructure.util.PageableMapper;
import com.solusi.erp.inventory.container.domain.model.Container;
import com.solusi.erp.inventory.container.domain.repository.ContainerRepository;
import com.solusi.erp.inventory.container.infrastructure.persistence.ContainerPersistenceMapper;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ContainerRepositoryImpl implements ContainerRepository {

    private final com.solusi.erp.inventory.repository.ContainerRepository jpaRepository;
    private final ContainerPersistenceMapper mapper;

    public ContainerRepositoryImpl(
            com.solusi.erp.inventory.repository.ContainerRepository jpaRepository,
            ContainerPersistenceMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Container save(Container domain) {
        com.solusi.erp.inventory.model.Container entity = mapper.toEntity(domain);
        com.solusi.erp.inventory.model.Container saved = jpaRepository.saveAndFlush(entity);
        return jpaRepository.findById(saved.getId()).map(mapper::toDomain)
            .orElseThrow(() -> new IllegalStateException("Failed to reload saved Container"));
    }

    @Override
    public Optional<Container> findById(Long id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Page<Container> findAll(String keyword, Long gridId, Pageable pageable) {
        org.springframework.data.domain.Pageable springPageable = PageableMapper.toSpring(pageable);
        org.springframework.data.domain.Page<com.solusi.erp.inventory.model.Container> springPage;
        if (keyword != null && !keyword.isBlank() && gridId != null) {
            springPage = jpaRepository.searchByGrid(keyword, gridId, springPageable);
        } else if (keyword != null && !keyword.isBlank()) {
            springPage = jpaRepository.search(keyword, springPageable);
        } else if (gridId != null) {
            springPage = jpaRepository.findByGridId(gridId, springPageable);
        } else {
            springPage = jpaRepository.findAll(springPageable);
        }
        return new Page<>(
            springPage.getContent().stream().map(mapper::toDomain).collect(Collectors.toList()),
            springPage.getNumber(),
            springPage.getSize(),
            springPage.getTotalElements()
        );
    }

    @Override
    public List<Container> search(String keyword, int limit) {
        return jpaRepository.search(keyword != null ? keyword : "", PageRequest.of(0, limit))
            .getContent().stream().map(mapper::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<Container> search(String keyword, Long gridId, Long facilityId, int limit) {
        String kw = keyword != null ? keyword : "";
        if (gridId != null) {
            return jpaRepository.searchByGrid(kw, gridId, PageRequest.of(0, limit))
                .getContent().stream().map(mapper::toDomain).collect(Collectors.toList());
        }
        if (facilityId != null) {
            return jpaRepository.searchByFacility(kw, facilityId, PageRequest.of(0, limit))
                .getContent().stream().map(mapper::toDomain).collect(Collectors.toList());
        }
        return jpaRepository.search(kw, PageRequest.of(0, limit))
            .getContent().stream().map(mapper::toDomain).collect(Collectors.toList());
    }

    @Override
    public void delete(Long id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public boolean existsByBarcode(String barcode) {
        return jpaRepository.existsByBarcode(barcode);
    }

    @Override
    public boolean existsByBarcodeAndIdNot(String barcode, Long id) {
        return jpaRepository.existsByBarcodeAndIdNot(barcode, id);
    }
}
