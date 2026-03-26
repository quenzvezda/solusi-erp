package com.solusi.erp.common.news.application.usecase.command;

import com.solusi.erp.common.news.domain.model.News;
import com.solusi.erp.common.news.domain.repository.NewsRepository;
import com.solusi.erp.core.exception.DomainException;
import java.time.LocalDateTime;

/**
 * Command Use Case Implementation for Finalizing News Publication.
 */
public class PublishNewsUseCaseImpl implements PublishNewsUseCase {

    private final NewsRepository newsRepository;

    public PublishNewsUseCaseImpl(NewsRepository newsRepository) {
        this.newsRepository = newsRepository;
    }

    @Override
    public News execute(Long id, LocalDateTime publishDate, LocalDateTime expiryDate) {
        // Refresh from database to get the latest version
        News news = newsRepository.findById(id)
                .orElseThrow(() -> new DomainException("msg.error.news.not-found"));

        news.publish(publishDate, expiryDate);
        return newsRepository.save(news);
    }
}
