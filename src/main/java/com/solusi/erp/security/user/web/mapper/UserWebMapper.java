package com.solusi.erp.security.user.web.mapper;

import com.solusi.erp.core.mapper.AuditMapperHelper;
import com.solusi.erp.security.user.domain.model.User;
import com.solusi.erp.security.user.domain.model.UserProfile;
import com.solusi.erp.security.user.web.dto.ProfileSaveRequest;
import com.solusi.erp.security.user.web.dto.ProfileResponse;
import com.solusi.erp.security.user.web.dto.UserDetailResponse;
import com.solusi.erp.security.user.web.dto.UserSaveRequest;
import com.solusi.erp.security.user.web.dto.UserSummaryResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UserWebMapper {

    @Autowired
    private AuditMapperHelper auditMapperHelper;

    public UserSummaryResponse toSummaryResponse(User user) {
        UserSummaryResponse response = new UserSummaryResponse();
        if (user == null) {
            return response;
        }
        response.setId(user.getId());
        response.setVersion(user.getMetadata().version() != null ? user.getMetadata().version().intValue() : null);
        response.setCreatedDate(user.getMetadata().createdDate());
        response.setUpdatedDate(user.getMetadata().updatedDate());
        response.setCreatedByName(auditMapperHelper.resolveUserDisplayName(user.getMetadata().createdBy()));
        response.setUpdatedByName(auditMapperHelper.resolveUserDisplayName(user.getMetadata().updatedBy()));
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setRoleName(user.getRoleName());
        response.setEnabled(user.isEnabled());
        response.setPasswordChangeRequired(user.isPasswordChangeRequired());
        response.setPartyId(user.getPartyId());
        response.setPartyCode(user.getPartyCode());
        response.setPartyName(user.getPartyName());
        if (user.getProfile() != null) {
            response.setFullName(user.getProfile().getFullName());
        }
        return response;
    }

    public UserDetailResponse toDetailResponse(User user) {
        UserDetailResponse response = new UserDetailResponse();
        if (user == null) {
            return response;
        }
        response.setId(user.getId());
        response.setVersion(user.getMetadata().version() != null ? user.getMetadata().version().intValue() : null);
        response.setCreatedDate(user.getMetadata().createdDate());
        response.setUpdatedDate(user.getMetadata().updatedDate());
        response.setCreatedByName(auditMapperHelper.resolveUserDisplayName(user.getMetadata().createdBy()));
        response.setUpdatedByName(auditMapperHelper.resolveUserDisplayName(user.getMetadata().updatedBy()));
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setRoleName(user.getRoleName());
        response.setEnabled(user.isEnabled());
        response.setPasswordChangeRequired(user.isPasswordChangeRequired());
        response.setPartyId(user.getPartyId());
        response.setPartyCode(user.getPartyCode());
        response.setPartyName(user.getPartyName());
        if (user.getProfile() != null) {
            response.setFullName(user.getProfile().getFullName());
        }
        return response;
    }

    public UserSaveRequest toSaveRequest(User user) {
        UserSaveRequest request = new UserSaveRequest();
        if (user == null) {
            return request;
        }
        request.setId(user.getId());
        request.setVersion(user.getMetadata().version() != null ? user.getMetadata().version().intValue() : null);
        request.setCreatedDate(user.getMetadata().createdDate());
        request.setUpdatedDate(user.getMetadata().updatedDate());
        request.setCreatedByName(auditMapperHelper.resolveUserDisplayName(user.getMetadata().createdBy()));
        request.setUpdatedByName(auditMapperHelper.resolveUserDisplayName(user.getMetadata().updatedBy()));
        request.setUsername(user.getUsername());
        request.setEmail(user.getEmail());
        request.setRoleId(user.getRoleId());
        request.setEnabled(user.isEnabled());
        request.setPasswordChangeRequired(user.isPasswordChangeRequired());
        request.setPartyId(user.getPartyId());
        request.setPartyCode(user.getPartyCode());
        request.setPartyName(user.getPartyName());
        if (user.getProfile() != null) {
            request.setFullName(user.getProfile().getFullName());
            request.setPhoneNumber(user.getProfile().getPhoneNumber());
        }
        return request;
    }

    public ProfileResponse toProfileResponse(User user) {
        ProfileResponse response = new ProfileResponse();
        if (user == null) {
            return response;
        }
        response.setId(user.getId());
        response.setVersion(user.getMetadata().version() != null ? user.getMetadata().version().intValue() : null);
        response.setCreatedDate(user.getMetadata().createdDate());
        response.setUpdatedDate(user.getMetadata().updatedDate());
        response.setCreatedByName(auditMapperHelper.resolveUserDisplayName(user.getMetadata().createdBy()));
        response.setUpdatedByName(auditMapperHelper.resolveUserDisplayName(user.getMetadata().updatedBy()));
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setRoleName(user.getRoleName());
        UserProfile profile = user.getProfile();
        if (profile != null) {
            response.setFullName(profile.getFullName());
            response.setPhoneNumber(profile.getPhoneNumber());
            response.setAvatarPath(profile.getAvatarPath());
            response.setLanguageCode(profile.getLanguageCode());
            response.setDefaultPageSize(profile.getDefaultPageSize());
            response.setTheme(profile.getTheme());
        }
        return response;
    }

    public ProfileSaveRequest toProfileSaveRequest(User user) {
        ProfileSaveRequest request = new ProfileSaveRequest();
        if (user == null) {
            return request;
        }
        request.setId(user.getId());
        request.setVersion(user.getMetadata().version() != null ? user.getMetadata().version().intValue() : null);
        request.setCreatedDate(user.getMetadata().createdDate());
        request.setUpdatedDate(user.getMetadata().updatedDate());
        request.setCreatedByName(auditMapperHelper.resolveUserDisplayName(user.getMetadata().createdBy()));
        request.setUpdatedByName(auditMapperHelper.resolveUserDisplayName(user.getMetadata().updatedBy()));
        request.setEmail(user.getEmail());
        UserProfile profile = user.getProfile();
        if (profile != null) {
            request.setFullName(profile.getFullName());
            request.setPhoneNumber(profile.getPhoneNumber());
            request.setLanguageCode(profile.getLanguageCode());
            request.setDefaultPageSize(profile.getDefaultPageSize());
            request.setTheme(profile.getTheme());
        }
        return request;
    }
}
