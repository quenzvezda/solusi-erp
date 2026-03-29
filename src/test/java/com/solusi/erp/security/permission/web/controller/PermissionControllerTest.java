package com.solusi.erp.security.permission.web.controller;

import com.solusi.erp.core.domain.model.AuditMetadata;
import com.solusi.erp.security.permission.application.usecase.command.CreatePermissionUseCase;
import com.solusi.erp.security.permission.application.usecase.command.DeletePermissionUseCase;
import com.solusi.erp.security.permission.application.usecase.query.FindPermissionsUseCase;
import com.solusi.erp.security.permission.domain.model.Permission;
import com.solusi.erp.security.permission.web.dto.PermissionSummaryResponse;
import com.solusi.erp.security.permission.web.mapper.PermissionWebMapper;
import com.solusi.erp.security.permissiongroup.application.usecase.query.FindPermissionGroupsUseCase;
import com.solusi.erp.security.permissiongroup.domain.model.PermissionGroup;
import com.solusi.erp.security.permissiongroup.web.dto.PermissionGroupSummaryResponse;
import com.solusi.erp.security.permissiongroup.web.mapper.PermissionGroupWebMapper;
import org.junit.jupiter.api.Test;
import org.springframework.context.MessageSource;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class PermissionControllerTest {

    private final CreatePermissionUseCase createUseCase = mock(CreatePermissionUseCase.class);
    private final DeletePermissionUseCase deleteUseCase = mock(DeletePermissionUseCase.class);
    private final FindPermissionsUseCase findUseCase = mock(FindPermissionsUseCase.class);
    private final FindPermissionGroupsUseCase findGroupsUseCase = mock(FindPermissionGroupsUseCase.class);
    private final PermissionWebMapper webMapper = mock(PermissionWebMapper.class);
    private final PermissionGroupWebMapper permissionGroupWebMapper = mock(PermissionGroupWebMapper.class);
    private final MessageSource messageSource = mock(MessageSource.class);

    private final PermissionController controller = new PermissionController(
            createUseCase, deleteUseCase, findUseCase, findGroupsUseCase, webMapper, permissionGroupWebMapper, messageSource);

    @Test
    void list_returnsListViewAndModel() {
        Permission permission = new Permission(new AuditMetadata(1L, 1L, null, null, null, null), "INV_READ", "desc", 1L);
        PermissionSummaryResponse permissionResponse = new PermissionSummaryResponse();
        permissionResponse.setName("INV_READ");
        permissionResponse.setPermissionGroupId(1L);

        PermissionGroup group = PermissionGroup.createNew("SEC-01", "Keamanan", "Security", "B", "B", "/u", null, null, null);
        com.solusi.erp.core.domain.model.Page<PermissionGroup> pgPage =
                new com.solusi.erp.core.domain.model.Page<>(List.of(group), 0, 1000, 1);
        PermissionGroupSummaryResponse groupResponse = new PermissionGroupSummaryResponse();
        groupResponse.setId(1L);
        groupResponse.setName("Security");

        when(findUseCase.execute()).thenReturn(List.of(permission));
        when(webMapper.toSummaryResponse(permission)).thenReturn(permissionResponse);
        when(findGroupsUseCase.execute(isNull(), any())).thenReturn(pgPage);
        when(permissionGroupWebMapper.toSummaryResponse(group)).thenReturn(groupResponse);

        Model model = new ExtendedModelMap();
        String view = controller.list(model);

        assertThat(view).isEqualTo("security/permissions/list");
        assertThat(model.getAttribute("groupedPermissions")).isNotNull();
        assertThat(model.getAttribute("permissionGroups")).isNotNull();
        assertThat(model.getAttribute("permissionRequest")).isNotNull();
    }

    @Test
    void create_redirectsToList() {
        when(messageSource.getMessage(eq("msg.permissions.success.create"), any(), any()))
                .thenReturn("ok");

        com.solusi.erp.security.permission.web.dto.PermissionSaveRequest request =
                new com.solusi.erp.security.permission.web.dto.PermissionSaveRequest();
        request.setName("INV_READ");
        request.setDescription("desc");
        request.setPermissionGroupId(1L);

        RedirectAttributesModelMap redirect = new RedirectAttributesModelMap();
        String view = controller.create(request, redirect);

        assertThat(view).isEqualTo("redirect:/security/permissions");
        verify(createUseCase).execute("INV_READ", "desc", 1L);
    }
}
