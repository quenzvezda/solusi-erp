package com.solusi.erp.security.role.web.controller;

import com.solusi.erp.core.domain.model.AuditMetadata;
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
import org.junit.jupiter.api.Test;
import org.springframework.context.MessageSource;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class RoleControllerTest {

    private final CreateRoleUseCase createUseCase = mock(CreateRoleUseCase.class);
    private final UpdateRoleUseCase updateUseCase = mock(UpdateRoleUseCase.class);
    private final DeleteRoleUseCase deleteUseCase = mock(DeleteRoleUseCase.class);
    private final FindRolesUseCase findRolesUseCase = mock(FindRolesUseCase.class);
    private final FindRoleByIdUseCase findRoleByIdUseCase = mock(FindRoleByIdUseCase.class);
    private final FindPermissionsUseCase findPermissionsUseCase = mock(FindPermissionsUseCase.class);
    private final RoleWebMapper roleWebMapper = mock(RoleWebMapper.class);
    private final PermissionWebMapper permissionWebMapper = mock(PermissionWebMapper.class);
    private final MessageSource messageSource = mock(MessageSource.class);

    private final RoleController controller = new RoleController(
            createUseCase, updateUseCase, deleteUseCase, findRolesUseCase, findRoleByIdUseCase,
            findPermissionsUseCase, roleWebMapper, permissionWebMapper, messageSource);

    @Test
    void list_returnsListViewAndModel() {
        Role role = new Role(new AuditMetadata(1L, 1L, null, null, null, null), "ROLE_USER", "desc", Set.of(1L));
        RoleSummaryResponse summaryResponse = new RoleSummaryResponse();
        summaryResponse.setName("ROLE_USER");

        Permission permission = new Permission(new AuditMetadata(1L, 1L, null, null, null, null), "USERS_READ", "desc", 1L);
        PermissionSummaryResponse permissionSummary = new PermissionSummaryResponse();
        permissionSummary.setId(1L);
        permissionSummary.setName("USERS_READ");

        when(findRolesUseCase.execute()).thenReturn(List.of(role));
        when(findPermissionsUseCase.execute()).thenReturn(List.of(permission));
        when(roleWebMapper.toSummaryResponse(role)).thenReturn(summaryResponse);
        when(roleWebMapper.toPermissionResponses(eq(Set.of(1L)), anyList(), eq(permissionWebMapper)))
                .thenReturn(Set.of(permissionSummary));

        Model model = new ExtendedModelMap();
        String view = controller.list(model);

        assertThat(view).isEqualTo("security/roles/list");
        assertThat(model.getAttribute("roles")).isNotNull();
    }

    @Test
    void showEditForm_returnsFormViewWithRoleData() {
        Role role = new Role(new AuditMetadata(1L, 1L, null, null, null, null), "ROLE_USER", "desc", Set.of(1L));
        when(roleWebMapper.toSaveRequest(role)).thenReturn(new RoleSaveRequest());
        when(roleWebMapper.toDetailResponse(role)).thenReturn(new RoleDetailResponse());
        when(findRoleByIdUseCase.execute(1L)).thenReturn(Optional.of(role));
        when(findPermissionsUseCase.execute()).thenReturn(List.of());

        Model model = new ExtendedModelMap();
        String view = controller.showEditForm(1L, model);

        assertThat(view).isEqualTo("security/roles/form");
        verify(roleWebMapper).toSaveRequest(role);
        verify(roleWebMapper).toDetailResponse(role);
    }
}

