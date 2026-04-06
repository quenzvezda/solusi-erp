package com.solusi.erp.common.news.application.usecase.command;

import com.solusi.erp.common.news.domain.model.News;
import com.solusi.erp.common.news.domain.repository.NewsRepository;
import com.solusi.erp.common.news.domain.service.NewsDomainService;
import com.solusi.erp.core.infrastructure.sequence.SequenceGeneratorService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

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

    @Mock
    private SequenceGeneratorService sequenceGeneratorService;

    @InjectMocks
    private CreateNewsUseCaseImpl createNewsUseCase;

    @Test
    void shouldExecuteCreateNewsFlowWithoutSpring() {
        // Given
        String title = "Pure Logic Proof Title";
        String content = "Full content of the news...";
        String author = "PureAuthor";
        
        doNothing().when(domainService).validateTitleUniqueness(title);
        when(sequenceGeneratorService.generate("NEWS")).thenReturn("NEWS-0001");
        when(newsRepository.save(any(News.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        News result = createNewsUseCase.execute(title, content, author, LocalDateTime.now().plusDays(1), null);

        // Then
        assertNotNull(result);
        assertEquals(title, result.getTitle());
        assertEquals("NEWS-0001", result.getCode());
        verify(domainService).validateTitleUniqueness(title);
        verify(newsRepository).save(any());
    }
}
