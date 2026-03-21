package com.solusi.erp.security.service.impl;

import com.solusi.erp.core.dto.FormViewDto;
import com.solusi.erp.security.dto.MenuNodeResponse;
import com.solusi.erp.security.dto.PermissionGroupRequest;
import com.solusi.erp.security.dto.PermissionGroupResponse;
import com.solusi.erp.security.mapper.PermissionGroupMapper;
import com.solusi.erp.security.model.PermissionGroup;
import com.solusi.erp.security.repository.PermissionGroupRepository;
import com.solusi.erp.security.service.PermissionGroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class PermissionGroupServiceImpl implements PermissionGroupService {

    // Dictionary for assigning static icons to auto-generated parent/folder menus.
    // We map using the English Breadcrumb piece so it is consistent across all user locales.
    private static final Map<String, String> PARENT_ICONS = Map.ofEntries(
            Map.entry("Company Admin", "ti-building-skyscraper"),
            Map.entry("Security", "ti-shield-lock"),
            Map.entry("Master Data", "ti-database"),
            Map.entry("Finance Settings", "ti-receipt-tax"),

            Map.entry("Operations", "ti-briefcase"),
            Map.entry("Inventory", "ti-packages"),
            Map.entry("Inventory Management", "ti-packages"),
            Map.entry("Inventory Setup", "ti-settings"),
            Map.entry("Inventory Transactions", "ti-transfer-in"),
            Map.entry("Inventory Reports", "ti-report-analytics"),
            Map.entry("Sales & Distribution", "ti-truck-delivery"),
            Map.entry("Procurement (Purchase)", "ti-shopping-cart"),

            Map.entry("Finance & Accounting", "ti-calculator"),
            Map.entry("Account Receivable (Sales Billing)", "ti-file-invoice"),
            Map.entry("Account Payable (Purchase Invoice)", "ti-receipt"),
            Map.entry("General Ledger", "ti-book"),
            Map.entry("Adjustments", "ti-adjustments-alt"),

            Map.entry("Production", "ti-assembly")
    );

    private final PermissionGroupRepository permissionGroupRepository;
    private final PermissionGroupMapper permissionGroupMapper;
    private final MessageSource messageSource;

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
                .orElseThrow(() -> new RuntimeException(getMessage("msg.error.notfound")));
        return permissionGroupMapper.toRequest(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public FormViewDto<PermissionGroupRequest, Void, PermissionGroupResponse> getEditView(Long id) {
        PermissionGroup entity = permissionGroupRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(getMessage("msg.error.notfound")));
        
        return FormViewDto.<PermissionGroupRequest, Void, PermissionGroupResponse>builder()
                .request(permissionGroupMapper.toRequest(entity))
                .audit(permissionGroupMapper.toResponse(entity))
                .build();
    }

    @Override
    @Transactional
    public PermissionGroupResponse create(PermissionGroupRequest request) {
        PermissionGroup entity = permissionGroupMapper.toEntity(request);
        PermissionGroup saved = permissionGroupRepository.save(entity);
        return permissionGroupMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public PermissionGroupResponse update(Long id, PermissionGroupRequest request) {
        PermissionGroup entity = permissionGroupRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(getMessage("msg.error.notfound")));

        entity.setCode(request.getCode());
        entity.setNameId(request.getNameId());
        entity.setNameEn(request.getNameEn());
        entity.setBreadcrumbId(request.getBreadcrumbId());
        entity.setBreadcrumbEn(request.getBreadcrumbEn());
        entity.setUrlPath(request.getUrlPath());
        entity.setIconClass(request.getIconClass());
        entity.setDescriptionId(request.getDescriptionId());
        entity.setDescriptionEn(request.getDescriptionEn());

        PermissionGroup saved = permissionGroupRepository.save(entity);
        return permissionGroupMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        permissionGroupRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MenuNodeResponse> buildMenuTree(Collection<String> authorities) {
        List<PermissionGroup> groups = permissionGroupRepository.findAllByAuthorities(authorities);
        List<MenuNodeResponse> root = new ArrayList<>();

        for (PermissionGroup group : groups) {
            String[] partsId = group.getBreadcrumbId().split(" > ");
            String[] partsEn = group.getBreadcrumbEn().split(" > ");


            List<MenuNodeResponse> currentList = root;

            for (int i = 0; i < partsId.length; i++) {
                String partId = partsId[i];
                // Fallback to ID part if EN part array is shorter for some reason
                String partEn = (i < partsEn.length) ? partsEn[i] : partId;
                boolean isLast = (i == partsId.length - 1);

                MenuNodeResponse existing = currentList.stream()
                        .filter(n -> partId.equals(n.getNameId()))
                        .findFirst()
                        .orElse(null);

                if (existing == null) {
                    existing = MenuNodeResponse.builder()
                            .nameId(partId)
                            .nameEn(partEn)
                            .urlPath(isLast ? group.getUrlPath() : null)
                            .iconClass(isLast ? group.getIconClass() : PARENT_ICONS.getOrDefault(partEn, "ti-folder"))
                            .children(new ArrayList<>())
                            .build();
                    currentList.add(existing);
                } else if (isLast) {
                    // Update if leaf (case where parent was created first)
                    existing.setUrlPath(group.getUrlPath());
                    existing.setIconClass(group.getIconClass());
                }

                currentList = existing.getChildren();
            }
        }
        return root;
    }

    private String getMessage(String key) {
        return messageSource.getMessage(key, null, LocaleContextHolder.getLocale());
    }
}
