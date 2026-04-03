package com.solusi.erp.security.permissiongroup.infrastructure.adapter;

import com.solusi.erp.core.domain.model.Page;
import com.solusi.erp.core.domain.model.Pageable;
import com.solusi.erp.core.infrastructure.util.PageableMapper;
import com.solusi.erp.security.permissiongroup.domain.model.PermissionGroup;
import com.solusi.erp.security.permissiongroup.domain.repository.PermissionGroupRepository;
import com.solusi.erp.security.permissiongroup.infrastructure.persistence.PermissionGroupJpaRepository;
import com.solusi.erp.security.permissiongroup.infrastructure.persistence.PermissionGroupPersistenceMapper;

import java.util.Optional;
import java.util.stream.Collectors;

public class PermissionGroupRepositoryAdapter implements PermissionGroupRepository {

    private final PermissionGroupJpaRepository jpaRepository;
    private final PermissionGroupPersistenceMapper mapper;

    public PermissionGroupRepositoryAdapter(PermissionGroupJpaRepository jpaRepository,
                                             PermissionGroupPersistenceMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public PermissionGroup save(PermissionGroup domain) {
        com.solusi.erp.security.permissiongroup.infrastructure.persistence.PermissionGroup entity = mapper.toEntity(domain);
        com.solusi.erp.security.permissiongroup.infrastructure.persistence.PermissionGroup saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<PermissionGroup> findById(Long id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Page<PermissionGroup> findAll(String keyword, Pageable pageable) {
        org.springframework.data.domain.Pageable springPageable = PageableMapper.toSpring(pageable);
        org.springframework.data.domain.Page<com.solusi.erp.security.permissiongroup.infrastructure.persistence.PermissionGroup> springPage =
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
    public void delete(Long id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public boolean existsByCode(String code) {
        return jpaRepository.findByCode(code).isPresent();
    }
}
