package com.solusi.erp.common.approval.application.service;

public record ApprovalActionOccurredPayload(
        Long approvalRequestId,
        String referenceType,
        Long referenceId,
        String referenceCode,
        String documentLabel,
        String documentPath,
        String action,
        String status,
        Long actorPartyId,
        String actorName,
        Long targetApproverPartyId,
        String targetApproverName,
        String targetApproverEmail,
        Long currentApproverPartyId,
        Long requesterPartyId,
        String requesterName,
        String requesterEmail,
        ApprovalNotificationTarget notificationTarget,
        String notes,
        String actedAt) {
}
