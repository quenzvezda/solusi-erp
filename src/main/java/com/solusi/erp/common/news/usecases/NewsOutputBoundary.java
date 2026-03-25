package com.solusi.erp.common.news.usecases;

import com.solusi.erp.common.news.entities.News;
import java.util.List;
import java.util.Optional;

/**
 * Clean Architecture: Output Boundary (Interface).
 * Menggantikan istilah Port Out atau RepositoryPort.
 */
public interface NewsOutputBoundary {
    News save(News news);
    Optional<News> findById(Long id);
    List<News> findPublishedNews();
}
