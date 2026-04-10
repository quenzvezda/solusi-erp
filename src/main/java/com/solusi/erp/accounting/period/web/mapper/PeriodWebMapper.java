package com.solusi.erp.accounting.period.web.mapper;

import com.solusi.erp.core.dto.BaseAuditResponse;
import com.solusi.erp.core.mapper.AuditMapperHelper;
import com.solusi.erp.accounting.period.domain.model.AccountingPeriod;
import com.solusi.erp.accounting.period.domain.model.FiscalYear;
import com.solusi.erp.accounting.period.web.dto.*;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class PeriodWebMapper {

    @Autowired
    protected AuditMapperHelper auditMapperHelper;

    @Mapping(target = "periodCount", expression = "java(domain.getPeriods() != null ? domain.getPeriods().size() : 0)")
    public abstract FiscalYearSummaryResponse toSummaryResponse(FiscalYear domain);

    @Mapping(target = "periods", source = "periods")
    public abstract FiscalYearDetailResponse toDetailResponse(FiscalYear domain);

    public abstract FiscalYearSaveRequest toSaveRequest(FiscalYear domain);

    @Mapping(target = "status", expression = "java(period.getStatus() != null ? period.getStatus().name() : null)")
    @Mapping(target = "periodNumber", source = "periodNumber")
    public abstract PeriodResponse toPeriodResponse(AccountingPeriod period);

    public abstract List<PeriodResponse> toPeriodResponses(List<AccountingPeriod> periods);

    @AfterMapping
    protected void mapAuditFields(FiscalYear domain, @MappingTarget BaseAuditResponse target) {
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
