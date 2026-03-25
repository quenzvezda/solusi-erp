package com.solusi.erp.common.news.application.usecase.command;

import com.solusi.erp.common.news.domain.model.News;
import com.solusi.erp.common.news.domain.repository.NewsRepository;
import com.solusi.erp.common.news.domain.service.NewsDomainService;

/**
 * Command Use Case Implementation for Creating News.
 */
public class CreateNewsUseCaseImpl implements CreateNewsUseCase {

    private final NewsRepository newsRepository;
    private final NewsDomainService domainService;

    public CreateNewsUseCaseImpl(NewsRepository newsRepository, NewsDomainService domainService) {
        this.newsRepository = newsRepository;
        this.domainService = domainService;
    }

    @Override
    public News execute(String title, String content, String author) {
        domainService.validateTitleUniqueness(title);
        News news = News.createNew(title, content, author);
        return newsRepository.save(news);
    }
}
