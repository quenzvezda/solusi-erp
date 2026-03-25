package com.solusi.erp.common.news.domain.service;

import com.solusi.erp.common.news.domain.model.News;
import com.solusi.erp.common.news.domain.repository.NewsRepository;
import com.solusi.erp.core.exception.DomainException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Domain Service Unit Test.
 */
@ExtendWith(MockitoExtension.class)
class NewsDomainServiceTest {

    @Mock
    private NewsRepository newsRepository;

    @InjectMocks
    private NewsDomainService newsDomainService;

    @Test
    void shouldThrowExceptionWhenTitleExists() {
        String existingTitle = "Duplicate Title Check";
        News mockNews = mock(News.class);
        
        when(newsRepository.findByTitle(existingTitle)).thenReturn(Optional.of(mockNews));
        
        DomainException ex = assertThrows(DomainException.class, () -> 
            newsDomainService.validateTitleUniqueness(existingTitle)
        );
        
        assertEquals("msg.error.news.title.duplicate", ex.getKey());
    }

    @Test
    void shouldSucceedWhenTitleIsUnique() {
        String uniqueTitle = "New Unique Title";
        
        when(newsRepository.findByTitle(uniqueTitle)).thenReturn(Optional.empty());
        
        assertDoesNotThrow(() -> 
            newsDomainService.validateTitleUniqueness(uniqueTitle)
        );
    }
}
