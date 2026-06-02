package com.solusi.erp.purchasing.purchasereturn.infrastructure.config;

import com.solusi.erp.purchasing.purchasereturn.domain.repository.PurchaseReturnRepository;
import com.solusi.erp.purchasing.purchasereturn.infrastructure.persistence.PurchaseReturnJpaRepository;
import com.solusi.erp.purchasing.purchasereturn.infrastructure.persistence.PurchaseReturnPersistenceMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {PurchaseReturnConfig.class, PurchaseReturnConfigTest.MocksConfig.class})
class PurchaseReturnConfigTest {

    @Autowired
    private PurchaseReturnRepository purchaseReturnRepository;

    @Test
    void wiresPurchaseReturnRepository() {
        assertThat(purchaseReturnRepository).isNotNull();
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
    }
}
