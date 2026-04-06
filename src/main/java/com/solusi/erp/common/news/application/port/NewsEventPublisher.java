package com.solusi.erp.common.news.application.port;

/**
 * Port for publishing news-related events to the outside world.
 */
public interface NewsEventPublisher {
    void publishApprovalRequested(Long newsId, String newsCode, Long requesterId, Long approverId);
}
