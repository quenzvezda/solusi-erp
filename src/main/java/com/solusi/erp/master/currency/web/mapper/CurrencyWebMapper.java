package com.solusi.erp.master.currency.web.mapper;

import com.solusi.erp.core.dto.BaseAuditResponse;
import com.solusi.erp.core.mapper.AuditMapperHelper;
import com.solusi.erp.master.currency.domain.model.Currency;
import com.solusi.erp.master.currency.web.dto.CurrencyDetailResponse;
import com.solusi.erp.master.currency.web.dto.CurrencySaveRequest;
import com.solusi.erp.master.currency.web.dto.CurrencySummaryResponse;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Web Mapper for Currency module.
 * Maps between Domain Model and Web DTOs.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class CurrencyWebMapper {

    @Autowired
    protected AuditMapperHelper auditMapperHelper;

    public abstract CurrencySummaryResponse toSummaryResponse(Currency domain);

    public abstract CurrencyDetailResponse toDetailResponse(Currency domain);

    public abstract CurrencySaveRequest toSaveRequest(Currency domain);

    @AfterMapping
    protected void mapAuditFields(Currency domain, @MappingTarget BaseAuditResponse target) {
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
