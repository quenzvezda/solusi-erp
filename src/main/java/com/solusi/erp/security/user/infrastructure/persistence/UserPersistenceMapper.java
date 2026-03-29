package com.solusi.erp.security.user.infrastructure.persistence;

import com.solusi.erp.core.domain.model.AuditMetadata;
import com.solusi.erp.security.user.domain.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserPersistenceMapper {

    @Mapping(target = "metadata", expression = "java(toAuditMetadata(entity))")
    @Mapping(target = "roleId", source = "role.id")
    @Mapping(target = "roleName", source = "role.name")
    @Mapping(target = "roleDescription", source = "role.description")
    @Mapping(target = "profile", expression = "java(toDomainProfile(entity.getProfile()))")
    User toDomain(com.solusi.erp.security.user.infrastructure.persistence.User entity);

    @Mapping(target = "id", source = "metadata.id")
    @Mapping(target = "version", expression = "java(domain.getMetadata().version() != null ? domain.getMetadata().version().intValue() : null)")
    @Mapping(target = "createdDate", source = "metadata.createdDate")
    @Mapping(target = "createdBy", source = "metadata.createdBy")
    @Mapping(target = "updatedDate", source = "metadata.updatedDate")
    @Mapping(target = "updatedBy", source = "metadata.updatedBy")
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "profile", ignore = true)
    @Mapping(target = "createdByUser", ignore = true)
    @Mapping(target = "updatedByUser", ignore = true)
    com.solusi.erp.security.user.infrastructure.persistence.User toEntity(User domain);

    default com.solusi.erp.security.user.domain.model.UserProfile toDomainProfile(UserProfile profile) {
        if (profile == null) {
            return null;
        }
        return new com.solusi.erp.security.user.domain.model.UserProfile(
                profile.getFullName(),
                profile.getPhoneNumber(),
                profile.getAvatarPath(),
                profile.getLanguageCode(),
                profile.getDefaultPageSize(),
                profile.getTheme());
    }

    default UserProfile toEntityProfile(com.solusi.erp.security.user.domain.model.UserProfile profile) {
        if (profile == null) {
            return null;
        }
        UserProfile entity = new UserProfile();
        entity.setFullName(profile.getFullName());
        entity.setPhoneNumber(profile.getPhoneNumber());
        entity.setAvatarPath(profile.getAvatarPath());
        entity.setLanguageCode(profile.getLanguageCode());
        entity.setDefaultPageSize(profile.getDefaultPageSize());
        entity.setTheme(profile.getTheme());
        return entity;
    }

    default AuditMetadata toAuditMetadata(com.solusi.erp.security.user.infrastructure.persistence.User entity) {
        return new AuditMetadata(
                entity.getId(),
                entity.getVersion() != null ? entity.getVersion().longValue() : null,
                entity.getCreatedDate(),
                entity.getCreatedBy(),
                entity.getUpdatedDate(),
                entity.getUpdatedBy());
    }
}
