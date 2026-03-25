package com.solusi.erp.common.news.infrastructure.config;

import com.solusi.erp.common.news.application.usecase.command.*;
import com.solusi.erp.common.news.application.usecase.query.*;
import com.solusi.erp.common.news.domain.repository.NewsRepository;
import com.solusi.erp.common.news.domain.service.NewsDomainService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Composition Root (Infrastructure Layer).
 */
@Configuration
public class NewsConfig {

    @Bean
    public NewsDomainService newsDomainService(NewsRepository newsRepository) {
        return new NewsDomainService(newsRepository);
    }

    // COMMANDS (Write Side)
    @Bean
    public CreateNewsUseCase createNewsUseCase(
            NewsRepository newsRepository, 
            NewsDomainService domainService,
            TransactionTemplate transactionTemplate) {
        CreateNewsUseCase pureUseCase = new CreateNewsUseCaseImpl(newsRepository, domainService);
        return (title, content, author) -> 
            transactionTemplate.execute(status -> pureUseCase.execute(title, content, author));
    }

    @Bean
    public UpdateNewsUseCase updateNewsUseCase(
            NewsRepository newsRepository, 
            TransactionTemplate transactionTemplate) {
        UpdateNewsUseCase pureUseCase = new UpdateNewsUseCaseImpl(newsRepository);
        return (id, title, content) -> 
            transactionTemplate.execute(status -> pureUseCase.execute(id, title, content));
    }

    // QUERIES (Read Side)
    @Bean
    public FindPublishedNewsUseCase findPublishedNewsUseCase(
            NewsRepository newsRepository, 
            TransactionTemplate transactionTemplate) {
        FindPublishedNewsUseCase pureUseCase = new FindPublishedNewsUseCaseImpl(newsRepository);
        return () -> {
            transactionTemplate.setReadOnly(true);
            return transactionTemplate.execute(status -> pureUseCase.execute());
        };
    }
}
