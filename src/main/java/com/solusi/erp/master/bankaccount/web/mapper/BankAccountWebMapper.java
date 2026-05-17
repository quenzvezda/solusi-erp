package com.solusi.erp.master.bankaccount.web.mapper;

import com.solusi.erp.core.dto.BaseAuditResponse;
import com.solusi.erp.core.mapper.AuditMapperHelper;
import com.solusi.erp.master.bankaccount.domain.model.BankAccount;
import com.solusi.erp.master.bankaccount.web.dto.BankAccountDetailResponse;
import com.solusi.erp.master.bankaccount.web.dto.BankAccountSaveRequest;
import com.solusi.erp.master.bankaccount.web.dto.BankAccountSummaryResponse;
import com.solusi.erp.master.shared.model.PaymentType;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class BankAccountWebMapper {

    @Autowired
    protected AuditMapperHelper auditMapperHelper;

    @Mapping(target = "accountType", expression = "java(enumToString(domain.getAccountType()))")
    public abstract BankAccountSummaryResponse toSummaryResponse(BankAccount domain);

    @Mapping(target = "accountType", expression = "java(enumToString(domain.getAccountType()))")
    public abstract BankAccountDetailResponse toDetailResponse(BankAccount domain);

    @Mapping(target = "accountType", expression = "java(enumToString(domain.getAccountType()))")
    public abstract BankAccountSaveRequest toSaveRequest(BankAccount domain);

    @AfterMapping
    protected void mapAuditFields(BankAccount domain, @MappingTarget BaseAuditResponse target) {
        if (domain.getMetadata() != null) {
            target.setId(domain.getId());
            target.setVersion(domain.getMetadata().version() != null ? domain.getMetadata().version().intValue() : null);
            target.setCreatedDate(domain.getMetadata().createdDate());
            target.setUpdatedDate(domain.getMetadata().updatedDate());
            target.setCreatedByName(auditMapperHelper.resolveUserDisplayName(domain.getMetadata().createdBy()));
            target.setUpdatedByName(auditMapperHelper.resolveUserDisplayName(domain.getMetadata().updatedBy()));
        }
    }

    protected String enumToString(PaymentType type) {
        return type != null ? type.name() : null;
    }
}
