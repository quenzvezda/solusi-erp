package com.solusi.erp.common.news.domain.service;

import com.solusi.erp.common.news.domain.repository.NewsRepository;
import com.solusi.erp.core.exception.DomainException;

/**
 * PURE Domain Service for News.
 * No Spring Annotations, No Lombok. 100% Pure Java.
 */
public class NewsDomainService {

    private final NewsRepository newsRepository;

    // Manual Constructor (No Lombok)
    public NewsDomainService(NewsRepository newsRepository) {
        this.newsRepository = newsRepository;
    }

    /**
     * Memastikan judul berita unik di seluruh sistem.
     */
    public void validateTitleUniqueness(String title) {
        newsRepository.findByTitle(title).ifPresent(news -> {
            throw new DomainException("msg.error.news.title.duplicate");
        });
    }
}
