package com.solusi.erp.common.news.application.usecase.command;

import com.solusi.erp.common.news.domain.model.News;
import com.solusi.erp.common.news.domain.repository.NewsRepository;
import com.solusi.erp.core.exception.DomainException;

/**
 * Command Use Case Implementation for Updating News.
 */
public class UpdateNewsUseCaseImpl implements UpdateNewsUseCase {

    private final NewsRepository newsRepository;

    public UpdateNewsUseCaseImpl(NewsRepository newsRepository) {
        this.newsRepository = newsRepository;
    }

    @Override
    public News execute(Long id, String title, String content) {
        News news = newsRepository.findById(id)
            .orElseThrow(() -> new DomainException("msg.error.news.not-found"));
        
        news.updateContent(title, content);
        return newsRepository.save(news);
    }
}
