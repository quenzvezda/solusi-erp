package com.solusi.erp.inventory.container.web.mapper;

import com.solusi.erp.core.dto.BaseAuditResponse;
import com.solusi.erp.core.mapper.AuditMapperHelper;
import com.solusi.erp.inventory.container.domain.model.Container;
import com.solusi.erp.inventory.container.web.dto.ContainerDetailResponse;
import com.solusi.erp.inventory.container.web.dto.ContainerSaveRequest;
import com.solusi.erp.inventory.container.web.dto.ContainerSummaryResponse;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class ContainerWebMapper {

    @Autowired
    protected AuditMapperHelper auditMapperHelper;

    public abstract ContainerSummaryResponse toSummaryResponse(Container domain);

    public abstract ContainerDetailResponse toDetailResponse(Container domain);

    public abstract ContainerSaveRequest toSaveRequest(Container domain);

    @AfterMapping
    protected void mapAuditFields(Container domain, @MappingTarget BaseAuditResponse target) {
        if (domain.getMetadata() != null) {
            target.setId(domain.getId());
            target.setVersion(domain.getMetadata().version() != null ? domain.getMetadata().version().intValue() : null);
            target.setCreatedDate(domain.getMetadata().createdDate());
            target.setUpdatedDate(domain.getMetadata().updatedDate());
            target.setCreatedByName(auditMapperHelper.resolveUserDisplayName(domain.getMetadata().createdBy()));
            target.setUpdatedByName(auditMapperHelper.resolveUserDisplayName(domain.getMetadata().updatedBy()));
        }
    }
}
