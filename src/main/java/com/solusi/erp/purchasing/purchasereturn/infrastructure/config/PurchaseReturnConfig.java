package com.solusi.erp.purchasing.purchasereturn.infrastructure.config;

import com.solusi.erp.purchasing.purchasereturn.domain.repository.PurchaseReturnRepository;
import com.solusi.erp.purchasing.purchasereturn.infrastructure.adapter.PurchaseReturnRepositoryImpl;
import com.solusi.erp.purchasing.purchasereturn.infrastructure.persistence.PurchaseReturnJpaRepository;
import com.solusi.erp.purchasing.purchasereturn.infrastructure.persistence.PurchaseReturnPersistenceMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PurchaseReturnConfig {

    @Bean
    public PurchaseReturnRepository purchaseReturnDomainRepository(
            PurchaseReturnJpaRepository jpaRepository,
            PurchaseReturnPersistenceMapper mapper) {
        return new PurchaseReturnRepositoryImpl(jpaRepository, mapper);
    }
}
