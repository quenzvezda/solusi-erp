package com.solusi.erp.common.news.application.usecase.command;

import com.solusi.erp.common.news.domain.model.News;

/**
 * Command Use Case Interface for Submitting News for Approval.
 */
public interface SubmitNewsForApprovalUseCase {
    News execute(Long id, Long requesterId, Long approverId);
}
