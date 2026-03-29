package com.solusi.erp.security.permissiongroup.web.controller;

import com.solusi.erp.core.dto.ApiResponse;
import com.solusi.erp.core.infrastructure.util.PageableMapper;
import com.solusi.erp.security.permissiongroup.application.usecase.command.CreatePermissionGroupUseCase;
import com.solusi.erp.security.permissiongroup.application.usecase.query.FindPermissionGroupsUseCase;
import com.solusi.erp.security.permissiongroup.domain.model.PermissionGroup;
import com.solusi.erp.security.permissiongroup.web.dto.PermissionGroupSaveRequest;
import com.solusi.erp.security.permissiongroup.web.dto.PermissionGroupSummaryResponse;
import com.solusi.erp.security.permissiongroup.web.mapper.PermissionGroupWebMapper;
import jakarta.validation.Valid;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/security/permission-groups")
public class PermissionGroupApiController {

    private final FindPermissionGroupsUseCase findPermissionGroupsUseCase;
    private final CreatePermissionGroupUseCase createPermissionGroupUseCase;
    private final PermissionGroupWebMapper webMapper;
    private final MessageSource messageSource;

    public PermissionGroupApiController(
            FindPermissionGroupsUseCase findPermissionGroupsUseCase,
            CreatePermissionGroupUseCase createPermissionGroupUseCase,
            PermissionGroupWebMapper webMapper,
            MessageSource messageSource) {
        this.findPermissionGroupsUseCase = findPermissionGroupsUseCase;
        this.createPermissionGroupUseCase = createPermissionGroupUseCase;
        this.webMapper = webMapper;
        this.messageSource = messageSource;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('PERMISSIONS_CREATE')")
    public List<PermissionGroupSummaryResponse> list() {
        com.solusi.erp.core.domain.model.Pageable pageable = PageableMapper.toDomain(org.springframework.data.domain.PageRequest.of(0, 1000));
        return findPermissionGroupsUseCase.execute(null, pageable).content().stream()
                .map(webMapper::toSummaryResponse)
                .collect(Collectors.toList());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('PERMISSIONS_CREATE')")
    public ResponseEntity<ApiResponse<PermissionGroupSummaryResponse>> create(@Valid @RequestBody PermissionGroupSaveRequest request) {
        PermissionGroup created = createPermissionGroupUseCase.execute(
                request.getCode(), request.getNameId(), request.getNameEn(),
                request.getBreadcrumbId(), request.getBreadcrumbEn(), request.getUrlPath(),
                request.getIconClass(), request.getDescriptionId(), request.getDescriptionEn());
        String msg = messageSource.getMessage("msg.success.create", null, LocaleContextHolder.getLocale());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(msg, webMapper.toSummaryResponse(created)));
    }
}
