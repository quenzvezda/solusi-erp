package com.solusi.erp.purchasing.supplierpricelist.infrastructure.adapter;

import com.solusi.erp.core.domain.model.Page;
import com.solusi.erp.core.domain.model.Pageable;
import com.solusi.erp.core.infrastructure.util.PageableMapper;
import com.solusi.erp.purchasing.supplierpricelist.domain.model.SupplierPriceList;
import com.solusi.erp.purchasing.supplierpricelist.domain.repository.SupplierPriceListRepository;
import com.solusi.erp.purchasing.supplierpricelist.infrastructure.persistence.SupplierPriceListEntity;
import com.solusi.erp.purchasing.supplierpricelist.infrastructure.persistence.SupplierPriceListJpaRepository;
import com.solusi.erp.purchasing.supplierpricelist.infrastructure.persistence.SupplierPriceListPersistenceMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class SupplierPriceListRepositoryImpl implements SupplierPriceListRepository {

    private final SupplierPriceListJpaRepository jpaRepository;
    private final SupplierPriceListPersistenceMapper mapper;
    private final EntityManager entityManager;

    public SupplierPriceListRepositoryImpl(SupplierPriceListJpaRepository jpaRepository,
                                            SupplierPriceListPersistenceMapper mapper,
                                            EntityManager entityManager) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
        this.entityManager = entityManager;
    }

    @Override
    public SupplierPriceList save(SupplierPriceList domain) {
        SupplierPriceListEntity entity = mapper.toEntity(domain);
        SupplierPriceListEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<SupplierPriceList> findById(Long id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Page<SupplierPriceList> findAll(String keyword, Pageable pageable) {
        String normalizedKeyword = keyword != null ? keyword.trim() : null;
        org.springframework.data.domain.Pageable springPageable = PageableMapper.toSpring(pageable);
        org.springframework.data.domain.Page<SupplierPriceListEntity> springPage;
        
        if (normalizedKeyword != null && !normalizedKeyword.isBlank()) {
            springPage = jpaRepository.search(normalizedKeyword, springPageable);
        } else {
            // Use custom query with proper sorting for joined attributes
            springPage = findAllWithSort(pageable);
        }
        
        return new Page<>(
            springPage.getContent().stream().map(mapper::toDomain).collect(Collectors.toList()),
            springPage.getNumber(),
            springPage.getSize(),
            springPage.getTotalElements()
        );
    }
    
    private org.springframework.data.domain.Page<SupplierPriceListEntity> findAllWithSort(Pageable pageable) {
        // Build ORDER BY clause based on sort field
        String orderBy = "";
        if (pageable.isSorted()) {
            String sortDir = "desc".equalsIgnoreCase(pageable.sortDir()) ? "DESC" : "ASC";
            if ("supplierName".equals(pageable.sortField())) {
                orderBy = " ORDER BY p.name " + sortDir;
            } else if ("productName".equals(pageable.sortField())) {
                orderBy = " ORDER BY prod.name " + sortDir;
            } else if ("code".equals(pageable.sortField())) {
                orderBy = " ORDER BY s.code " + sortDir;
            }
        }
        
        // Build JPQL query
        String baseQuery = "SELECT s FROM SupplierPriceListEntity s " +
                           "LEFT JOIN Party p ON p.id = s.supplierId " +
                           "LEFT JOIN ProductEntity prod ON prod.id = s.productId" + orderBy;
        
        // Get total count
        Long total = (Long) entityManager.createQuery(
            "SELECT COUNT(s) FROM SupplierPriceListEntity s")
            .getSingleResult();
        
        // Get paginated content
        TypedQuery<SupplierPriceListEntity> query = entityManager.createQuery(baseQuery, SupplierPriceListEntity.class);
        query.setFirstResult(pageable.page() * pageable.size());
        query.setMaxResults(pageable.size());
        List<SupplierPriceListEntity> content = query.getResultList();
        
        return new org.springframework.data.domain.PageImpl<>(
            content,
            org.springframework.data.domain.PageRequest.of(pageable.page(), pageable.size()),
            total
        );
    }

    @Override
    public List<SupplierPriceList> search(String keyword, int limit) {
        String normalizedKeyword = keyword != null ? keyword.trim() : "";
        return jpaRepository.search(
                normalizedKeyword,
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
    public boolean existsOverlapping(Long supplierId, Long productId, Long uomId, Long currencyId,
                                      LocalDate effectiveFrom, LocalDate effectiveTo, Long excludeId) {
        return jpaRepository.existsOverlapping(supplierId, productId, uomId, currencyId,
            effectiveFrom, effectiveTo, excludeId);
    }
}
