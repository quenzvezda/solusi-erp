package com.solusi.erp.security.service.impl;

import com.solusi.erp.security.dto.PermissionRequest;
import com.solusi.erp.security.dto.PermissionResponse;
import com.solusi.erp.security.mapper.PermissionMapper;
import com.solusi.erp.security.model.Permission;
import com.solusi.erp.security.repository.PermissionRepository;
import com.solusi.erp.security.service.PermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PermissionServiceImpl implements PermissionService {

    private final PermissionRepository permissionRepository;
    private final PermissionMapper permissionMapper;

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
            throw new RuntimeException("Permission " + name + " sudah ada");
        }
        Permission permission = permissionMapper.toEntity(request);
        permission.setName(name);
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

        List<Permission> toSave = new ArrayList<>();
        for (String action : actions) {
            String fullName = moduleBase + "_" + action.toUpperCase();
            if (permissionRepository.findByName(fullName).isEmpty()) {
                Permission p = new Permission();
                p.setName(fullName);
                p.setDescription("Akses " + action + " untuk modul " + moduleBase);
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
                .orElseThrow(() -> new RuntimeException("Permission tidak ditemukan"));
        
        // Prevent deleting core permissions if needed
        if (permission.getName().startsWith("USERS_") || permission.getName().startsWith("ROLES_") || permission.getName().startsWith("DASHBOARD_")) {
            throw new RuntimeException("Permission sistem tidak boleh dihapus");
        }
        
        permissionRepository.delete(permission);
    }
}
