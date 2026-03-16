package com.solusi.erp.security.service.impl;

import com.solusi.erp.security.dto.PermissionGroupRequest;
import com.solusi.erp.security.dto.PermissionGroupResponse;
import com.solusi.erp.security.mapper.PermissionGroupMapper;
import com.solusi.erp.security.model.PermissionGroup;
import com.solusi.erp.security.repository.PermissionGroupRepository;
import com.solusi.erp.security.service.PermissionGroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PermissionGroupServiceImpl implements PermissionGroupService {

    private final PermissionGroupRepository permissionGroupRepository;
    private final PermissionGroupMapper permissionGroupMapper;

    @Override
    @Transactional(readOnly = true)
    public List<PermissionGroupResponse> findAll() {
        return permissionGroupMapper.toResponseList(permissionGroupRepository.findAll());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PermissionGroupResponse> findAll(String keyword, Pageable pageable) {
        return permissionGroupRepository.search(keyword, pageable)
                .map(permissionGroupMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public PermissionGroupRequest getById(Long id) {
        PermissionGroup entity = permissionGroupRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Permission Group not found"));
        return permissionGroupMapper.toRequest(entity);
    }

    @Override
    @Transactional
    public void create(PermissionGroupRequest request) {
        PermissionGroup entity = permissionGroupMapper.toEntity(request);
        permissionGroupRepository.save(entity);
    }

    @Override
    @Transactional
    public void update(Long id, PermissionGroupRequest request) {
        PermissionGroup entity = permissionGroupRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Permission Group not found"));
        
        entity.setCode(request.getCode());
        entity.setNameId(request.getNameId());
        entity.setNameEn(request.getNameEn());
        entity.setBreadcrumbId(request.getBreadcrumbId());
        entity.setBreadcrumbEn(request.getBreadcrumbEn());
        entity.setUrlPath(request.getUrlPath());
        entity.setDescriptionId(request.getDescriptionId());
        entity.setDescriptionEn(request.getDescriptionEn());
        
        permissionGroupRepository.save(entity);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        permissionGroupRepository.deleteById(id);
    }
}
