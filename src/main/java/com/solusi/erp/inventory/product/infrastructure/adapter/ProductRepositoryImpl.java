package com.solusi.erp.inventory.product.infrastructure.adapter;

import com.solusi.erp.core.domain.model.Page;
import com.solusi.erp.core.domain.model.Pageable;
import com.solusi.erp.inventory.product.domain.model.Product;
import com.solusi.erp.inventory.product.domain.repository.ProductRepository;
import com.solusi.erp.inventory.product.infrastructure.persistence.ProductEntity;
import com.solusi.erp.inventory.product.infrastructure.persistence.JpaProductRepository;
import com.solusi.erp.inventory.product.infrastructure.persistence.ProductPersistenceMapper;
import org.springframework.data.domain.PageRequest;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Adapter implementation of ProductRepository.
 * Bridges Domain and Infrastructure Persistence.
 */
public class ProductRepositoryImpl implements ProductRepository {

    private final JpaProductRepository jpaRepository;
    private final ProductPersistenceMapper mapper;

    public ProductRepositoryImpl(JpaProductRepository jpaRepository, ProductPersistenceMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Product save(Product product) {
        ProductEntity entity = mapper.toEntity(product);
        ProductEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<Product> findById(Long id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<Product> findByCode(String code) {
        return jpaRepository.findByCode(code).map(mapper::toDomain);
    }

    @Override
    public Page<Product> findAll(String keyword, Pageable pageable) {
        org.springframework.data.domain.Pageable springPageable = PageRequest.of(pageable.page(), pageable.size());
        org.springframework.data.domain.Page<ProductEntity> springPage = jpaRepository.findAllByKeyword(keyword, springPageable);
        
        return new Page<>(
            springPage.getContent().stream().map(mapper::toDomain).collect(Collectors.toList()),
            springPage.getNumber(),
            springPage.getSize(),
            springPage.getTotalElements()
        );
    }

    @Override
    public void delete(Long id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public boolean existsByCode(String code) {
        return jpaRepository.existsByCode(code);
    }
}
