package com.solusi.erp.common.news.application.usecase.command;

import com.solusi.erp.common.news.application.port.NewsEventPublisher;
import com.solusi.erp.common.news.domain.model.News;
import com.solusi.erp.common.news.domain.repository.NewsRepository;
import com.solusi.erp.core.exception.DomainException;

/**
 * Command Use Case Implementation for Submitting News for Approval.
 */
public class SubmitNewsForApprovalUseCaseImpl implements SubmitNewsForApprovalUseCase {

    private final NewsRepository newsRepository;
    private final NewsEventPublisher eventPublisher;

    public SubmitNewsForApprovalUseCaseImpl(NewsRepository newsRepository, NewsEventPublisher eventPublisher) {
        this.newsRepository = newsRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public News execute(Long id, String requester) {
        News news = newsRepository.findById(id)
                .orElseThrow(() -> new DomainException("msg.error.news.not-found"));

        news.submitForApproval();
        News savedNews = newsRepository.save(news);

        // Publish event to the outside world
        eventPublisher.publishApprovalRequested(savedNews.getId(), requester);

        return savedNews;
    }
}
