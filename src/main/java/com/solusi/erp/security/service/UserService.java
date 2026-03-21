package com.solusi.erp.security.service;

import com.solusi.erp.core.dto.FormViewDto;
import com.solusi.erp.security.dto.ProfileRequest;
import com.solusi.erp.security.dto.ProfileResponse;
import com.solusi.erp.security.dto.UserRequest;
import com.solusi.erp.security.dto.UserResponse;
import com.solusi.erp.security.form.UserUIForm;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {
    Page<UserResponse> findAll(String keyword, Pageable pageable);

    UserResponse findById(Long id);

    UserRequest getEditData(Long id);

    FormViewDto<UserRequest, UserUIForm, UserResponse> getUserEditView(Long id);

    UserResponse create(UserRequest request);

    UserResponse update(Long id, UserRequest request);

    void delete(Long id);

    void toggleStatus(Long id);

    // Profile Management
    ProfileResponse getProfile(String username);

    ProfileRequest getProfileUpdateData(String username);

    void updateProfile(String username, ProfileRequest request);
}
