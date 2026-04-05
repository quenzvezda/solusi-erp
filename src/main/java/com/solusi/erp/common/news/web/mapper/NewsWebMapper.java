package com.solusi.erp.common.news.web.mapper;

import com.solusi.erp.common.news.domain.model.News;
import com.solusi.erp.common.news.web.dto.NewsSaveRequest;
import com.solusi.erp.common.news.web.dto.NewsDetailResponse;
import com.solusi.erp.core.domain.model.AuditMetadata;
import com.solusi.erp.core.dto.BaseAuditResponse;
import com.solusi.erp.core.mapper.AuditMapperHelper;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Web Mapper for News module (Domain → DTO).
 * Resolves audit user names via AuditMapperHelper.resolveUserDisplayName(Long).
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class NewsWebMapper {

    @Autowired
    protected AuditMapperHelper auditMapperHelper;

    @Mapping(target = "code", source = "code")
    @Mapping(target = "title", source = "title")
    @Mapping(target = "content", source = "contentText")
    public abstract NewsDetailResponse toResponse(News domain);

    @Mapping(target = "title", source = "title")
    @Mapping(target = "content", source = "contentText")
    public abstract NewsSaveRequest toRequest(News domain);

    @AfterMapping
    protected void mapAuditFields(News source, @MappingTarget BaseAuditResponse target) {
        AuditMetadata meta = source.getMetadata();
        if (meta != null) {
            target.setId(meta.id());
            target.setVersion(meta.version() != null ? meta.version().intValue() : null);
            target.setCreatedDate(meta.createdDate());
            target.setUpdatedDate(meta.updatedDate());
            target.setCreatedByName(auditMapperHelper.resolveUserDisplayName(meta.createdBy()));
            target.setUpdatedByName(auditMapperHelper.resolveUserDisplayName(meta.updatedBy()));
        }
    }
}
