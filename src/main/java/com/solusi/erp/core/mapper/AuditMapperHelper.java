package com.solusi.erp.core.mapper;

import com.solusi.erp.core.dto.BaseAuditResponse;
import com.solusi.erp.core.model.BaseModel;
import com.solusi.erp.security.user.infrastructure.persistence.User;
import com.solusi.erp.security.user.infrastructure.persistence.UserJpaRepository;
import com.solusi.erp.security.user.infrastructure.persistence.UserProfile;
import org.mapstruct.AfterMapping;
import org.mapstruct.MappingTarget;
import org.springframework.stereotype.Component;

@Component
public class AuditMapperHelper {

    private final UserJpaRepository userRepository;

    public AuditMapperHelper(UserJpaRepository userRepository) {
        this.userRepository = userRepository;
    }

    @AfterMapping
    public void mapAuditFields(BaseModel source, @MappingTarget BaseAuditResponse target) {
        if (source == null || target == null) {
            return;
        }

        // Map Basic Fields
        target.setId(source.getId());
        target.setVersion(source.getVersion());
        target.setCreatedDate(source.getCreatedDate());
        target.setUpdatedDate(source.getUpdatedDate());

        // Map createdBy
        User createdUser = source.getCreatedByUser();
        if (createdUser != null) {
            UserProfile profile = createdUser.getProfile();
            target.setCreatedByName(profile != null && profile.getFullName() != null 
                    ? profile.getFullName() : createdUser.getUsername());
        } else if (source.getCreatedBy() != null) {
             target.setCreatedByName("SYSTEM");
        }

        // Map updatedBy
        User updatedUser = source.getUpdatedByUser();
        if (updatedUser != null) {
            UserProfile profile = updatedUser.getProfile();
            target.setUpdatedByName(profile != null && profile.getFullName() != null 
                    ? profile.getFullName() : updatedUser.getUsername());
        } else if (source.getUpdatedBy() != null) {
             target.setUpdatedByName("SYSTEM");
        }
    }

    /**
     * Resolve a user's display name from their ID.
     * Used by Clean Architecture web mappers that map Domain → DTO
     * (where BaseModel is not available as a source).
     */
    public String resolveUserDisplayName(Long userId) {
        if (userId == null) return null;
        return userRepository.findById(userId)
                .map(this::extractDisplayName)
                .orElse("SYSTEM");
    }

    private String extractDisplayName(User user) {
        UserProfile profile = user.getProfile();
        return (profile != null && profile.getFullName() != null && !profile.getFullName().isBlank())
                ? profile.getFullName() : user.getUsername();
    }
}
