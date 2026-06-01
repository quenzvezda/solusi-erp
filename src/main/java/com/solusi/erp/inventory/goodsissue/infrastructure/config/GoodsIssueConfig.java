package com.solusi.erp.inventory.goodsissue.infrastructure.config;

import com.solusi.erp.inventory.goodsissue.application.usecase.query.FindGoodsIssuesUseCase;
import com.solusi.erp.inventory.goodsissue.application.usecase.query.FindGoodsIssuesUseCaseImpl;
import com.solusi.erp.inventory.goodsissue.application.usecase.query.GetGoodsIssueCreateViewUseCase;
import com.solusi.erp.inventory.goodsissue.application.usecase.query.GetGoodsIssueCreateViewUseCaseImpl;
import com.solusi.erp.inventory.goodsissue.application.usecase.query.GetGoodsIssueEditViewUseCase;
import com.solusi.erp.inventory.goodsissue.application.usecase.query.GetGoodsIssueEditViewUseCaseImpl;
import com.solusi.erp.inventory.goodsissue.application.usecase.query.GetGoodsIssueUseCase;
import com.solusi.erp.inventory.goodsissue.application.usecase.query.GetGoodsIssueUseCaseImpl;
import com.solusi.erp.inventory.goodsissue.domain.port.GoodsIssueReferenceLookupProvider;
import com.solusi.erp.inventory.goodsissue.domain.port.GoodsIssueSourceResolver;
import com.solusi.erp.inventory.goodsissue.domain.repository.GoodsIssueRepository;
import com.solusi.erp.inventory.goodsissue.infrastructure.adapter.GoodsIssueReferenceLookupProviderImpl;
import com.solusi.erp.inventory.goodsissue.infrastructure.adapter.GoodsIssueRepositoryImpl;
import com.solusi.erp.inventory.goodsissue.infrastructure.persistence.GoodsIssueJpaRepository;
import com.solusi.erp.inventory.goodsissue.infrastructure.persistence.GoodsIssuePersistenceMapper;
import com.solusi.erp.inventory.goodsissue.infrastructure.service.GoodsIssueSourceResolverRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class GoodsIssueConfig {

    @Bean
    public GoodsIssueRepository goodsIssueRepository(GoodsIssueJpaRepository jpaRepository,
                                                     GoodsIssuePersistenceMapper mapper) {
        return new GoodsIssueRepositoryImpl(jpaRepository, mapper);
    }

    @Bean
    public GoodsIssueReferenceLookupProvider goodsIssueReferenceLookupProvider() {
        return new GoodsIssueReferenceLookupProviderImpl();
    }

    @Bean
    public GoodsIssueSourceResolverRegistry goodsIssueSourceResolverRegistry(List<GoodsIssueSourceResolver> resolvers) {
        return new GoodsIssueSourceResolverRegistry(resolvers);
    }

    @Bean
    public GetGoodsIssueCreateViewUseCase getGoodsIssueCreateViewUseCase(GoodsIssueSourceResolverRegistry registry) {
        return new GetGoodsIssueCreateViewUseCaseImpl(registry);
    }

    @Bean
    public GetGoodsIssueUseCase getGoodsIssueUseCase(GoodsIssueRepository repository) {
        return new GetGoodsIssueUseCaseImpl(repository);
    }

    @Bean
    public GetGoodsIssueEditViewUseCase getGoodsIssueEditViewUseCase(GoodsIssueRepository repository) {
        return new GetGoodsIssueEditViewUseCaseImpl(repository);
    }

    @Bean
    public FindGoodsIssuesUseCase findGoodsIssuesUseCase(GoodsIssueRepository repository) {
        return new FindGoodsIssuesUseCaseImpl(repository);
    }
}
