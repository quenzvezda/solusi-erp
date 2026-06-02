package com.solusi.erp.purchasing.purchasereturn.infrastructure.config;

import com.solusi.erp.purchasing.purchasereturn.domain.repository.PurchaseReturnRepository;
import com.solusi.erp.purchasing.purchasereturn.application.usecase.query.FindEligiblePurchaseReturnGoodsReceiptsUseCase;
import com.solusi.erp.purchasing.purchasereturn.application.usecase.query.FindEligiblePurchaseReturnGoodsReceiptsUseCaseImpl;
import com.solusi.erp.purchasing.purchasereturn.application.usecase.query.FindPurchaseReturnGrLineSlicesUseCase;
import com.solusi.erp.purchasing.purchasereturn.application.usecase.query.FindPurchaseReturnGrLineSlicesUseCaseImpl;
import com.solusi.erp.purchasing.purchasereturn.application.usecase.query.FindPurchaseReturnSerialsUseCase;
import com.solusi.erp.purchasing.purchasereturn.application.usecase.query.FindPurchaseReturnSerialsUseCaseImpl;
import com.solusi.erp.purchasing.purchasereturn.application.usecase.query.GetEligiblePurchaseReturnPurchaseOrderLookupUseCase;
import com.solusi.erp.purchasing.purchasereturn.application.usecase.query.GetEligiblePurchaseReturnPurchaseOrderLookupUseCaseImpl;
import com.solusi.erp.purchasing.purchasereturn.application.usecase.query.GetPurchaseReturnCreateViewUseCase;
import com.solusi.erp.purchasing.purchasereturn.application.usecase.query.GetPurchaseReturnCreateViewUseCaseImpl;
import com.solusi.erp.purchasing.purchasereturn.domain.port.PurchaseReturnSourceQueryPort;
import com.solusi.erp.purchasing.purchasereturn.infrastructure.adapter.PurchaseReturnSourceQueryAdapter;
import com.solusi.erp.purchasing.purchasereturn.infrastructure.adapter.PurchaseReturnRepositoryImpl;
import com.solusi.erp.purchasing.purchasereturn.infrastructure.persistence.PurchaseReturnJpaRepository;
import com.solusi.erp.purchasing.purchasereturn.infrastructure.persistence.PurchaseReturnPersistenceMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

@Configuration
public class PurchaseReturnConfig {

    @Bean
    public PurchaseReturnRepository purchaseReturnDomainRepository(
            PurchaseReturnJpaRepository jpaRepository,
            PurchaseReturnPersistenceMapper mapper) {
        return new PurchaseReturnRepositoryImpl(jpaRepository, mapper);
    }

    @Bean
    public PurchaseReturnSourceQueryPort purchaseReturnSourceQueryPort(
            NamedParameterJdbcTemplate jdbcTemplate) {
        return new PurchaseReturnSourceQueryAdapter(jdbcTemplate);
    }

    @Bean
    public FindEligiblePurchaseReturnGoodsReceiptsUseCase findEligiblePurchaseReturnGoodsReceiptsUseCase(
            PurchaseReturnSourceQueryPort queryPort) {
        return new FindEligiblePurchaseReturnGoodsReceiptsUseCaseImpl(queryPort);
    }

    @Bean
    public GetEligiblePurchaseReturnPurchaseOrderLookupUseCase getEligiblePurchaseReturnPurchaseOrderLookupUseCase(
            PurchaseReturnSourceQueryPort queryPort) {
        return new GetEligiblePurchaseReturnPurchaseOrderLookupUseCaseImpl(queryPort);
    }

    @Bean
    public GetPurchaseReturnCreateViewUseCase getPurchaseReturnCreateViewUseCase(
            PurchaseReturnSourceQueryPort queryPort) {
        return new GetPurchaseReturnCreateViewUseCaseImpl(queryPort);
    }

    @Bean
    public FindPurchaseReturnGrLineSlicesUseCase findPurchaseReturnGrLineSlicesUseCase(
            PurchaseReturnSourceQueryPort queryPort) {
        return new FindPurchaseReturnGrLineSlicesUseCaseImpl(queryPort);
    }

    @Bean
    public FindPurchaseReturnSerialsUseCase findPurchaseReturnSerialsUseCase(
            PurchaseReturnSourceQueryPort queryPort) {
        return new FindPurchaseReturnSerialsUseCaseImpl(queryPort);
    }
}
