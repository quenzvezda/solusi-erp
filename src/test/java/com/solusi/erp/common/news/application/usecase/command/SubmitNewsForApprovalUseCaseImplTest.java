package com.solusi.erp.common.news.application.usecase.command;

import com.solusi.erp.common.news.application.port.NewsEventPublisher;
import com.solusi.erp.common.news.domain.model.News;
import com.solusi.erp.common.news.domain.model.NewsContent;
import com.solusi.erp.common.news.domain.model.NewsStatus;
import com.solusi.erp.common.news.domain.repository.NewsRepository;
import com.solusi.erp.core.domain.model.AuditMetadata;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SubmitNewsForApprovalUseCaseImplTest {

    @Mock
    private NewsRepository newsRepository;

    @Mock
    private NewsEventPublisher eventPublisher;

    private SubmitNewsForApprovalUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        useCase = new SubmitNewsForApprovalUseCaseImpl(newsRepository, eventPublisher);
    }

    @Test
    void shouldSubmitDraftNewsForApprovalWithoutPersistingToDatabase() {
        News draftNews = newsWithStatus(11L, NewsStatus.DRAFT);

        when(newsRepository.findById(11L)).thenReturn(Optional.of(draftNews));
        when(newsRepository.save(any(News.class))).thenAnswer(invocation -> invocation.getArgument(0));

        News result = useCase.execute(11L, "requester");

        assertNotNull(result);
        assertEquals(11L, result.getId());
        assertEquals(NewsStatus.PENDING_APPROVAL, result.getStatus());

        ArgumentCaptor<News> captor = ArgumentCaptor.forClass(News.class);
        verify(newsRepository).save(captor.capture());
        assertEquals(NewsStatus.PENDING_APPROVAL, captor.getValue().getStatus());
        verify(eventPublisher).publishApprovalRequested(11L, "requester");
    }

    private static News newsWithStatus(Long id, NewsStatus status) {
        return new News(
            new AuditMetadata(id, 1L, null, null, null, null),
            new NewsContent("Draft News Title", "Draft news content"),
            status,
            null,
            null,
            "Author"
        );
    }
}
