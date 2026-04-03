package com.solusi.erp.security.role.web.controller;

import com.solusi.erp.core.annotation.DefaultRedirectUrl;
import com.solusi.erp.core.dto.ApiResponse;
import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.security.permission.application.usecase.query.FindPermissionsUseCase;
import com.solusi.erp.security.permission.domain.model.Permission;
import com.solusi.erp.security.permission.web.dto.PermissionSummaryResponse;
import com.solusi.erp.security.permission.web.mapper.PermissionWebMapper;
import com.solusi.erp.security.role.application.usecase.command.CreateRoleUseCase;
import com.solusi.erp.security.role.application.usecase.command.DeleteRoleUseCase;
import com.solusi.erp.security.role.application.usecase.command.UpdateRoleUseCase;
import com.solusi.erp.security.role.application.usecase.query.FindRoleByIdUseCase;
import com.solusi.erp.security.role.application.usecase.query.FindRolesUseCase;
import com.solusi.erp.security.role.domain.model.Role;
import com.solusi.erp.security.role.web.dto.RoleDetailResponse;
import com.solusi.erp.security.role.web.dto.RoleSaveRequest;
import com.solusi.erp.security.role.web.dto.RoleSummaryResponse;
import com.solusi.erp.security.role.web.mapper.RoleWebMapper;
import com.solusi.erp.util.HtmxResponseUtility;
import jakarta.validation.Valid;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/security/roles")
@DefaultRedirectUrl
public class RoleController {

    private final CreateRoleUseCase createRoleUseCase;
    private final UpdateRoleUseCase updateRoleUseCase;
    private final DeleteRoleUseCase deleteRoleUseCase;
    private final FindRolesUseCase findRolesUseCase;
    private final FindRoleByIdUseCase findRoleByIdUseCase;
    private final FindPermissionsUseCase findPermissionsUseCase;
    private final RoleWebMapper roleWebMapper;
    private final PermissionWebMapper permissionWebMapper;
    private final MessageSource messageSource;

    public RoleController(
            CreateRoleUseCase createRoleUseCase,
            UpdateRoleUseCase updateRoleUseCase,
            DeleteRoleUseCase deleteRoleUseCase,
            FindRolesUseCase findRolesUseCase,
            FindRoleByIdUseCase findRoleByIdUseCase,
            FindPermissionsUseCase findPermissionsUseCase,
            RoleWebMapper roleWebMapper,
            PermissionWebMapper permissionWebMapper,
            MessageSource messageSource) {
        this.createRoleUseCase = createRoleUseCase;
        this.updateRoleUseCase = updateRoleUseCase;
        this.deleteRoleUseCase = deleteRoleUseCase;
        this.findRolesUseCase = findRolesUseCase;
        this.findRoleByIdUseCase = findRoleByIdUseCase;
        this.findPermissionsUseCase = findPermissionsUseCase;
        this.roleWebMapper = roleWebMapper;
        this.permissionWebMapper = permissionWebMapper;
        this.messageSource = messageSource;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ROLES_READ')")
    public String list(Model model) {
        List<Permission> permissions = findPermissionsUseCase.execute();
        List<RoleSummaryResponse> roles = findRolesUseCase.execute().stream()
                .map(role -> toSummaryResponse(role, permissions))
                .collect(Collectors.toList());
        model.addAttribute("roles", roles);
        return "security/roles/list";
    }

    @GetMapping("/create")
    @PreAuthorize("hasAuthority('ROLES_CREATE')")
    public String showCreateForm(Model model) {
        model.addAttribute("roleRequest", new RoleSaveRequest());
        model.addAttribute("groupedPermissions", getGroupedPermissions());
        return "security/roles/form";
    }

    @PostMapping("/create")
    @PreAuthorize("hasAuthority('ROLES_CREATE')")
    @ResponseBody
    public ResponseEntity<ApiResponse<RoleDetailResponse>> create(@Valid @RequestBody RoleSaveRequest request) {
        Role created = createRoleUseCase.execute(request.getName(), request.getDescription(), request.getPermissionIds());
        RoleDetailResponse data = toDetailResponse(created, findPermissionsUseCase.execute());
        String msg = messageSource.getMessage("msg.roles.success.create", null, LocaleContextHolder.getLocale());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(msg, data));
    }

    @GetMapping("/edit/{id}")
    @PreAuthorize("hasAuthority('ROLES_UPDATE')")
    public String showEditForm(@PathVariable Long id, Model model) {
        Role role = findRoleByIdUseCase.execute(id)
                .orElseThrow(() -> new DomainException("msg.error.role.notfound"));

        model.addAttribute("roleRequest", roleWebMapper.toSaveRequest(role));
        model.addAttribute("auditInfo", toDetailResponse(role, findPermissionsUseCase.execute()));
        model.addAttribute("groupedPermissions", getGroupedPermissions());
        return "security/roles/form";
    }

    @PostMapping("/edit/{id}")
    @PreAuthorize("hasAuthority('ROLES_UPDATE')")
    @ResponseBody
    public ResponseEntity<ApiResponse<RoleDetailResponse>> update(@PathVariable Long id, @Valid @RequestBody RoleSaveRequest request) {
        Role updated = updateRoleUseCase.execute(id, request.getName(), request.getDescription(), request.getPermissionIds());
        RoleDetailResponse data = toDetailResponse(updated, findPermissionsUseCase.execute());
        String msg = messageSource.getMessage("msg.roles.success.update", null, LocaleContextHolder.getLocale());
        return ResponseEntity.ok(ApiResponse.success(msg, data));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLES_DELETE')")
    @ResponseBody
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        deleteRoleUseCase.execute(id);
        String msg = messageSource.getMessage("msg.success.delete", null, LocaleContextHolder.getLocale());
        return HtmxResponseUtility.okWithRefreshTableAndSuccess(msg);
    }

    private Map<String, List<PermissionSummaryResponse>> getGroupedPermissions() {
        return findPermissionsUseCase.execute().stream()
                .map(permissionWebMapper::toSummaryResponse)
                .collect(Collectors.groupingBy(permission -> {
                    String name = permission.getName();
                    int underscoreIndex = name.indexOf('_');
                    return underscoreIndex != -1
                            ? name.substring(0, underscoreIndex)
                            : messageSource.getMessage("label.other", null, LocaleContextHolder.getLocale());
                }, TreeMap::new, Collectors.toList()));
    }

    private RoleSummaryResponse toSummaryResponse(Role role, List<Permission> allPermissions) {
        RoleSummaryResponse response = roleWebMapper.toSummaryResponse(role);
        response.setPermissions(roleWebMapper.toPermissionResponses(role.getPermissionIds(), allPermissions, permissionWebMapper));
        return response;
    }

    private RoleDetailResponse toDetailResponse(Role role, List<Permission> allPermissions) {
        RoleDetailResponse response = roleWebMapper.toDetailResponse(role);
        response.setPermissions(roleWebMapper.toPermissionResponses(role.getPermissionIds(), allPermissions, permissionWebMapper));
        return response;
    }
}

