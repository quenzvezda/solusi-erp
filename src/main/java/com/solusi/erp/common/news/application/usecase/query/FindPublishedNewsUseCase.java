package com.solusi.erp.common.news.application.usecase.query;

import com.solusi.erp.common.news.domain.model.News;
import java.util.List;

/**
 * Query Use Case Interface.
 * Fokus pada pembacaan data (Read side).
 */
public interface FindPublishedNewsUseCase {
    List<News> execute();
}
