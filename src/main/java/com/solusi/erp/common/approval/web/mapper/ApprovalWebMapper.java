package com.solusi.erp.common.approval.web.mapper;

import com.solusi.erp.common.approval.domain.model.ApprovalHistory;
import com.solusi.erp.common.approval.domain.model.ApprovalRequest;
import com.solusi.erp.common.approval.signature.domain.model.ApprovalSignature;
import com.solusi.erp.common.approval.web.dto.ApprovalHistoryResponse;
import com.solusi.erp.common.approval.web.dto.ApprovalSignatureResponse;
import com.solusi.erp.common.approval.web.dto.ApprovalStatusResponse;
import com.solusi.erp.core.dto.LookupDto;
import com.solusi.erp.core.mapper.AuditMapperHelper;
import com.solusi.erp.core.storage.domain.port.StorageProvider;
import com.solusi.erp.master.party.domain.port.PartyLookupProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Mapper for converting Approval domain objects to web response DTOs.
 */
@Component
@RequiredArgsConstructor
public class ApprovalWebMapper {

    private final StorageProvider storageProvider;
    private final PartyLookupProvider partyLookupProvider;
    private final AuditMapperHelper auditMapperHelper;

    public ApprovalStatusResponse toStatusResponse(ApprovalRequest request) {
        List<ApprovalHistoryResponse> histories = request.getHistories().stream()
                .map(this::toHistoryResponse)
                .toList();

        return ApprovalStatusResponse.builder()
                .requestId(request.getId())
                .status(request.getStatus().name())
                .referenceType(request.getReferenceType())
                .referenceId(request.getReferenceId())
                .referenceCode(request.getReferenceCode())
                .currentApproverName(resolveActorName(request.getCurrentApproverId()))
                .histories(histories)
                .build();
    }

    public ApprovalHistoryResponse toHistoryResponse(ApprovalHistory history) {
        return ApprovalHistoryResponse.builder()
                .id(history.id())
                .action(history.action().name())
                .actorId(history.actorId())
                .actorName(resolveActorName(history.actorId()))
                .targetApproverName(resolveActorName(history.targetApproverId()))
                .notes(history.notes())
                .actionDate(history.actionDate())
                .build();
    }

    public ApprovalSignatureResponse toSignatureResponse(ApprovalSignature signature) {
        String url = storageProvider.getUrl(signature.getBucketName(), signature.getStorageKey());
        return ApprovalSignatureResponse.builder()
                .requestId(signature.getRequestId())
                .signatureUrl(url)
                .signerUserId(signature.getSignerUserId())
                .signedAt(signature.getStoredAt())
                .build();
    }

    private String resolveActorName(Long actorId) {
        if (actorId == null) return null;
        // Try party lookup first (normal case for approver actions)
        LookupDto lookup = partyLookupProvider.resolve(actorId);
        if (lookup != null) return lookup.name();
        // Fallback: resolve as userId (for REQUESTED action or when party not linked)
        return auditMapperHelper.resolveUserDisplayName(actorId);
    }
}
