package com.solusi.erp.common.news.application.usecase.command;

import com.solusi.erp.common.news.domain.model.News;

import java.time.LocalDateTime;

/**
 * Command Use Case Interface for Updating News.
 */
public interface UpdateNewsUseCase {
    News execute(Long id, String title, String content, LocalDateTime publishDate, LocalDateTime expiryDate);
}
