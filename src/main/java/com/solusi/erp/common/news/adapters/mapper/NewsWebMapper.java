package com.solusi.erp.common.news.adapters.mapper;

import com.solusi.erp.common.news.adapters.dto.NewsRequest;
import com.solusi.erp.common.news.adapters.dto.NewsResponse;
import com.solusi.erp.common.news.entities.News;
import com.solusi.erp.core.mapper.AuditMapperHelper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

/**
 * Mapper untuk UI Layer (DTO <-> Domain).
 * Menggunakan AuditMapperHelper untuk mapping ID & Audit fields secara otomatis.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = {AuditMapperHelper.class})
public interface NewsWebMapper {

    @Mapping(target = "title", source = "title")
    @Mapping(target = "content", source = "contentText")
    NewsResponse toResponse(News domain);

    @Mapping(target = "title", source = "title")
    @Mapping(target = "content", source = "contentText")
    NewsRequest toRequest(News domain);
}
