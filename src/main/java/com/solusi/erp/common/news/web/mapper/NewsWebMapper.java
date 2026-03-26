package com.solusi.erp.common.news.web.mapper;

import com.solusi.erp.common.news.domain.model.News;
import com.solusi.erp.common.news.web.dto.NewsSaveRequest;
import com.solusi.erp.common.news.web.dto.NewsDetailResponse;
import com.solusi.erp.core.mapper.AuditMapperHelper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

/**
 * Mapper untuk UI Layer (DTO <-> Domain).
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = {AuditMapperHelper.class})
public interface NewsWebMapper {

    @Mapping(target = "title", source = "title")
    @Mapping(target = "content", source = "contentText")
    NewsDetailResponse toResponse(News domain);

    @Mapping(target = "title", source = "title")
    @Mapping(target = "content", source = "contentText")
    NewsSaveRequest toRequest(News domain);
}
