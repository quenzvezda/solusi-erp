package com.solusi.erp.common.news.application.usecase.command;

import com.solusi.erp.common.news.domain.model.News;
import com.solusi.erp.common.news.domain.repository.NewsRepository;
import com.solusi.erp.common.news.domain.service.NewsDomainService;
import com.solusi.erp.core.infrastructure.sequence.SequenceGeneratorService;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;

import java.time.LocalDateTime;

/**
 * Command Use Case Implementation for Creating News.
 */
public class CreateNewsUseCaseImpl implements CreateNewsUseCase {

    private final NewsRepository newsRepository;
    private final NewsDomainService domainService;
    private final SequenceGeneratorService sequenceGeneratorService;

    public CreateNewsUseCaseImpl(NewsRepository newsRepository, NewsDomainService domainService, SequenceGeneratorService sequenceGeneratorService) {
        this.newsRepository = newsRepository;
        this.domainService = domainService;
        this.sequenceGeneratorService = sequenceGeneratorService;
    }

    @Override
    public News execute(String title, String content, String author, LocalDateTime publishDate, LocalDateTime expiryDate) {
        domainService.validateTitleUniqueness(title);
        String code = sequenceGeneratorService.generate("NEWS");
        String sanitizedContent = Jsoup.clean(content != null ? content : "", Safelist.relaxed());
        News news = News.createNew(code, title, sanitizedContent, author, publishDate, expiryDate);
        return newsRepository.save(news);
    }
}
