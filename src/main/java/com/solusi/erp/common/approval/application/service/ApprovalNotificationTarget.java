package com.solusi.erp.common.approval.application.service;

public record ApprovalNotificationTarget(
        String role,
        Long partyId,
        String name,
        String email) {
}
