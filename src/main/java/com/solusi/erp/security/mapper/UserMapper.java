package com.solusi.erp.security.mapper;

import com.solusi.erp.security.dto.ProfileRequest;
import com.solusi.erp.security.dto.ProfileResponse;
import com.solusi.erp.security.dto.UserRequest;
import com.solusi.erp.security.dto.UserResponse;
import com.solusi.erp.security.model.User;
import com.solusi.erp.security.model.UserProfile;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "roleName", source = "role.name")
    @Mapping(target = "fullName", source = "profile.fullName")
    UserResponse toResponse(User user);

    List<UserResponse> toResponseList(List<User> users);

    @Mapping(target = "password", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "profile", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "updatedDate", ignore = true)
    @Mapping(target = "version", ignore = true)
    User toEntity(UserRequest request);

    @Mapping(target = "user", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "updatedDate", ignore = true)
    @Mapping(target = "version", ignore = true)
    UserProfile toProfileEntity(UserRequest request);

    @Mapping(target = "password", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "profile", ignore = true)
    @Mapping(target = "id", ignore = true)
    void updateEntity(UserRequest request, @MappingTarget User user);

    @Mapping(target = "user", ignore = true)
    @Mapping(target = "id", ignore = true)
    void updateProfileEntity(UserRequest request, @MappingTarget UserProfile profile);

    @Mapping(target = "roleName", source = "role.name")
    @Mapping(target = "fullName", source = "profile.fullName")
    @Mapping(target = "phoneNumber", source = "profile.phoneNumber")
    @Mapping(target = "avatarPath", source = "profile.avatarPath")
    @Mapping(target = "languageCode", source = "profile.languageCode")
    @Mapping(target = "defaultPageSize", source = "profile.defaultPageSize")
    @Mapping(target = "theme", source = "profile.theme")
    ProfileResponse toProfileResponse(User user);

    @Mapping(target = "fullName", source = "fullName")
    @Mapping(target = "phoneNumber", source = "phoneNumber")
    @Mapping(target = "languageCode", source = "languageCode")
    @Mapping(target = "defaultPageSize", source = "defaultPageSize")
    @Mapping(target = "theme", source = "theme")
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "id", ignore = true)
    void updateProfileFromRequest(ProfileRequest request, @MappingTarget UserProfile profile);
}
