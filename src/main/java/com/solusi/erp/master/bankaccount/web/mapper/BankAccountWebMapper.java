package com.solusi.erp.master.bankaccount.web.mapper;

import com.solusi.erp.core.dto.BaseAuditResponse;
import com.solusi.erp.core.mapper.AuditMapperHelper;
import com.solusi.erp.master.bankaccount.domain.model.BankAccount;
import com.solusi.erp.master.bankaccount.web.dto.BankAccountDetailResponse;
import com.solusi.erp.master.bankaccount.web.dto.BankAccountSaveRequest;
import com.solusi.erp.master.bankaccount.web.dto.BankAccountSummaryResponse;
import org.mapstruct.*;

/**
 * Web Mapper for BankAccount module.
 * Maps between Domain Model and Web DTOs.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = {AuditMapperHelper.class})
public abstract class BankAccountWebMapper {

    public abstract BankAccountSummaryResponse toSummaryResponse(BankAccount domain);

    public abstract BankAccountDetailResponse toDetailResponse(BankAccount domain);

    public abstract BankAccountSaveRequest toSaveRequest(BankAccount domain);

    @AfterMapping
    protected void mapAuditFields(BankAccount domain, @MappingTarget BaseAuditResponse target) {
        if (domain.getMetadata() != null) {
            target.setId(domain.getId());
            target.setVersion(domain.getMetadata().version() != null ? domain.getMetadata().version().intValue() : null);
            target.setCreatedDate(domain.getMetadata().createdDate());
            target.setUpdatedDate(domain.getMetadata().updatedDate());
        }
    }
}
