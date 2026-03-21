package com.solusi.erp.security.controller;

import com.solusi.erp.security.dto.PermissionResponse;
import com.solusi.erp.security.dto.RoleRequest;
import com.solusi.erp.security.dto.RoleResponse;
import com.solusi.erp.security.service.RoleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/security/roles")
@RequiredArgsConstructor
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
    public String create(@Valid @ModelAttribute("roleRequest") RoleRequest request,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("groupedPermissions", getGroupedPermissions());
            return "security/roles/form";
        }

        try {
            roleService.create(request);
            redirectAttributes.addFlashAttribute("successMessage", 
                messageSource.getMessage("msg.roles.success.create", null, LocaleContextHolder.getLocale()));
            return "redirect:/security/roles";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("groupedPermissions", getGroupedPermissions());
            return "security/roles/form";
        }
    }

    @GetMapping("/edit/{id}")
    @PreAuthorize("hasAuthority('ROLES_UPDATE')")
    public String showEditForm(@PathVariable Long id, Model model) {
        RoleResponse roleResponse = roleService.findById(id);
        
        RoleRequest request = RoleRequest.builder()
                .name(roleResponse.getName())
                .description(roleResponse.getDescription())
                .permissionIds(roleResponse.getPermissions().stream()
                        .map(p -> p.getId())
                        .collect(Collectors.toSet()))
                .build();
        request.setId(roleResponse.getId());

        model.addAttribute("roleRequest", request);
        model.addAttribute("groupedPermissions", getGroupedPermissions());
        return "security/roles/form";
    }

    @PostMapping("/edit/{id}")
    @PreAuthorize("hasAuthority('ROLES_UPDATE')")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("roleRequest") RoleRequest request,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("groupedPermissions", getGroupedPermissions());
            return "security/roles/form";
        }

        try {
            roleService.update(id, request);
            redirectAttributes.addFlashAttribute("successMessage", 
                messageSource.getMessage("msg.roles.success.update", null, LocaleContextHolder.getLocale()));
            return "redirect:/security/roles";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("groupedPermissions", getGroupedPermissions());
            return "security/roles/form";
        }
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
