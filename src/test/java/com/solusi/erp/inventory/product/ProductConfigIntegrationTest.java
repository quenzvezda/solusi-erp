package com.solusi.erp.inventory.product;

import com.solusi.erp.core.service.SequenceGeneratorService;
import com.solusi.erp.inventory.product.application.usecase.command.*;
import com.solusi.erp.inventory.product.application.usecase.query.*;
import com.solusi.erp.inventory.product.domain.repository.ProductRepository;
import com.solusi.erp.inventory.product.infrastructure.config.ProductConfig;
import com.solusi.erp.inventory.product.infrastructure.persistence.JpaProductRepository;
import com.solusi.erp.inventory.product.infrastructure.persistence.ProductPersistenceMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.support.TransactionTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {ProductConfig.class, ProductConfigIntegrationTest.MocksConfig.class})
class ProductConfigIntegrationTest {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CreateProductUseCase createProductUseCase;

    @Autowired
    private FindProductsUseCase findProductsUseCase;

    @Configuration
    static class MocksConfig {
        @Bean
        public JpaProductRepository jpaProductRepository() { return mock(JpaProductRepository.class); }
        @Bean
        public ProductPersistenceMapper productPersistenceMapper() { return mock(ProductPersistenceMapper.class); }
        @Bean
        public SequenceGeneratorService sequenceGeneratorService() { return mock(SequenceGeneratorService.class); }
        @Bean
        public TransactionTemplate transactionTemplate() { return mock(TransactionTemplate.class); }
        @Bean
        public org.springframework.transaction.PlatformTransactionManager platformTransactionManager() { return mock(org.springframework.transaction.PlatformTransactionManager.class); }
    }

    @Test
    void shouldRegisterBeans() {
        assertNotNull(productRepository);
        assertNotNull(createProductUseCase);
        assertNotNull(findProductsUseCase);
    }

    @Test
    void createUseCaseShouldBeWrapped() {
        assertFalse(createProductUseCase instanceof CreateProductUseCaseImpl, 
            "CreateProductUseCase should be wrapped by TransactionTemplate in Config");
    }
}
