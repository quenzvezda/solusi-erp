package com.solusi.erp.common.news.infrastructure.persistence;

import com.solusi.erp.common.news.domain.model.News;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface NewsPersistenceMapper {

    @Mapping(target = "code", source = "code")
    @Mapping(target = "content", expression = "java(new com.solusi.erp.common.news.domain.model.NewsContent(entity.getTitle(), entity.getContent()))")
    @Mapping(target = "metadata", expression = "java(new com.solusi.erp.core.domain.model.AuditMetadata(entity.getId(), entity.getVersion() != null ? entity.getVersion().longValue() : null, entity.getCreatedDate(), entity.getCreatedBy(), entity.getUpdatedDate(), entity.getUpdatedBy()))")
    News toDomain(NewsEntity entity);

    @Mapping(target = "id", source = "metadata.id")
    @Mapping(target = "version", source = "metadata.version")
    @Mapping(target = "code", source = "code")
    @Mapping(target = "title", source = "content.title")
    @Mapping(target = "content", source = "content.content")
    @Mapping(target = "status", source = "status")
    @Mapping(target = "publishDate", source = "publishDate")
    @Mapping(target = "expiryDate", source = "expiryDate")
    @Mapping(target = "author", source = "author")
    NewsEntity toEntity(News domain);
}
