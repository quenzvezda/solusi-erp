package com.solusi.erp.common.news.application.usecase.command;

import com.solusi.erp.common.news.domain.model.News;

/**
 * Command Use Case Interface for Creating News.
 */
public interface CreateNewsUseCase {
    News execute(String title, String content, String author);
}
