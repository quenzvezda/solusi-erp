package com.solusi.erp.common.news.domain.repository;

import com.solusi.erp.common.news.domain.model.News;
import java.util.List;
import java.util.Optional;

/**
 * Domain Repository Interface.
 * Kontrak untuk akses data News yang didefinisikan di Layer Domain.
 */
public interface NewsRepository {
    News save(News news);
    Optional<News> findById(Long id);
    Optional<News> findByTitle(String title);
    List<News> findPublishedNews();
}
