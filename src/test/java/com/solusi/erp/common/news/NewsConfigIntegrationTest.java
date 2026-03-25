package com.solusi.erp.common.news;

import com.solusi.erp.common.news.application.usecase.command.CreateNewsUseCase;
import com.solusi.erp.common.news.application.usecase.command.CreateNewsUseCaseImpl;
import com.solusi.erp.common.news.application.usecase.query.FindPublishedNewsUseCase;
import com.solusi.erp.common.news.application.usecase.query.FindPublishedNewsUseCaseImpl;
import com.solusi.erp.common.news.domain.repository.NewsRepository;
import com.solusi.erp.common.news.infrastructure.config.NewsConfig;
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

/**
 * PROOF: Test ini membuktikan bahwa konfigurasi di NewsConfig 
 * berhasil mendaftarkan Bean COMMAND dan QUERY ke Spring Context secara SURGICAL.
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {NewsConfig.class, NewsConfigIntegrationTest.MocksConfig.class})
class NewsConfigIntegrationTest {

    @Autowired
    private CreateNewsUseCase createNewsUseCase;

    @Autowired
    private FindPublishedNewsUseCase findPublishedNewsUseCase;

    /**
     * Konfigurasi Mock khusus untuk Test Context ini saja.
     */
    @Configuration
    static class MocksConfig {
        @Bean
        public NewsRepository newsRepository() { 
            return mock(NewsRepository.class); 
        }

        @Bean
        public TransactionTemplate transactionTemplate() { 
            return mock(TransactionTemplate.class); 
        }
    }

    @Test
    void shouldRegisterCreateNewsUseCaseBean() {
        assertNotNull(createNewsUseCase, "Bean CreateNewsUseCase (COMMAND) harus terdaftar");
        
        // Membuktikan bahwa yang di-inject adalah Lambda Wrapper, bukan class impl langsung
        assertFalse(createNewsUseCase instanceof CreateNewsUseCaseImpl,
            "CreateNewsUseCase harus sudah dibungkus (Proxy/Lambda) oleh NewsConfig");
    }

    @Test
    void shouldRegisterFindPublishedNewsUseCaseBean() {
        assertNotNull(findPublishedNewsUseCase, "Bean FindPublishedNewsUseCase (QUERY) harus terdaftar");
        
        // Membuktikan bahwa Query juga dibungkus (untuk optimasi Read-Only)
        assertFalse(findPublishedNewsUseCase instanceof FindPublishedNewsUseCaseImpl,
            "FindPublishedNewsUseCase harus sudah dibungkus (Proxy/Lambda) oleh NewsConfig");
    }
}
