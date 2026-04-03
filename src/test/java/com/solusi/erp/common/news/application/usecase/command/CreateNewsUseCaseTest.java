package com.solusi.erp.common.news.application.usecase.command;

import com.solusi.erp.common.news.domain.model.News;
import com.solusi.erp.common.news.domain.repository.NewsRepository;
import com.solusi.erp.common.news.domain.service.NewsDomainService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * PROOF: Unit Test ini berjalan 100% tanpa Spring Context.
 */
@ExtendWith(MockitoExtension.class)
class CreateNewsUseCaseTest {

    @Mock
    private NewsRepository newsRepository;

    @Mock
    private NewsDomainService domainService;

    @InjectMocks
    private CreateNewsUseCaseImpl createNewsUseCase;

    @Test
    void shouldExecuteCreateNewsFlowWithoutSpring() {
        // Given
        String title = "Pure Logic Proof Title";
        String content = "Full content of the news...";
        String author = "PureAuthor";
        
        doNothing().when(domainService).validateTitleUniqueness(title);
        when(newsRepository.save(any(News.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        News result = createNewsUseCase.execute(title, content, author);

        // Then
        assertNotNull(result);
        assertEquals(title, result.getTitle());
        verify(domainService).validateTitleUniqueness(title);
        verify(newsRepository).save(any());
    }
}
