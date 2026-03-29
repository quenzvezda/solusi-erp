package com.solusi.erp.security.role.infrastructure.adapter;

import com.solusi.erp.security.permission.infrastructure.persistence.Permission;
import com.solusi.erp.security.permission.infrastructure.persistence.PermissionJpaRepository;
import com.solusi.erp.security.role.domain.model.Role;
import com.solusi.erp.security.role.domain.repository.RoleRepository;
import com.solusi.erp.security.role.infrastructure.persistence.RoleJpaRepository;
import com.solusi.erp.security.role.infrastructure.persistence.RolePersistenceMapper;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class RoleRepositoryAdapter implements RoleRepository {

    private final RoleJpaRepository roleJpaRepository;
    private final PermissionJpaRepository permissionRepository;
    private final RolePersistenceMapper mapper;

    public RoleRepositoryAdapter(
            RoleJpaRepository roleJpaRepository,
            PermissionJpaRepository permissionRepository,
            RolePersistenceMapper mapper) {
        this.roleJpaRepository = roleJpaRepository;
        this.permissionRepository = permissionRepository;
        this.mapper = mapper;
    }

    @Override
    public Role save(Role role) {
        com.solusi.erp.security.role.infrastructure.persistence.Role entity = mapper.toEntity(role);
        if (role.getPermissionIds() == null || role.getPermissionIds().isEmpty()) {
            entity.setPermissions(new HashSet<>());
        } else {
            List<Permission> permissions = permissionRepository.findAllById(role.getPermissionIds());
            entity.setPermissions(new HashSet<>(permissions));
        }
        com.solusi.erp.security.role.infrastructure.persistence.Role saved = roleJpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<Role> findById(Long id) {
        return roleJpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<Role> findByName(String name) {
        return roleJpaRepository.findByName(name).map(mapper::toDomain);
    }

    @Override
    public List<Role> findAll() {
        return roleJpaRepository.findAll().stream().map(mapper::toDomain).collect(Collectors.toList());
    }

    @Override
    public void delete(Role role) {
        roleJpaRepository.deleteById(role.getId());
    }

    @Override
    public boolean existsByName(String name) {
        return roleJpaRepository.findByName(name).isPresent();
    }
}

