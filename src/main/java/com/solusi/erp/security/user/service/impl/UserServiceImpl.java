package com.solusi.erp.security.user.service.impl;

import com.solusi.erp.core.dto.FormViewDto;
import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.core.infrastructure.util.PageableMapper;
import com.solusi.erp.security.permissiongroup.application.usecase.query.FindPermissionGroupsUseCase;
import com.solusi.erp.security.permissiongroup.web.mapper.PermissionGroupWebMapper;
import com.solusi.erp.security.role.application.usecase.query.FindRolesUseCase;
import com.solusi.erp.security.role.domain.model.Role;
import com.solusi.erp.security.user.application.usecase.command.*;
import com.solusi.erp.security.user.application.usecase.query.*;
import com.solusi.erp.security.user.domain.model.User;
import com.solusi.erp.security.user.service.UserService;
import com.solusi.erp.security.user.web.dto.ProfileSaveRequest;
import com.solusi.erp.security.user.web.dto.ProfileResponse;
import com.solusi.erp.security.user.web.dto.UserDetailResponse;
import com.solusi.erp.security.user.web.dto.UserSaveRequest;
import com.solusi.erp.security.user.web.dto.UserSummaryResponse;
import com.solusi.erp.security.user.web.dto.UserUiForm;
import com.solusi.erp.security.user.web.mapper.UserWebMapper;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.PageImpl;

import java.util.List;

public class UserServiceImpl implements UserService {

    private final CreateUserUseCase createUserUseCase;
    private final UpdateUserUseCase updateUserUseCase;
    private final DeleteUserUseCase deleteUserUseCase;
    private final ToggleUserStatusUseCase toggleUserStatusUseCase;
    private final UpdateProfileUseCase updateProfileUseCase;
    private final FindUsersUseCase findUsersUseCase;
    private final FindUserByIdUseCase findUserByIdUseCase;
    private final GetUserEditViewUseCase getUserEditViewUseCase;
    private final GetProfileUseCase getProfileUseCase;
    private final GetProfileUpdateDataUseCase getProfileUpdateDataUseCase;
    private final FindRolesUseCase findRolesUseCase;
    private final FindPermissionGroupsUseCase findPermissionGroupsUseCase;
    private final UserWebMapper userWebMapper;
    private final PermissionGroupWebMapper permissionGroupWebMapper;
    private final MessageSource messageSource;

    public UserServiceImpl(CreateUserUseCase createUserUseCase,
                           UpdateUserUseCase updateUserUseCase,
                           DeleteUserUseCase deleteUserUseCase,
                           ToggleUserStatusUseCase toggleUserStatusUseCase,
                           UpdateProfileUseCase updateProfileUseCase,
                           FindUsersUseCase findUsersUseCase,
                           FindUserByIdUseCase findUserByIdUseCase,
                           GetUserEditViewUseCase getUserEditViewUseCase,
                           GetProfileUseCase getProfileUseCase,
                           GetProfileUpdateDataUseCase getProfileUpdateDataUseCase,
                           FindRolesUseCase findRolesUseCase,
                           FindPermissionGroupsUseCase findPermissionGroupsUseCase,
                           UserWebMapper userWebMapper,
                           PermissionGroupWebMapper permissionGroupWebMapper,
                           MessageSource messageSource) {
        this.createUserUseCase = createUserUseCase;
        this.updateUserUseCase = updateUserUseCase;
        this.deleteUserUseCase = deleteUserUseCase;
        this.toggleUserStatusUseCase = toggleUserStatusUseCase;
        this.updateProfileUseCase = updateProfileUseCase;
        this.findUsersUseCase = findUsersUseCase;
        this.findUserByIdUseCase = findUserByIdUseCase;
        this.getUserEditViewUseCase = getUserEditViewUseCase;
        this.getProfileUseCase = getProfileUseCase;
        this.getProfileUpdateDataUseCase = getProfileUpdateDataUseCase;
        this.findRolesUseCase = findRolesUseCase;
        this.findPermissionGroupsUseCase = findPermissionGroupsUseCase;
        this.userWebMapper = userWebMapper;
        this.permissionGroupWebMapper = permissionGroupWebMapper;
        this.messageSource = messageSource;
    }

    @Override
    public org.springframework.data.domain.Page<UserSummaryResponse> findAll(String keyword, org.springframework.data.domain.Pageable pageable) {
        com.solusi.erp.core.domain.model.Page<User> page = findUsersUseCase.execute(keyword, PageableMapper.toDomain(pageable));
        List<UserSummaryResponse> rows = page.content().stream().map(this::toSummaryWithRole).toList();
        return new PageImpl<>(rows, pageable, page.totalElements());
    }

