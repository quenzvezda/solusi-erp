package com.solusi.erp.security.service.impl;

import com.solusi.erp.security.dto.PermissionResponse;
import com.solusi.erp.security.dto.RoleRequest;
import com.solusi.erp.security.dto.RoleResponse;
import com.solusi.erp.security.mapper.RoleMapper;
import com.solusi.erp.security.model.Permission;
import com.solusi.erp.security.model.Role;
import com.solusi.erp.security.repository.PermissionRepository;
import com.solusi.erp.security.repository.RoleRepository;
import com.solusi.erp.security.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final RoleMapper roleMapper;
    private final MessageSource messageSource;

    @Override
    @Transactional(readOnly = true)
    public List<RoleResponse> findAll() {
        return roleMapper.toResponseList(roleRepository.findAll());
    }

    @Override
    @Transactional(readOnly = true)
    public RoleResponse findById(Long id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(messageSource.getMessage("msg.error.role.notfound", null, LocaleContextHolder.getLocale())));
        return roleMapper.toResponse(role);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PermissionResponse> findAllPermissions() {
        return roleMapper.toPermissionResponseList(permissionRepository.findAll());
    }

    @Override
    @Transactional
    public void create(RoleRequest request) {
        if (roleRepository.findByName(request.getName()).isPresent()) {
            throw new RuntimeException(messageSource.getMessage("msg.error.role.duplicate", null, LocaleContextHolder.getLocale()));
        }

        Role role = roleMapper.toEntity(request);
        setPermissions(role, request.getPermissionIds());
        roleRepository.save(role);
    }

    @Override
    @Transactional
    public void update(Long id, RoleRequest request) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(messageSource.getMessage("msg.error.role.notfound", null, LocaleContextHolder.getLocale())));

        roleRepository.findByName(request.getName())
                .ifPresent(existing -> {
                    if (!existing.getId().equals(id)) {
                        throw new RuntimeException(messageSource.getMessage("msg.error.role.duplicate", null, LocaleContextHolder.getLocale()));
                    }
                });

        roleMapper.updateEntity(request, role);
        setPermissions(role, request.getPermissionIds());
        roleRepository.save(role);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(messageSource.getMessage("msg.error.role.notfound", null, LocaleContextHolder.getLocale())));
        
        // Prevent deleting essential roles if necessary
        if ("ROLE_ADMIN".equals(role.getName())) {
            throw new RuntimeException(messageSource.getMessage("msg.error.role.admin.nodelete", null, LocaleContextHolder.getLocale()));
        }
        
        roleRepository.delete(role);
    }

    private void setPermissions(Role role, Set<Long> permissionIds) {
        if (permissionIds == null || permissionIds.isEmpty()) {
            role.setPermissions(new HashSet<>());
            return;
        }
        List<Permission> permissions = permissionRepository.findAllById(permissionIds);
        role.setPermissions(new HashSet<>(permissions));
    }
}
