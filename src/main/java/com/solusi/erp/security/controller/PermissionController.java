package com.solusi.erp.security.controller;

import com.solusi.erp.security.dto.PermissionRequest;
import com.solusi.erp.security.dto.PermissionResponse;
import com.solusi.erp.security.service.PermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import com.solusi.erp.security.service.PermissionGroupService;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/security/permissions")
@RequiredArgsConstructor
public class PermissionController {

    private final PermissionService permissionService;
    private final PermissionGroupService permissionGroupService;
    private final MessageSource messageSource;

    @GetMapping
    @PreAuthorize("hasAuthority('PERMISSIONS_READ')")
    public String list(Model model) {
        java.util.Map<String, List<PermissionResponse>> groupedPermissions =
            permissionService.findAll().stream()
                .collect(java.util.stream.Collectors.groupingBy(p -> {
                    String name = p.getName();
                    int underscoreIndex = name.indexOf('_');
                    return underscoreIndex != -1 ? name.substring(0, underscoreIndex) : 
                        messageSource.getMessage("label.other", null, LocaleContextHolder.getLocale());
                }, java.util.TreeMap::new, java.util.stream.Collectors.toList()));

        model.addAttribute("groupedPermissions", groupedPermissions);
        model.addAttribute("permissionGroups", permissionGroupService.findAll());
        model.addAttribute("permissionRequest", new PermissionRequest());
        return "security/permissions/list";
    }

    @PostMapping("/batch")
    @PreAuthorize("hasAuthority('PERMISSIONS_CREATE')")
    public String createBatch(@ModelAttribute("permissionRequest") PermissionRequest request,
                              @RequestParam(value = "actions", required = false) List<String> actions,
                              RedirectAttributes redirectAttributes) {
        try {
            request.setBatchActions(actions);
            permissionService.createBatch(request);
            redirectAttributes.addFlashAttribute("successMessage", 
                messageSource.getMessage("msg.permissions.success.batch", new Object[]{request.getName()}, LocaleContextHolder.getLocale()));
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/security/permissions";
    }

    @PostMapping("/create")
    @PreAuthorize("hasAuthority('PERMISSIONS_CREATE')")
    public String create(@ModelAttribute("permissionRequest") PermissionRequest request,
                         RedirectAttributes redirectAttributes) {
        try {
            permissionService.create(request);
            redirectAttributes.addFlashAttribute("successMessage", 
                messageSource.getMessage("msg.permissions.success.create", new Object[]{request.getName()}, LocaleContextHolder.getLocale()));
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/security/permissions";
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('PERMISSIONS_DELETE')")
    @ResponseBody
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        permissionService.delete(id);
        return ResponseEntity.ok()
                .header("HX-Trigger", "refresh-table")
                .build();
    }
}
