package com.solusi.erp.inventory.uomconversion.infrastructure.adapter;

import com.solusi.erp.core.domain.model.Page;
import com.solusi.erp.core.domain.model.Pageable;
import com.solusi.erp.core.util.PageableMapper;
import com.solusi.erp.inventory.model.ProductUomConversion;
import com.solusi.erp.inventory.product.infrastructure.persistence.JpaProductRepository;
import com.solusi.erp.inventory.product.infrastructure.persistence.ProductEntity;
import com.solusi.erp.inventory.repository.ProductUomConversionRepository;
import com.solusi.erp.inventory.repository.UnitOfMeasureRepository;
import com.solusi.erp.inventory.uomconversion.domain.model.UomConversion;
import com.solusi.erp.inventory.uomconversion.domain.repository.UomConversionRepository;
import com.solusi.erp.inventory.uomconversion.infrastructure.persistence.UomConversionPersistenceMapper;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Adapter implementation of UomConversionRepository.
 * Bridges Domain and Infrastructure Persistence.
 */
public class UomConversionRepositoryImpl implements UomConversionRepository {

    private final ProductUomConversionRepository jpaRepo;
    private final JpaProductRepository productRepo;
    private final UnitOfMeasureRepository uomRepo;
    private final UomConversionPersistenceMapper mapper;

    public UomConversionRepositoryImpl(ProductUomConversionRepository jpaRepo,
                                        JpaProductRepository productRepo,
                                        UnitOfMeasureRepository uomRepo,
                                        UomConversionPersistenceMapper mapper) {
        this.jpaRepo = jpaRepo;
        this.productRepo = productRepo;
        this.uomRepo = uomRepo;
        this.mapper = mapper;
    }

    @Override
    public UomConversion save(UomConversion domain) {
        ProductUomConversion entity;
        if (domain.getMetadata().id() == null) {
            entity = new ProductUomConversion();
        } else {
            entity = jpaRepo.findById(domain.getMetadata().id())
                .orElseThrow(() -> new RuntimeException("UomConversion not found: " + domain.getMetadata().id()));
        }

        entity.setConversionFactor(domain.getConversionFactor());

        ProductEntity product = productRepo.findById(domain.getProductId())
            .orElseThrow(() -> new RuntimeException("Product not found: " + domain.getProductId()));
        entity.setProduct(product);

        com.solusi.erp.inventory.model.UnitOfMeasure fromUom = uomRepo.findById(domain.getFromUomId())
            .orElseThrow(() -> new RuntimeException("UOM not found: " + domain.getFromUomId()));
        entity.setFromUom(fromUom);

        com.solusi.erp.inventory.model.UnitOfMeasure toUom = uomRepo.findById(domain.getToUomId())
            .orElseThrow(() -> new RuntimeException("UOM not found: " + domain.getToUomId()));
        entity.setToUom(toUom);

        return mapper.toDomain(jpaRepo.save(entity));
    }

    @Override
    public Optional<UomConversion> findById(Long id) {
        return jpaRepo.findById(id).map(mapper::toDomain);
    }

    @Override
    public Page<UomConversion> findAll(String keyword, Pageable pageable) {
        org.springframework.data.domain.Pageable springPageable =
            PageableMapper.toSpring(pageable);
        org.springframework.data.domain.Page<ProductUomConversion> springPage =
            jpaRepo.search(keyword, springPageable);
        return new Page<>(
            springPage.getContent().stream().map(mapper::toDomain).collect(Collectors.toList()),
            springPage.getNumber(),
            springPage.getSize(),
            springPage.getTotalElements()
        );
    }

    @Override
    public void deleteById(Long id) {
        jpaRepo.deleteById(id);
    }

    @Override
    public boolean existsById(Long id) {
        return jpaRepo.existsById(id);
    }

    @Override
    public boolean existsByProductIdAndFromUomId(Long productId, Long fromUomId) {
        return jpaRepo.existsByProductIdAndFromUomId(productId, fromUomId);
    }

    @Override
    public boolean existsByProductIdAndFromUomIdAndIdNot(Long productId, Long fromUomId, Long id) {
        return jpaRepo.existsByProductIdAndFromUomIdAndIdNot(productId, fromUomId, id);
    }

    @Override
    public List<UomConversion> findByProductId(Long productId) {
        return jpaRepo.findByProductId(productId).stream()
            .map(mapper::toDomain)
            .collect(Collectors.toList());
    }
}
