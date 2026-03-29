package com.solusi.erp.security.user.service;

import com.solusi.erp.core.dto.FormViewDto;
import com.solusi.erp.security.user.web.dto.ProfileSaveRequest;
import com.solusi.erp.security.user.web.dto.ProfileResponse;
import com.solusi.erp.security.user.web.dto.UserDetailResponse;
import com.solusi.erp.security.user.web.dto.UserSaveRequest;
import com.solusi.erp.security.user.web.dto.UserSummaryResponse;
import com.solusi.erp.security.user.web.dto.UserUiForm;

public interface UserService {
    org.springframework.data.domain.Page<UserSummaryResponse> findAll(String keyword, org.springframework.data.domain.Pageable pageable);

    UserDetailResponse findById(Long id);

    UserSaveRequest getEditData(Long id);

    FormViewDto<UserSaveRequest, UserUiForm, UserDetailResponse> getUserEditView(Long id);

    UserDetailResponse create(UserSaveRequest request);

    UserDetailResponse update(Long id, UserSaveRequest request);

    void delete(Long id);

    void toggleStatus(Long id);

    ProfileResponse getProfile(String username);

    ProfileSaveRequest getProfileUpdateData(String username);

    FormViewDto<ProfileSaveRequest, Void, ProfileResponse> getProfileEditView(String username);

    ProfileResponse updateProfile(String username, ProfileSaveRequest request);
}
