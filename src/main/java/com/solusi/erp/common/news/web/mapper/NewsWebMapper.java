package com.solusi.erp.common.news.web.mapper;

import com.solusi.erp.common.news.domain.model.News;
import com.solusi.erp.common.news.web.dto.NewsSaveRequest;
import com.solusi.erp.common.news.web.dto.NewsDetailResponse;
import com.solusi.erp.core.mapper.AuditMapperHelper;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

/**
 * Mapper untuk UI Layer (DTO <-> Domain).
 * Note: id/version are set via @AfterMapping because Lombok @Builder does not include
 * inherited fields from BaseAuditResponse in the generated builder.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = {AuditMapperHelper.class})
public interface NewsWebMapper {

    @Mapping(target = "title", source = "title")
    @Mapping(target = "content", source = "contentText")
    NewsDetailResponse toResponse(News domain);

    @Mapping(target = "title", source = "title")
    @Mapping(target = "content", source = "contentText")
    NewsSaveRequest toRequest(News domain);

    @AfterMapping
    default void applyNewsIds(News source, @MappingTarget NewsDetailResponse target) {
        target.setId(source.getId());
        target.setVersion(source.getVersion() != null ? source.getVersion().intValue() : null);
    }

    @AfterMapping
    default void applyNewsIds(News source, @MappingTarget NewsSaveRequest target) {
        target.setId(source.getId());
        target.setVersion(source.getVersion() != null ? source.getVersion().intValue() : null);
    }
}
