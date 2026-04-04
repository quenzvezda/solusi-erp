package com.solusi.erp.common.news.infrastructure.persistence;

import com.solusi.erp.common.news.domain.model.News;
import com.solusi.erp.common.news.domain.model.NewsContent;
import com.solusi.erp.core.domain.model.AuditMetadata;
import com.solusi.erp.security.user.infrastructure.persistence.User;
import com.solusi.erp.security.user.infrastructure.persistence.UserProfile;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface NewsPersistenceMapper {

    @Mapping(target = "content", expression = "java(new com.solusi.erp.common.news.domain.model.NewsContent(entity.getTitle(), entity.getContent()))")
    @Mapping(target = "metadata", expression = "java(new com.solusi.erp.core.domain.model.AuditMetadata(entity.getId(), entity.getVersion() != null ? entity.getVersion().longValue() : null, entity.getCreatedDate(), entity.getCreatedBy(), entity.getUpdatedDate(), entity.getUpdatedBy()))")
    News toDomain(NewsEntity entity);

    @Mapping(target = "id", source = "metadata.id")
    @Mapping(target = "version", source = "metadata.version")
    @Mapping(target = "title", source = "content.title")
    @Mapping(target = "content", source = "content.content")
    @Mapping(target = "status", source = "status")
    @Mapping(target = "publishDate", source = "publishDate")
    @Mapping(target = "expiryDate", source = "expiryDate")
    @Mapping(target = "author", source = "author")
    NewsEntity toEntity(News domain);

    @AfterMapping
    default void resolveUserNames(NewsEntity entity, @MappingTarget News domain) {
        domain.setCreatedByName(resolveDisplayName(entity.getCreatedByUser()));
        domain.setUpdatedByName(resolveDisplayName(entity.getUpdatedByUser()));
    }

    private static String resolveDisplayName(User user) {
        if (user == null) return null;
        UserProfile profile = user.getProfile();
        if (profile != null && profile.getFullName() != null && !profile.getFullName().isBlank()) {
            return profile.getFullName();
        }
        return user.getUsername();
    }
}
