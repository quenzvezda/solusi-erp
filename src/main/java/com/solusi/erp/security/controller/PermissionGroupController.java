package com.solusi.erp.security.controller;

import com.solusi.erp.core.annotation.DefaultRedirectUrl;
import com.solusi.erp.core.dto.ApiResponse;
import com.solusi.erp.security.dto.PermissionGroupRequest;
import com.solusi.erp.security.dto.PermissionGroupResponse;
import com.solusi.erp.security.service.PermissionGroupService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/security/menu-groups")
@RequiredArgsConstructor
@DefaultRedirectUrl
public class PermissionGroupController {

    private final PermissionGroupService service;
    private final MessageSource messageSource;

    @GetMapping
    @PreAuthorize("hasAuthority('MENU-GROUP_READ')")
    public String list(@RequestParam(value = "keyword", required = false) String keyword,
                       Pageable pageable,
                       Model model) {
        model.addAttribute("page", service.findAll(keyword, pageable));
        return "security/permission-groups/list";
    }

    @GetMapping("/create")
    @PreAuthorize("hasAuthority('MENU-GROUP_CREATE')")
    public String showCreateForm(Model model) {
        model.addAttribute("request", new PermissionGroupRequest());
        return "security/permission-groups/form";
    }

    @PostMapping("/create")
    @PreAuthorize("hasAuthority('MENU-GROUP_CREATE')")
    @ResponseBody
    public ResponseEntity<ApiResponse<PermissionGroupResponse>> create(@Valid @RequestBody PermissionGroupRequest request) {
        PermissionGroupResponse data = service.create(request);
        String msg = messageSource.getMessage("msg.success.create", null, LocaleContextHolder.getLocale());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(msg, data));
    }

    @GetMapping("/edit/{id}")
    @PreAuthorize("hasAuthority('MENU-GROUP_UPDATE')")
    public String showEditForm(@PathVariable Long id, Model model) {
        var viewDto = service.getEditView(id);
        model.addAttribute("request", viewDto.getRequest());
        model.addAttribute("auditInfo", viewDto.getAudit());
        return "security/permission-groups/form";
    }

    @PostMapping("/edit/{id}")
    @PreAuthorize("hasAuthority('MENU-GROUP_UPDATE')")
    @ResponseBody
    public ResponseEntity<ApiResponse<PermissionGroupResponse>> update(@PathVariable Long id, @Valid @RequestBody PermissionGroupRequest request) {
        PermissionGroupResponse data = service.update(id, request);
        String msg = messageSource.getMessage("msg.success.update", null, LocaleContextHolder.getLocale());
        return ResponseEntity.ok(ApiResponse.success(msg, data));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('MENU-GROUP_DELETE')")
    @ResponseBody
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok()
                .header("HX-Trigger", "refresh-table")
                .build();
    }
}
