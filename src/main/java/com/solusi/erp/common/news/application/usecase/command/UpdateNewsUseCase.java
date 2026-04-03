package com.solusi.erp.common.news.application.usecase.command;

import com.solusi.erp.common.news.domain.model.News;

/**
 * Command Use Case Interface for Updating News.
 */
public interface UpdateNewsUseCase {
    News execute(Long id, String title, String content);
}
