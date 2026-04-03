package com.solusi.erp.security.permission.web.controller;

import com.solusi.erp.core.annotation.DefaultRedirectUrl;
import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.core.infrastructure.util.PageableMapper;
import com.solusi.erp.security.permission.application.usecase.command.CreatePermissionUseCase;
import com.solusi.erp.security.permission.application.usecase.command.DeletePermissionUseCase;
import com.solusi.erp.security.permission.application.usecase.query.FindPermissionsUseCase;
import com.solusi.erp.security.permission.domain.model.Permission;
import com.solusi.erp.security.permission.web.dto.PermissionSaveRequest;
import com.solusi.erp.security.permission.web.dto.PermissionSummaryResponse;
import com.solusi.erp.security.permission.web.mapper.PermissionWebMapper;
import com.solusi.erp.security.permissiongroup.application.usecase.query.FindPermissionGroupsUseCase;
import com.solusi.erp.security.permissiongroup.domain.model.PermissionGroup;
import com.solusi.erp.security.permissiongroup.web.dto.PermissionGroupSummaryResponse;
import com.solusi.erp.security.permissiongroup.web.mapper.PermissionGroupWebMapper;
import com.solusi.erp.util.HtmxResponseUtility;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/security/permissions")
@DefaultRedirectUrl
public class PermissionController {

    private final CreatePermissionUseCase createPermissionUseCase;
    private final DeletePermissionUseCase deletePermissionUseCase;
    private final FindPermissionsUseCase findPermissionsUseCase;
    private final FindPermissionGroupsUseCase findPermissionGroupsUseCase;
    private final PermissionWebMapper webMapper;
    private final PermissionGroupWebMapper permissionGroupWebMapper;
    private final MessageSource messageSource;

    public PermissionController(
            CreatePermissionUseCase createPermissionUseCase,
            DeletePermissionUseCase deletePermissionUseCase,
            FindPermissionsUseCase findPermissionsUseCase,
            FindPermissionGroupsUseCase findPermissionGroupsUseCase,
            PermissionWebMapper webMapper,
            PermissionGroupWebMapper permissionGroupWebMapper,
            MessageSource messageSource) {
        this.createPermissionUseCase = createPermissionUseCase;
        this.deletePermissionUseCase = deletePermissionUseCase;
        this.findPermissionsUseCase = findPermissionsUseCase;
        this.findPermissionGroupsUseCase = findPermissionGroupsUseCase;
        this.webMapper = webMapper;
        this.permissionGroupWebMapper = permissionGroupWebMapper;
        this.messageSource = messageSource;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('PERMISSIONS_READ')")
    public String list(Model model) {
        List<PermissionGroupSummaryResponse> permissionGroups = fetchPermissionGroups();
        Map<Long, String> groupNames = permissionGroups.stream()
                .collect(Collectors.toMap(PermissionGroupSummaryResponse::getId, PermissionGroupSummaryResponse::getName));

        Map<String, List<PermissionSummaryResponse>> groupedPermissions = findPermissionsUseCase.execute().stream()
                .map(webMapper::toSummaryResponse)
                .peek(p -> p.setPermissionGroupName(groupNames.get(p.getPermissionGroupId())))
                .collect(Collectors.groupingBy(p -> {
                    String name = p.getName();
                    int underscoreIndex = name.indexOf('_');
                    return underscoreIndex != -1
                            ? name.substring(0, underscoreIndex)
                            : messageSource.getMessage("label.other", null, LocaleContextHolder.getLocale());
                }, TreeMap::new, Collectors.toList()));

        model.addAttribute("groupedPermissions", groupedPermissions);
        model.addAttribute("permissionGroups", permissionGroups);
        model.addAttribute("permissionRequest", new PermissionSaveRequest());
        return "security/permissions/list";
    }

    @PostMapping("/batch")
    @PreAuthorize("hasAuthority('PERMISSIONS_CREATE')")
    public String createBatch(@ModelAttribute("permissionRequest") PermissionSaveRequest request,
                              @RequestParam(value = "actions", required = false) List<String> actions,
                              RedirectAttributes redirectAttributes) {
        try {
            String moduleBase = normalizePermissionName(request.getName());
            List<String> batchActions = (actions == null || actions.isEmpty())
                    ? List.of("READ", "CREATE", "UPDATE", "DELETE")
                    : actions;

            Set<String> existing = findPermissionsUseCase.execute().stream()
                    .map(Permission::getName)
                    .collect(Collectors.toSet());

            for (String action : batchActions) {
                String fullName = moduleBase + "_" + action.toUpperCase(Locale.ROOT);
                if (!existing.contains(fullName)) {
                    String description = messageSource.getMessage("msg.permission.description",
                            new Object[]{action, moduleBase}, LocaleContextHolder.getLocale());
                    createPermissionUseCase.execute(fullName, description, request.getPermissionGroupId());
                }
            }

            redirectAttributes.addFlashAttribute("successMessage",
                    messageSource.getMessage("msg.permissions.success.batch", new Object[]{request.getName()}, LocaleContextHolder.getLocale()));
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", resolveErrorMessage(e));
        }
        return "redirect:/security/permissions";
    }

    @PostMapping("/create")
    @PreAuthorize("hasAuthority('PERMISSIONS_CREATE')")
    public String create(@ModelAttribute("permissionRequest") PermissionSaveRequest request,
                         RedirectAttributes redirectAttributes) {
        try {
            createPermissionUseCase.execute(request.getName(), request.getDescription(), request.getPermissionGroupId());
            redirectAttributes.addFlashAttribute("successMessage",
                    messageSource.getMessage("msg.permissions.success.create", new Object[]{request.getName()}, LocaleContextHolder.getLocale()));
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", resolveErrorMessage(e));
        }
        return "redirect:/security/permissions";
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('PERMISSIONS_DELETE')")
    @ResponseBody
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        deletePermissionUseCase.execute(id);
        String msg = messageSource.getMessage("msg.success.delete", null, LocaleContextHolder.getLocale());
        return HtmxResponseUtility.okWithRefreshTableAndSuccess(msg);
    }

    private String normalizePermissionName(String rawName) {
        return rawName == null ? "" : rawName.trim().toUpperCase(Locale.ROOT).replace(" ", "-");
    }

    private String resolveErrorMessage(Exception e) {
        if (e instanceof DomainException de) {
            return messageSource.getMessage(de.getKey(), de.getArgs(), LocaleContextHolder.getLocale());
        }
        return e.getMessage();
    }

    private List<PermissionGroupSummaryResponse> fetchPermissionGroups() {
        com.solusi.erp.core.domain.model.Pageable pageable = PageableMapper.toDomain(org.springframework.data.domain.PageRequest.of(0, 1000));
        com.solusi.erp.core.domain.model.Page<PermissionGroup> page = findPermissionGroupsUseCase.execute(null, pageable);
        return page.content().stream().map(permissionGroupWebMapper::toSummaryResponse).collect(Collectors.toList());
    }
}
