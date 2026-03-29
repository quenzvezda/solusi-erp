package com.solusi.erp.security.permissiongroup.web.controller;

import com.solusi.erp.core.annotation.DefaultRedirectUrl;
import com.solusi.erp.core.domain.model.Pageable;
import com.solusi.erp.core.infrastructure.util.PageableMapper;
import com.solusi.erp.core.dto.ApiResponse;
import com.solusi.erp.security.permissiongroup.application.usecase.command.CreatePermissionGroupUseCase;
import com.solusi.erp.security.permissiongroup.application.usecase.command.DeletePermissionGroupUseCase;
import com.solusi.erp.security.permissiongroup.application.usecase.command.UpdatePermissionGroupUseCase;
import com.solusi.erp.security.permissiongroup.application.usecase.query.FindPermissionGroupByIdUseCase;
import com.solusi.erp.security.permissiongroup.application.usecase.query.FindPermissionGroupsUseCase;
import com.solusi.erp.security.permissiongroup.domain.model.PermissionGroup;
import com.solusi.erp.security.permissiongroup.web.dto.PermissionGroupDetailResponse;
import com.solusi.erp.security.permissiongroup.web.dto.PermissionGroupSaveRequest;
import com.solusi.erp.security.permissiongroup.web.dto.PermissionGroupSummaryResponse;
import com.solusi.erp.security.permissiongroup.web.mapper.PermissionGroupWebMapper;
import com.solusi.erp.util.HtmxResponseUtility;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/security/menu-groups")
@RequiredArgsConstructor
@DefaultRedirectUrl
public class PermissionGroupController {

    private final CreatePermissionGroupUseCase createPermissionGroupUseCase;
    private final UpdatePermissionGroupUseCase updatePermissionGroupUseCase;
    private final DeletePermissionGroupUseCase deletePermissionGroupUseCase;
    private final FindPermissionGroupsUseCase findPermissionGroupsUseCase;
    private final FindPermissionGroupByIdUseCase findPermissionGroupByIdUseCase;
    private final PermissionGroupWebMapper webMapper;
    private final MessageSource messageSource;

    @GetMapping
    @PreAuthorize("hasAuthority('MENU-GROUP_READ')")
    public String list(@RequestParam(required = false) String keyword,
                       org.springframework.data.domain.Pageable springPageable,
                       Model model) {
        Pageable domainPageable = PageableMapper.toDomain(springPageable);
        com.solusi.erp.core.domain.model.Page<PermissionGroup> domainPage =
                findPermissionGroupsUseCase.execute(keyword, domainPageable);

        List<PermissionGroupSummaryResponse> content = domainPage.content().stream()
                .map(webMapper::toSummaryResponse)
                .collect(Collectors.toList());

        Page<PermissionGroupSummaryResponse> springPage = new PageImpl<>(content, springPageable, domainPage.totalElements());
        model.addAttribute("page", springPage);
        model.addAttribute("keyword", keyword);
        return "security/permission-groups/list";
    }

    @GetMapping("/create")
    @PreAuthorize("hasAuthority('MENU-GROUP_CREATE')")
    public String showCreateForm(Model model) {
        model.addAttribute("request", new PermissionGroupSaveRequest());
        return "security/permission-groups/form";
    }

    @PostMapping("/create")
    @PreAuthorize("hasAuthority('MENU-GROUP_CREATE')")
    @ResponseBody
    public ResponseEntity<ApiResponse<PermissionGroupDetailResponse>> create(@Valid @RequestBody PermissionGroupSaveRequest request) {
        PermissionGroup domain = createPermissionGroupUseCase.execute(
                request.getCode(), request.getNameId(), request.getNameEn(),
                request.getBreadcrumbId(), request.getBreadcrumbEn(), request.getUrlPath(),
                request.getIconClass(), request.getDescriptionId(), request.getDescriptionEn());
        PermissionGroupDetailResponse data = webMapper.toDetailResponse(domain);
        String msg = messageSource.getMessage("msg.success.create", null, LocaleContextHolder.getLocale());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(msg, data));
    }

    @GetMapping("/edit/{id}")
    @PreAuthorize("hasAuthority('MENU-GROUP_UPDATE')")
    public String showEditForm(@PathVariable Long id, Model model) {
        PermissionGroup domain = findPermissionGroupByIdUseCase.execute(id)
                .orElseThrow(() -> new RuntimeException("PermissionGroup not found: " + id));
        model.addAttribute("request", webMapper.toSaveRequest(domain));
        model.addAttribute("auditInfo", webMapper.toDetailResponse(domain));
        return "security/permission-groups/form";
    }

    @PostMapping("/edit/{id}")
    @PreAuthorize("hasAuthority('MENU-GROUP_UPDATE')")
    @ResponseBody
    public ResponseEntity<ApiResponse<PermissionGroupDetailResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody PermissionGroupSaveRequest request) {
        PermissionGroup domain = updatePermissionGroupUseCase.execute(
                id, request.getNameId(), request.getNameEn(),
                request.getBreadcrumbId(), request.getBreadcrumbEn(), request.getUrlPath(),
                request.getIconClass(), request.getDescriptionId(), request.getDescriptionEn());
        PermissionGroupDetailResponse data = webMapper.toDetailResponse(domain);
        String msg = messageSource.getMessage("msg.success.update", null, LocaleContextHolder.getLocale());
        return ResponseEntity.ok(ApiResponse.success(msg, data));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('MENU-GROUP_DELETE')")
    @ResponseBody
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        deletePermissionGroupUseCase.execute(id);
        String msg = messageSource.getMessage("msg.success.delete", null, LocaleContextHolder.getLocale());
        return HtmxResponseUtility.okWithRefreshTableAndSuccess(msg);
    }
}
