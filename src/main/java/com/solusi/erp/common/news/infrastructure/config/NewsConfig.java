package com.solusi.erp.common.news.infrastructure.config;

import com.solusi.erp.common.news.application.port.NewsEventPublisher;
import com.solusi.erp.common.news.application.usecase.command.*;
import com.solusi.erp.common.news.application.usecase.query.*;
import com.solusi.erp.common.news.domain.repository.NewsRepository;
import com.solusi.erp.common.news.domain.service.NewsDomainService;
import com.solusi.erp.core.infrastructure.sequence.SequenceGeneratorService;
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
            SequenceGeneratorService sequenceGeneratorService,
            TransactionTemplate transactionTemplate) {
        CreateNewsUseCase pureUseCase = new CreateNewsUseCaseImpl(newsRepository, domainService, sequenceGeneratorService);
        return (title, content, author, publishDate, expiryDate) -> 
            transactionTemplate.execute(status -> pureUseCase.execute(title, content, author, publishDate, expiryDate));
    }

    @Bean
    public UpdateNewsUseCase updateNewsUseCase(
            NewsRepository newsRepository, 
            TransactionTemplate transactionTemplate) {
        UpdateNewsUseCase pureUseCase = new UpdateNewsUseCaseImpl(newsRepository);
        return (id, title, content, publishDate, expiryDate) -> 
            transactionTemplate.execute(status -> pureUseCase.execute(id, title, content, publishDate, expiryDate));
    }

    @Bean
    public SubmitNewsForApprovalUseCase submitNewsForApprovalUseCase(
            NewsRepository newsRepository,
            NewsEventPublisher eventPublisher,
            TransactionTemplate transactionTemplate) {
        SubmitNewsForApprovalUseCase pureUseCase = new SubmitNewsForApprovalUseCaseImpl(newsRepository, eventPublisher);
        return (id, requester, approverId) ->
            transactionTemplate.execute(status -> pureUseCase.execute(id, requester, approverId));
    }

    @Bean
    public PublishNewsUseCase publishNewsUseCase(
            NewsRepository newsRepository,
            TransactionTemplate transactionTemplate) {
        PublishNewsUseCase pureUseCase = new PublishNewsUseCaseImpl(newsRepository);
        return (id, publishDate, expiryDate) ->
            transactionTemplate.execute(status -> pureUseCase.execute(id, publishDate, expiryDate));
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
