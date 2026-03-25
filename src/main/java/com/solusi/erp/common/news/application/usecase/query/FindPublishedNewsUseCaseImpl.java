package com.solusi.erp.common.news.application.usecase.query;

import com.solusi.erp.common.news.domain.model.News;
import com.solusi.erp.common.news.domain.repository.NewsRepository;
import java.util.List;

/**
 * PURE Query implementation.
 */
public class FindPublishedNewsUseCaseImpl implements FindPublishedNewsUseCase {

    private final NewsRepository newsRepository;

    public FindPublishedNewsUseCaseImpl(NewsRepository newsRepository) {
        this.newsRepository = newsRepository;
    }

    @Override
    public List<News> execute() {
        // Pada CQRS yang lebih advance, kita bisa langsung mengembalikan DTO 
        // dari repository untuk meningkatkan performa.
        return newsRepository.findPublishedNews();
    }
}
