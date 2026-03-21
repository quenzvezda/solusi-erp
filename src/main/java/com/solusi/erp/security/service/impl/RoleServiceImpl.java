package com.solusi.erp.security.service.impl;

import com.solusi.erp.core.dto.FormViewDto;
import com.solusi.erp.core.dto.LookupDto;
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
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
                .orElseThrow(() -> new RuntimeException(getMessage("msg.error.role.notfound")));
        return roleMapper.toResponse(role);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PermissionResponse> findAllPermissions() {
        return roleMapper.toPermissionResponseList(permissionRepository.findAll());
    }

    @Override
    @Transactional(readOnly = true)
    public FormViewDto<RoleRequest, Void, RoleResponse> getRoleEditView(Long id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(getMessage("msg.error.role.notfound")));
        
        RoleRequest request = RoleRequest.builder()
                .name(role.getName())
                .description(role.getDescription())
                .permissionIds(role.getPermissions().stream()
                        .map(Permission::getId)
                        .collect(Collectors.toSet()))
                .build();
        request.setId(role.getId());
        request.setVersion(role.getVersion());

        return FormViewDto.<RoleRequest, Void, RoleResponse>builder()
                .request(request)
                .ui(null)
                .audit(roleMapper.toResponse(role))
                .build();
    }

    @Override
    @Transactional
    public RoleResponse create(RoleRequest request) {
        if (roleRepository.findByName(request.getName()).isPresent()) {
            throw new RuntimeException(getMessage("msg.error.role.duplicate"));
        }

        Role role = roleMapper.toEntity(request);
        setPermissions(role, request.getPermissionIds());
        return roleMapper.toResponse(roleRepository.save(role));
    }

    @Override
    @Transactional
    public RoleResponse update(Long id, RoleRequest request) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(getMessage("msg.error.role.notfound")));

        roleRepository.findByName(request.getName())
                .ifPresent(existing -> {
                    if (!existing.getId().equals(id)) {
                        throw new RuntimeException(getMessage("msg.error.role.duplicate"));
                    }
                });

        roleMapper.updateEntity(request, role);
        setPermissions(role, request.getPermissionIds());
        return roleMapper.toResponse(roleRepository.save(role));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(getMessage("msg.error.role.notfound")));
        
        // Prevent deleting essential roles if necessary
        if ("ROLE_ADMIN".equals(role.getName())) {
            throw new RuntimeException(getMessage("msg.error.role.admin.nodelete"));
        }
        
        roleRepository.delete(role);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LookupDto> lookupRoles(String keyword, int limit) {
        // We need a search method in RoleRepository or just use findAll if small
        // Let's check RoleRepository
        return roleRepository.findAll(PageRequest.of(0, limit)).getContent().stream()
                .filter(r -> keyword == null || r.getName().toLowerCase().contains(keyword.toLowerCase()))
                .map(r -> new LookupDto(r.getId(), r.getName(), r.getDescription()))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public LookupDto getLookupRole(Long id) {
        Role r = roleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(getMessage("msg.error.role.notfound")));
        return new LookupDto(r.getId(), r.getName(), r.getDescription());
    }

    private String getMessage(String key) {
        return messageSource.getMessage(key, null, LocaleContextHolder.getLocale());
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
