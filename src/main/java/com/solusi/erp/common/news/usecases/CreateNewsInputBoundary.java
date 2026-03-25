package com.solusi.erp.common.news.usecases;

import com.solusi.erp.common.news.entities.News;

/**
 * Clean Architecture: Input Boundary (Interface).
 * Menggantikan istilah Port In atau UseCase.
 */
public interface CreateNewsInputBoundary {
    News execute(String title, String content, String author);
}
