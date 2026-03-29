package com.solusi.erp.security.permission.infrastructure.adapter;

import com.solusi.erp.security.permission.domain.model.Permission;
import com.solusi.erp.security.permission.domain.repository.PermissionRepository;
import com.solusi.erp.security.permission.infrastructure.persistence.PermissionJpaRepository;
import com.solusi.erp.security.permission.infrastructure.persistence.PermissionPersistenceMapper;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class PermissionRepositoryAdapter implements PermissionRepository {

    private final PermissionJpaRepository permissionJpaRepository;
    private final com.solusi.erp.security.permissiongroup.infrastructure.persistence.PermissionGroupJpaRepository permissionGroupRepository;
    private final PermissionPersistenceMapper mapper;

    public PermissionRepositoryAdapter(
            PermissionJpaRepository permissionJpaRepository,
            com.solusi.erp.security.permissiongroup.infrastructure.persistence.PermissionGroupJpaRepository permissionGroupRepository,
            PermissionPersistenceMapper mapper) {
        this.permissionJpaRepository = permissionJpaRepository;
        this.permissionGroupRepository = permissionGroupRepository;
        this.mapper = mapper;
    }

    @Override
    public Permission save(Permission permission) {
        com.solusi.erp.security.permission.infrastructure.persistence.Permission entity = mapper.toEntity(permission);
        if (permission.getPermissionGroupId() != null) {
            com.solusi.erp.security.permissiongroup.infrastructure.persistence.PermissionGroup group = permissionGroupRepository.findById(permission.getPermissionGroupId())
                    .orElseThrow(() -> new RuntimeException("Permission Group not found"));
            entity.setPermissionGroup(group);
        } else {
            entity.setPermissionGroup(null);
        }
        com.solusi.erp.security.permission.infrastructure.persistence.Permission saved = permissionJpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<Permission> findById(Long id) {
        return permissionJpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<Permission> findByName(String name) {
        return permissionJpaRepository.findByName(name).map(mapper::toDomain);
    }

    @Override
    public List<Permission> findAll() {
        return permissionJpaRepository.findAll().stream().map(mapper::toDomain).collect(Collectors.toList());
    }

    @Override
    public void delete(Permission permission) {
        permissionJpaRepository.deleteById(permission.getId());
    }

    @Override
    public boolean existsByName(String name) {
        return permissionJpaRepository.findByName(name).isPresent();
    }
}
