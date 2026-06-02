package com.solusi.erp.purchasing.purchasereturn.infrastructure.config;

import com.solusi.erp.purchasing.purchasereturn.domain.repository.PurchaseReturnRepository;
import com.solusi.erp.purchasing.purchasereturn.application.usecase.query.FindEligiblePurchaseReturnGoodsReceiptsUseCase;
import com.solusi.erp.purchasing.purchasereturn.application.usecase.query.FindPurchaseReturnGrLineSlicesUseCase;
import com.solusi.erp.purchasing.purchasereturn.application.usecase.query.FindPurchaseReturnSerialsUseCase;
import com.solusi.erp.purchasing.purchasereturn.application.usecase.query.GetPurchaseReturnCreateViewUseCase;
import com.solusi.erp.purchasing.purchasereturn.infrastructure.persistence.PurchaseReturnJpaRepository;
import com.solusi.erp.purchasing.purchasereturn.infrastructure.persistence.PurchaseReturnPersistenceMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {PurchaseReturnConfig.class, PurchaseReturnConfigTest.MocksConfig.class})
class PurchaseReturnConfigTest {

    @Autowired
    private PurchaseReturnRepository purchaseReturnRepository;

    @Autowired
    private FindEligiblePurchaseReturnGoodsReceiptsUseCase findEligibleGoodsReceiptsUseCase;

    @Autowired
    private GetPurchaseReturnCreateViewUseCase getPurchaseReturnCreateViewUseCase;

    @Autowired
    private FindPurchaseReturnGrLineSlicesUseCase findPurchaseReturnGrLineSlicesUseCase;

    @Autowired
    private FindPurchaseReturnSerialsUseCase findPurchaseReturnSerialsUseCase;

    @Test
    void wiresPurchaseReturnRepository() {
        assertThat(purchaseReturnRepository).isNotNull();
        assertThat(findEligibleGoodsReceiptsUseCase).isNotNull();
        assertThat(getPurchaseReturnCreateViewUseCase).isNotNull();
        assertThat(findPurchaseReturnGrLineSlicesUseCase).isNotNull();
        assertThat(findPurchaseReturnSerialsUseCase).isNotNull();
    }

    @Configuration
    static class MocksConfig {

        @Bean
        PurchaseReturnJpaRepository purchaseReturnJpaRepository() {
            return mock(PurchaseReturnJpaRepository.class);
        }

        @Bean
        PurchaseReturnPersistenceMapper purchaseReturnPersistenceMapper() {
            return mock(PurchaseReturnPersistenceMapper.class);
        }

        @Bean
        NamedParameterJdbcTemplate namedParameterJdbcTemplate() {
            return mock(NamedParameterJdbcTemplate.class);
        }
    }
}
