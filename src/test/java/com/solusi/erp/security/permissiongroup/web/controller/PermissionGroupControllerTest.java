package com.solusi.erp.security.permissiongroup.web.controller;

import com.solusi.erp.core.domain.model.AuditMetadata;
import com.solusi.erp.security.permissiongroup.application.usecase.command.CreatePermissionGroupUseCase;
import com.solusi.erp.security.permissiongroup.application.usecase.command.DeletePermissionGroupUseCase;
import com.solusi.erp.security.permissiongroup.application.usecase.command.UpdatePermissionGroupUseCase;
import com.solusi.erp.security.permissiongroup.application.usecase.query.FindPermissionGroupByIdUseCase;
import com.solusi.erp.security.permissiongroup.application.usecase.query.FindPermissionGroupsUseCase;
import com.solusi.erp.security.permissiongroup.domain.model.PermissionGroup;
import com.solusi.erp.security.permissiongroup.web.mapper.PermissionGroupWebMapper;
import org.junit.jupiter.api.Test;
import org.springframework.context.MessageSource;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class PermissionGroupControllerTest {

    private final CreatePermissionGroupUseCase createUseCase = mock(CreatePermissionGroupUseCase.class);
    private final UpdatePermissionGroupUseCase updateUseCase = mock(UpdatePermissionGroupUseCase.class);
    private final DeletePermissionGroupUseCase deleteUseCase = mock(DeletePermissionGroupUseCase.class);
    private final FindPermissionGroupsUseCase findUseCase = mock(FindPermissionGroupsUseCase.class);
    private final FindPermissionGroupByIdUseCase findByIdUseCase = mock(FindPermissionGroupByIdUseCase.class);
    private final PermissionGroupWebMapper webMapper = mock(PermissionGroupWebMapper.class);
    private final MessageSource messageSource = mock(MessageSource.class);

    private final PermissionGroupController controller = new PermissionGroupController(
            createUseCase, updateUseCase, deleteUseCase, findUseCase, findByIdUseCase, webMapper, messageSource);

    @Test
    void list_returnsListViewWithPage() {
        com.solusi.erp.core.domain.model.Page<PermissionGroup> domainPage =
                new com.solusi.erp.core.domain.model.Page<>(List.of(), 0, 20, 0L);
        when(findUseCase.execute(any(), any())).thenReturn(domainPage);

        Model model = new ExtendedModelMap();
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(0, 20);

        String view = controller.list(null, pageable, model);

        assertThat(view).isEqualTo("security/permission-groups/list");
        assertThat(model.getAttribute("page")).isNotNull();
    }

    @Test
    void showCreateForm_returnsFormViewWithEmptyRequest() {
        Model model = new ExtendedModelMap();
        String view = controller.showCreateForm(model);
        assertThat(view).isEqualTo("security/permission-groups/form");
        assertThat(model.getAttribute("request")).isNotNull();
    }

    @Test
    void showEditForm_returnsFormViewWithExistingData() {
        PermissionGroup pg = new PermissionGroup(
                new AuditMetadata(1L, 1L, null, null, null, null),
                "CODE", "N", "N", "B", "B", "/u", null, null, null);
        when(findByIdUseCase.execute(1L)).thenReturn(Optional.of(pg));

        Model model = new ExtendedModelMap();
        String view = controller.showEditForm(1L, model);

        assertThat(view).isEqualTo("security/permission-groups/form");
        verify(webMapper).toSaveRequest(pg);
        verify(webMapper).toDetailResponse(pg);
    }
}
