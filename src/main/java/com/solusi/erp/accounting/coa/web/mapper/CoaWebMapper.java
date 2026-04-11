package com.solusi.erp.accounting.coa.web.mapper;

import com.solusi.erp.core.dto.BaseAuditResponse;
import com.solusi.erp.core.mapper.AuditMapperHelper;
import com.solusi.erp.accounting.coa.domain.model.ChartOfAccount;
import com.solusi.erp.accounting.coa.web.dto.CoaDetailResponse;
import com.solusi.erp.accounting.coa.web.dto.CoaSaveRequest;
import com.solusi.erp.accounting.coa.web.dto.CoaSummaryResponse;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class CoaWebMapper {

    @Autowired
    protected AuditMapperHelper auditMapperHelper;

    @Mapping(target = "accountType", expression = "java(domain.getAccountType() != null ? domain.getAccountType().name() : null)")
    @Mapping(target = "normalBalance", expression = "java(domain.getNormalBalance() != null ? domain.getNormalBalance().name() : null)")
    @Mapping(target = "parentName", ignore = true)
    public abstract CoaSummaryResponse toSummaryResponse(ChartOfAccount domain);

    @Mapping(target = "accountType", expression = "java(domain.getAccountType() != null ? domain.getAccountType().name() : null)")
    @Mapping(target = "normalBalance", expression = "java(domain.getNormalBalance() != null ? domain.getNormalBalance().name() : null)")
    @Mapping(target = "parentName", ignore = true)
    public abstract CoaDetailResponse toDetailResponse(ChartOfAccount domain);

    @Mapping(target = "accountType", expression = "java(domain.getAccountType() != null ? domain.getAccountType().name() : null)")
    public abstract CoaSaveRequest toSaveRequest(ChartOfAccount domain);

    @AfterMapping
    protected void mapAuditFields(ChartOfAccount domain, @MappingTarget BaseAuditResponse target) {
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
