package com.solusi.erp.security.service.impl;

import com.solusi.erp.security.dto.PermissionRequest;
import com.solusi.erp.security.dto.PermissionResponse;
import com.solusi.erp.security.mapper.PermissionMapper;
import com.solusi.erp.security.model.Permission;
import com.solusi.erp.security.model.PermissionGroup;
import com.solusi.erp.security.repository.PermissionGroupRepository;
import com.solusi.erp.security.repository.PermissionRepository;
import com.solusi.erp.security.service.PermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PermissionServiceImpl implements PermissionService {

    private final PermissionRepository permissionRepository;
    private final PermissionGroupRepository permissionGroupRepository;
    private final PermissionMapper permissionMapper;
    private final MessageSource messageSource;

    @Override
    @Transactional(readOnly = true)
    public List<PermissionResponse> findAll() {
        return permissionMapper.toResponseList(permissionRepository.findAll());
    }

    @Override
    @Transactional
    public void create(PermissionRequest request) {
        String name = request.getName().toUpperCase().replace(" ", "-");
        if (permissionRepository.findByName(name).isPresent()) {
            throw new RuntimeException(messageSource.getMessage("msg.error.permission.exists", new Object[]{name}, LocaleContextHolder.getLocale()));
        }
        Permission permission = permissionMapper.toEntity(request);
        permission.setName(name);

        if (request.getPermissionGroupId() != null) {
            PermissionGroup group = permissionGroupRepository.findById(request.getPermissionGroupId())
                    .orElseThrow(() -> new RuntimeException("Permission Group not found"));
            permission.setPermissionGroup(group);
        }

        permissionRepository.save(permission);
    }

    @Override
    @Transactional
    public void createBatch(PermissionRequest request) {
        String moduleBase = request.getName().toUpperCase().replace(" ", "-");
        List<String> actions = request.getBatchActions();
        
        if (actions == null || actions.isEmpty()) {
            actions = List.of("READ", "CREATE", "UPDATE", "DELETE");
        }

        PermissionGroup group = null;
        if (request.getPermissionGroupId() != null) {
            group = permissionGroupRepository.findById(request.getPermissionGroupId())
                    .orElseThrow(() -> new RuntimeException("Permission Group not found"));
        }

        List<Permission> toSave = new ArrayList<>();
        for (String action : actions) {
            String fullName = moduleBase + "_" + action.toUpperCase();
            if (permissionRepository.findByName(fullName).isEmpty()) {
                Permission p = new Permission();
                p.setName(fullName);
                p.setDescription(messageSource.getMessage("msg.permission.description", new Object[]{action, moduleBase}, LocaleContextHolder.getLocale()));
                p.setPermissionGroup(group);
                toSave.add(p);
            }
        }
        
        if (!toSave.isEmpty()) {
            permissionRepository.saveAll(toSave);
        }
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Permission permission = permissionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(messageSource.getMessage("msg.error.permission.notfound", null, LocaleContextHolder.getLocale())));
        
        // Prevent deleting core permissions if needed
        if (permission.getName().startsWith("USERS_") || permission.getName().startsWith("ROLES_") || permission.getName().startsWith("DASHBOARD_") || permission.getName().startsWith("PERMISSIONS_")) {
            throw new RuntimeException(messageSource.getMessage("msg.error.permission.system.nodelete", null, LocaleContextHolder.getLocale()));
        }
        
        permissionRepository.delete(permission);
    }
}