    @Override
    public UserDetailResponse findById(Long id) {
        User user = findUserByIdUseCase.execute(id)
                .orElseThrow(() -> new DomainException("msg.error.user.notfound"));
        return toDetailWithRole(user);
    }

    @Override
    public UserSaveRequest getEditData(Long id) {
        User user = findUserByIdUseCase.execute(id)
                .orElseThrow(() -> new DomainException("msg.error.user.notfound"));
        return userWebMapper.toSaveRequest(toUserWithRole(user));
    }

    @Override
    public FormViewDto<UserSaveRequest, UserUiForm, UserDetailResponse> getUserEditView(Long id) {
        User user = getUserEditViewUseCase.execute(id)
                .orElseThrow(() -> new DomainException("msg.error.user.notfound"));
        User withRole = toUserWithRole(user);
        UserSaveRequest request = userWebMapper.toSaveRequest(withRole);
        UserUiForm ui = new UserUiForm(withRole.getRoleName(), withRole.getRoleDescription());
        UserDetailResponse audit = userWebMapper.toDetailResponse(withRole);
        return new FormViewDto<>(request, ui, audit);
    }

    @Override
    public UserDetailResponse create(UserSaveRequest request) {
        User user = createUserUseCase.execute(
                request.getUsername(), request.getEmail(), request.getPassword(), request.getRoleId(),
                request.getFullName(), request.getPhoneNumber(), request.getEnabled(), request.getPartyId());
        return toDetailWithRole(user);
    }

    @Override
    public UserDetailResponse update(Long id, UserSaveRequest request) {
        User user = updateUserUseCase.execute(
                id, request.getUsername(), request.getEmail(), request.getPassword(), request.getRoleId(),
                request.getFullName(), request.getPhoneNumber(), request.getEnabled(), request.getPasswordChangeRequired(),
                request.getPartyId());
        return toDetailWithRole(user);
    }

    @Override
    public void delete(Long id) {
        deleteUserUseCase.execute(id);
    }

    @Override
    public void toggleStatus(Long id) {
        toggleUserStatusUseCase.execute(id);
    }

    @Override
    public ProfileResponse getProfile(String username) {
        User user = getProfileUseCase.execute(username)
                .orElseThrow(() -> new DomainException("msg.error.user.notfound"));
        return userWebMapper.toProfileResponse(toUserWithRole(user));
    }

    @Override
    public ProfileSaveRequest getProfileUpdateData(String username) {
        User user = getProfileUpdateDataUseCase.execute(username)
                .orElseThrow(() -> new DomainException("msg.error.user.notfound"));
        return userWebMapper.toProfileSaveRequest(toUserWithRole(user));
    }

    @Override
    public FormViewDto<ProfileSaveRequest, Void, ProfileResponse> getProfileEditView(String username) {
        User user = getProfileUpdateDataUseCase.execute(username)
                .orElseThrow(() -> new DomainException("msg.error.user.notfound"));
        User withRole = toUserWithRole(user);
        return new FormViewDto<>(userWebMapper.toProfileSaveRequest(withRole), null, userWebMapper.toProfileResponse(withRole));
    }

    @Override
    public ProfileResponse updateProfile(String username, ProfileSaveRequest request) {
        User user = updateProfileUseCase.execute(username, request.getFullName(), request.getEmail(), request.getPhoneNumber(),
                request.getLanguageCode(), request.getDefaultPageSize(), request.getTheme(), request.getCurrentPassword(),
                request.getNewPassword(), request.getConfirmPassword());
        return userWebMapper.toProfileResponse(toUserWithRole(user));
    }

    private User toResponseReadyUser(User user) {
        User withRole = toUserWithRole(user);
        return withRole;
    }

    private UserSummaryResponse toSummaryWithRole(User user) {
        return userWebMapper.toSummaryResponse(toResponseReadyUser(user));
    }

    private UserDetailResponse toDetailWithRole(User user) {
        return userWebMapper.toDetailResponse(toResponseReadyUser(user));
    }

    private User toUserWithRole(User user) {
        if (user.getRoleId() == null) {
            return user;
        }
        Role role = findRolesUseCase.execute().stream()
                .filter(it -> it.getId() != null && it.getId().equals(user.getRoleId()))
                .findFirst()
                .orElse(null);
        if (role != null) {
            user.setPartyReference(user.getPartyId(), user.getPartyCode(), user.getPartyName());
            return new User(user.getMetadata(), user.getUsername(), user.getPassword(), user.getEmail(),
                    user.isEnabled(), user.isPasswordChangeRequired(), user.getLastPasswordChange(),
                    user.getRoleId(), role.getName(), role.getDescription(), user.getPartyId(), user.getPartyCode(),
                    user.getPartyName(), user.getProfile());
        }
        return user;
    }

    private String message(String key) {
        return messageSource.getMessage(key, null, LocaleContextHolder.getLocale());
    }
}
