package com.solusi.erp.inventory.productcategory.infrastructure.adapter;

import com.solusi.erp.core.domain.model.Page;
import com.solusi.erp.core.domain.model.Pageable;
import com.solusi.erp.core.util.PageableMapper;
import com.solusi.erp.inventory.productcategory.domain.model.ProductCategory;
import com.solusi.erp.inventory.productcategory.domain.repository.ProductCategoryRepository;
import com.solusi.erp.inventory.productcategory.infrastructure.persistence.ProductCategoryPersistenceMapper;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ProductCategoryRepositoryImpl implements ProductCategoryRepository {

    private final com.solusi.erp.inventory.repository.ProductCategoryRepository jpaRepository;
    private final ProductCategoryPersistenceMapper mapper;

    public ProductCategoryRepositoryImpl(
            com.solusi.erp.inventory.repository.ProductCategoryRepository jpaRepository,
            ProductCategoryPersistenceMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public ProductCategory save(ProductCategory domain) {
        com.solusi.erp.inventory.model.ProductCategory entity = mapper.toEntity(domain);
        com.solusi.erp.inventory.model.ProductCategory saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<ProductCategory> findById(Long id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Page<ProductCategory> findAll(String keyword, Pageable pageable) {
        org.springframework.data.domain.Pageable springPageable = PageableMapper.toSpring(pageable);
        org.springframework.data.domain.Page<com.solusi.erp.inventory.model.ProductCategory> springPage =
            (keyword != null && !keyword.isBlank())
                ? jpaRepository.search(keyword, springPageable)
                : jpaRepository.findAll(springPageable);
        return new Page<>(
            springPage.getContent().stream().map(mapper::toDomain).collect(Collectors.toList()),
            springPage.getNumber(),
            springPage.getSize(),
            springPage.getTotalElements()
        );
    }

    @Override
    public List<ProductCategory> search(String keyword, int limit) {
        return jpaRepository.search(
                keyword != null ? keyword : "",
                PageRequest.of(0, limit)
            ).getContent().stream()
            .map(mapper::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    public void delete(Long id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public boolean existsByCode(String code) {
        return jpaRepository.existsByCode(code);
    }

    @Override
    public boolean existsByCodeAndIdNot(String code, Long id) {
        return jpaRepository.existsByCodeAndIdNot(code, id);
    }
}
