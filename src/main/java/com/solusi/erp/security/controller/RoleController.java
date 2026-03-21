package com.solusi.erp.security.controller;

import com.solusi.erp.core.annotation.DefaultRedirectUrl;
import com.solusi.erp.core.dto.ApiResponse;
import com.solusi.erp.security.dto.PermissionResponse;
import com.solusi.erp.security.dto.RoleRequest;
import com.solusi.erp.security.dto.RoleResponse;
import com.solusi.erp.security.service.RoleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/security/roles")
@RequiredArgsConstructor
@DefaultRedirectUrl
public class RoleController {

    private final RoleService roleService;
    private final MessageSource messageSource;

    @GetMapping
    @PreAuthorize("hasAuthority('ROLES_READ')")
    public String list(Model model) {
        model.addAttribute("roles", roleService.findAll());
        return "security/roles/list";
    }

    @GetMapping("/create")
    @PreAuthorize("hasAuthority('ROLES_CREATE')")
    public String showCreateForm(Model model) {
        model.addAttribute("roleRequest", new RoleRequest());
        model.addAttribute("groupedPermissions", getGroupedPermissions());
        return "security/roles/form";
    }

    @PostMapping("/create")
    @PreAuthorize("hasAuthority('ROLES_CREATE')")
    @ResponseBody
    public ResponseEntity<ApiResponse<RoleResponse>> create(@Valid @RequestBody RoleRequest request) {
        RoleResponse data = roleService.create(request);
        String msg = messageSource.getMessage("msg.roles.success.create", null, LocaleContextHolder.getLocale());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(msg, data));
    }

    @GetMapping("/edit/{id}")
    @PreAuthorize("hasAuthority('ROLES_UPDATE')")
    public String showEditForm(@PathVariable Long id, Model model) {
        var viewDto = roleService.getRoleEditView(id);
        model.addAttribute("roleRequest", viewDto.getRequest());
        model.addAttribute("auditInfo", viewDto.getAudit());
        model.addAttribute("groupedPermissions", getGroupedPermissions());
        return "security/roles/form";
    }

    @PostMapping("/edit/{id}")
    @PreAuthorize("hasAuthority('ROLES_UPDATE')")
    @ResponseBody
    public ResponseEntity<ApiResponse<RoleResponse>> update(@PathVariable Long id, @Valid @RequestBody RoleRequest request) {
        RoleResponse data = roleService.update(id, request);
        String msg = messageSource.getMessage("msg.roles.success.update", null, LocaleContextHolder.getLocale());
        return ResponseEntity.ok(ApiResponse.success(msg, data));
    }

    private Map<String, List<PermissionResponse>> getGroupedPermissions() {
        return roleService.findAllPermissions().stream()
                .collect(Collectors.groupingBy(p -> {
                    String name = p.getName();
                    int underscoreIndex = name.indexOf('_');
                    return underscoreIndex != -1 ? name.substring(0, underscoreIndex) : 
                        messageSource.getMessage("label.other", null, LocaleContextHolder.getLocale());
                }));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLES_DELETE')")
    @ResponseBody
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        roleService.delete(id);
        return ResponseEntity.ok()
                .header("HX-Trigger", "refresh-table")
                .build();
    }
}
