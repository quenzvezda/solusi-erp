package com.solusi.erp.security.service;

import com.solusi.erp.security.dto.ProfileRequest;
import com.solusi.erp.security.dto.ProfileResponse;
import com.solusi.erp.security.dto.UserRequest;
import com.solusi.erp.security.dto.UserResponse;

import java.util.List;

public interface UserService {
    List<UserResponse> findAll();
    UserResponse findById(Long id);
    UserRequest getEditData(Long id);
    void create(UserRequest request);
    void update(Long id, UserRequest request);
    void delete(Long id);
    void toggleStatus(Long id);

    // Profile Management
    ProfileResponse getProfile(String username);
    ProfileRequest getProfileUpdateData(String username);
    void updateProfile(String username, ProfileRequest request);
}
