package com.solusi.erp.common.news.usecases.interactor;

import com.solusi.erp.common.news.entities.News;
import com.solusi.erp.common.news.usecases.CreateNewsInputBoundary;
import com.solusi.erp.common.news.usecases.NewsOutputBoundary;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Clean Architecture: Use Case Interactor (Implementation).
 * Menggantikan istilah Application Service atau Service Impl.
 */
@Service
@RequiredArgsConstructor
public class CreateNewsInteractor implements CreateNewsInputBoundary {
    private final NewsOutputBoundary outputBoundary;

    @Override
    @Transactional
    public News execute(String title, String content, String author) {
        News news = News.createNew(title, content, author);
        return outputBoundary.save(news);
    }
}
