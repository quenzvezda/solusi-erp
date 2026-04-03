package com.solusi.erp.common.approval.infrastructure.persistence;

import com.solusi.erp.common.approval.domain.model.ApprovalHistory;
import com.solusi.erp.common.approval.domain.model.ApprovalRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper between Domain and JPA Entities for Approval Module.
 */
@Component
public class ApprovalPersistenceMapper {

    public ApprovalRequest toDomain(ApprovalRequestEntity entity) {
        ApprovalRequest domain = new ApprovalRequest(
            new com.solusi.erp.core.domain.model.AuditMetadata(
                entity.getId(), 
                entity.getVersion() != null ? entity.getVersion().longValue() : null, 
                entity.getCreatedDate(), 
                entity.getCreatedBy(), 
                entity.getUpdatedDate(), 
                entity.getUpdatedBy()
            ),
            entity.getReferenceType(),
            entity.getReferenceId(),
            entity.getStatus(),
            entity.getCurrentApproverId()
        );
        
        List<ApprovalHistory> historyDomains = entity.getHistories().stream()
            .map(h -> new ApprovalHistory(
                h.getId(), h.getAction(), h.getActorId(), 
                h.getTargetApproverId(), h.getNotes(), h.getActionDate()
            ))
            .collect(Collectors.toList());
            
        domain.setHistories(historyDomains);
        return domain;
    }

    public ApprovalRequestEntity toEntity(ApprovalRequest domain) {
        ApprovalRequestEntity entity = new ApprovalRequestEntity();
        entity.setId(domain.getMetadata().id());
        entity.setVersion(domain.getMetadata().version() != null ? domain.getMetadata().version().intValue() : null);
        entity.setReferenceType(domain.getReferenceType());
        entity.setReferenceId(domain.getReferenceId());
        entity.setStatus(domain.getStatus());
        entity.setCurrentApproverId(domain.getCurrentApproverId());
        
        List<ApprovalHistoryEntity> historyEntities = domain.getHistories().stream()
            .map(h -> {
                ApprovalHistoryEntity he = new ApprovalHistoryEntity();
                he.setId(h.id());
                he.setAction(h.action());
                he.setActorId(h.actorId());
                he.setTargetApproverId(h.targetApproverId());
                he.setNotes(h.notes());
                he.setActionDate(h.actionDate());
                he.setRequest(entity);
                return he;
            })
            .collect(Collectors.toList());
            
        entity.setHistories(historyEntities);
        return entity;
    }
}
