package com.solusi.erp.inventory.goodsissue.infrastructure.config;

import com.solusi.erp.inventory.goodsissue.domain.repository.GoodsIssueRepository;
import com.solusi.erp.inventory.goodsissue.infrastructure.adapter.GoodsIssueRepositoryImpl;
import com.solusi.erp.inventory.goodsissue.infrastructure.persistence.GoodsIssueJpaRepository;
import com.solusi.erp.inventory.goodsissue.infrastructure.persistence.GoodsIssuePersistenceMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GoodsIssueConfig {

    @Bean
    public GoodsIssueRepository goodsIssueRepository(GoodsIssueJpaRepository jpaRepository,
                                                     GoodsIssuePersistenceMapper mapper) {
        return new GoodsIssueRepositoryImpl(jpaRepository, mapper);
    }
}
