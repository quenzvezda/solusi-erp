package com.solusi.erp.common.news.adapters.mapper;

import com.solusi.erp.common.news.entities.News;
import com.solusi.erp.common.news.frameworks.persistence.NewsEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface NewsPersistenceMapper {

    @Mapping(target = "content", expression = "java(new com.solusi.erp.common.news.entities.NewsContent(entity.getTitle(), entity.getContent()))")
    News toDomain(NewsEntity entity);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "title", source = "content.title")
    @Mapping(target = "content", source = "content.content")
    @Mapping(target = "status", source = "status")
    @Mapping(target = "publishDate", source = "publishDate")
    @Mapping(target = "expiryDate", source = "expiryDate")
    @Mapping(target = "author", source = "author")
    NewsEntity toEntity(News domain);
}
