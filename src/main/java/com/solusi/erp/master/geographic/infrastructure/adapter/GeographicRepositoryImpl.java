package com.solusi.erp.master.geographic.infrastructure.adapter;

import com.solusi.erp.core.domain.model.Page;
import com.solusi.erp.core.domain.model.Pageable;
import com.solusi.erp.core.infrastructure.util.PageableMapper;
import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.master.geographic.domain.model.Geographic;
import com.solusi.erp.master.geographic.domain.repository.GeographicRepository;
import com.solusi.erp.master.geographic.infrastructure.persistence.GeographicPersistenceMapper;
import com.solusi.erp.master.shared.model.GeographicType;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Adapter implementation of the domain GeographicRepository.
 * Bridges Domain and Infrastructure Persistence.
 */
public class GeographicRepositoryImpl implements GeographicRepository {

    private final com.solusi.erp.master.geographic.infrastructure.persistence.GeographicJpaRepository jpaRepository;
    private final GeographicPersistenceMapper mapper;

    public GeographicRepositoryImpl(
            com.solusi.erp.master.geographic.infrastructure.persistence.GeographicJpaRepository jpaRepository,
            GeographicPersistenceMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Geographic save(Geographic domain) {
        com.solusi.erp.master.geographic.infrastructure.persistence.Geographic entity = mapper.toEntity(domain);
        if (domain.getParentId() != null) {
            com.solusi.erp.master.geographic.infrastructure.persistence.Geographic parent = jpaRepository.findById(domain.getParentId())
                    .orElseThrow(() -> new DomainException("msg.error.geographic.parent.notfound"));
            entity.setParent(parent);
        }
        com.solusi.erp.master.geographic.infrastructure.persistence.Geographic saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<Geographic> findById(Long id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Page<Geographic> findAll(String keyword, Long parentId, Pageable pageable) {
        org.springframework.data.domain.Pageable sp = PageableMapper.toSpring(pageable);
        org.springframework.data.domain.Page<com.solusi.erp.master.geographic.infrastructure.persistence.Geographic> springPage;

        if (parentId != null) {
            springPage = jpaRepository.findByParentId(parentId, sp);
        } else if (keyword != null && !keyword.isBlank()) {
            springPage = jpaRepository.search(keyword, sp);
        } else {
            springPage = jpaRepository.findAll(sp);
        }

        return new Page<>(
                springPage.getContent().stream().map(mapper::toDomain).collect(Collectors.toList()),
                springPage.getNumber(),
                springPage.getSize(),
                springPage.getTotalElements()
        );
    }

    @Override
    public void delete(Long id) {
        com.solusi.erp.master.geographic.infrastructure.persistence.Geographic entity = jpaRepository.findById(id)
                .orElseThrow(() -> new DomainException("msg.error.geographic.notfound"));
        entity.setIsActive(false);
        jpaRepository.save(entity);
    }

    @Override
    public boolean existsByCode(String code) {
        return jpaRepository.existsByCode(code);
    }

    @Override
    public List<Geographic> findCountries(String keyword, int limit) {
        String q = keyword != null ? keyword : "";
        int safeLimit = Math.max(1, limit);
        return jpaRepository.lookupByType(GeographicType.COUNTRY, q, PageRequest.of(0, safeLimit))
                .getContent().stream().map(mapper::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<Geographic> findProvincesByCountry(Long countryId, String keyword, int limit) {
        String q = keyword != null ? keyword : "";
        int safeLimit = Math.max(1, limit);
        if (countryId == null) {
            return jpaRepository.lookupProvincesByType(GeographicType.STATE_PROVINCE, q, PageRequest.of(0, safeLimit))
                    .getContent().stream().map(mapper::toDomain).collect(Collectors.toList());
        }
        return jpaRepository.lookupProvincesByParent(countryId, q, PageRequest.of(0, safeLimit))
                .getContent().stream().map(mapper::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<Geographic> findCitiesByProvince(Long provinceId, String keyword, int limit) {
        String q = keyword != null ? keyword : "";
        int safeLimit = Math.max(1, limit);
        if (provinceId == null) {
            return jpaRepository.lookupCitiesByType(GeographicType.CITY_MUNICIPALITY, q, PageRequest.of(0, safeLimit))
                    .getContent().stream().map(mapper::toDomain).collect(Collectors.toList());
        }
        return jpaRepository.lookupCitiesByParent(provinceId, q, PageRequest.of(0, safeLimit))
                .getContent().stream().map(mapper::toDomain).collect(Collectors.toList());
    }
}

