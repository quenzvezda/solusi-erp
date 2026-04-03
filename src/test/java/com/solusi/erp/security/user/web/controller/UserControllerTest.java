package com.solusi.erp.security.user.web.controller;

import com.solusi.erp.core.dto.FormViewDto;
import com.solusi.erp.security.role.application.usecase.query.FindRolesUseCase;
import com.solusi.erp.security.role.domain.model.Role;
import com.solusi.erp.security.role.web.mapper.RoleWebMapper;
import com.solusi.erp.security.user.service.UserService;
import com.solusi.erp.security.user.web.dto.UserDetailResponse;
import com.solusi.erp.security.user.web.dto.UserSaveRequest;
import com.solusi.erp.security.user.web.dto.UserUiForm;
import org.junit.jupiter.api.Test;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.PageImpl;
import org.springframework.ui.ExtendedModelMap;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class UserControllerTest {

    private final UserService userService = mock(UserService.class);
    private final FindRolesUseCase findRolesUseCase = mock(FindRolesUseCase.class);
    private final RoleWebMapper roleWebMapper = mock(RoleWebMapper.class);
    private final MessageSource messageSource = mock(MessageSource.class);

    private final UserController controller = new UserController(userService, findRolesUseCase, roleWebMapper, messageSource);

    @Test
    void list_returnsUserListTemplate() {
        when(userService.findAll(any(), any())).thenReturn(new PageImpl<>(List.of()));

        String view = controller.list(null, org.springframework.data.domain.PageRequest.of(0, 10), new ExtendedModelMap());

        assertThat(view).isEqualTo("security/users/list");
    }

    @Test
    void showEditForm_populatesModel() {
        UserSaveRequest request = new UserSaveRequest();
        UserDetailResponse response = new UserDetailResponse();
        FormViewDto<UserSaveRequest, UserUiForm, UserDetailResponse> dto =
                new FormViewDto<>(request, new UserUiForm(), response);
        when(userService.getUserEditView(1L)).thenReturn(dto);
        when(findRolesUseCase.execute()).thenReturn(List.of(Role.createNew("ROLE_USER", "", Set.of())));

        ExtendedModelMap model = new ExtendedModelMap();
        String view = controller.showEditForm(1L, model);

        assertThat(view).isEqualTo("security/users/form");
        assertThat(model.get("userRequest")).isNotNull();
    }
}
