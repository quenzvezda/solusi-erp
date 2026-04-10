package com.solusi.erp.common.news.infrastructure.adapter;

import com.solusi.erp.common.news.domain.model.News;
import com.solusi.erp.common.news.domain.model.NewsStatus;
import com.solusi.erp.common.news.infrastructure.persistence.NewsEntity;
import com.solusi.erp.common.news.infrastructure.persistence.NewsJpaRepository;
import com.solusi.erp.common.news.infrastructure.persistence.NewsPersistenceMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * Unit Test for NewsRepositoryAdapter - Expiration Date Filtering (SOL-43).
 * Tests the repository layer filtering of published news excluding expired items.
 */
@ExtendWith(MockitoExtension.class)
class NewsRepositoryAdapterTest {

    @Mock
    private NewsJpaRepository jpaRepository;

    @Mock
    private NewsPersistenceMapper mapper;

    private NewsRepositoryAdapter repositoryAdapter;

    @BeforeEach
    void setUp() {
        repositoryAdapter = new NewsRepositoryAdapter(jpaRepository, mapper);
    }

    @Test
    void shouldReturnPublishedNewsWhenNoExpiryDateSet() {
        // Arrange: News with no expiry date should always be included
        LocalDateTime publishDate = LocalDateTime.now().minusDays(5);
        NewsEntity newsEntity = createNewsEntity(1L, "NEWS-001", "Important Update", NewsStatus.PUBLISHED, publishDate, null);
        News news = createNews(1L, "NEWS-001", "Important Update", NewsStatus.PUBLISHED, publishDate, null);

        when(jpaRepository.findAllByStatusPublishedAndNotExpired()).thenReturn(Arrays.asList(newsEntity));
        when(mapper.toDomain(newsEntity)).thenReturn(news);

        // Act
        List<News> result = repositoryAdapter.findPublishedNews();

        // Assert
        assertEquals(1, result.size());
        assertEquals("NEWS-001", result.get(0).getCode());
        assertNull(result.get(0).getExpiryDate());
    }

    @Test
    void shouldReturnPublishedNewsWhenExpiryDateIsInFuture() {
        // Arrange: News with future expiry date should be included
        LocalDateTime publishDate = LocalDateTime.now().minusDays(3);
        LocalDateTime expiryDate = LocalDateTime.now().plusDays(10);
        NewsEntity newsEntity = createNewsEntity(2L, "NEWS-002", "New Feature Release", NewsStatus.PUBLISHED, publishDate, expiryDate);
        News news = createNews(2L, "NEWS-002", "New Feature Release", NewsStatus.PUBLISHED, publishDate, expiryDate);

        when(jpaRepository.findAllByStatusPublishedAndNotExpired()).thenReturn(Arrays.asList(newsEntity));
        when(mapper.toDomain(newsEntity)).thenReturn(news);

        // Act
        List<News> result = repositoryAdapter.findPublishedNews();

        // Assert
        assertEquals(1, result.size());
        assertEquals("NEWS-002", result.get(0).getCode());
        assertTrue(result.get(0).getExpiryDate().isAfter(LocalDateTime.now()));
    }

    @Test
    void shouldExcludePublishedNewsWhenExpiryDateIsInPast() {
        // Arrange: News with past expiry date should NOT be returned from repository query
        // This test simulates that the JPQL query has already filtered out expired items
        when(jpaRepository.findAllByStatusPublishedAndNotExpired()).thenReturn(Arrays.asList());

        // Act
        List<News> result = repositoryAdapter.findPublishedNews();

        // Assert
        assertTrue(result.isEmpty(), "Expired news should be filtered out at database level");
    }

    @Test
    void shouldReturnMultiplePublishedNewsExcludingExpired() {
        // Arrange: Mix of valid and expired news (simulating database already filtered)
        LocalDateTime now = LocalDateTime.now();
        
        NewsEntity validNews1 = createNewsEntity(1L, "NEWS-001", "Current Update", NewsStatus.PUBLISHED, now.minusDays(5), null);
        NewsEntity validNews2 = createNewsEntity(2L, "NEWS-002", "Upcoming Event", NewsStatus.PUBLISHED, now.minusDays(3), now.plusDays(7));
        
        News news1 = createNews(1L, "NEWS-001", "Current Update", NewsStatus.PUBLISHED, now.minusDays(5), null);
        News news2 = createNews(2L, "NEWS-002", "Upcoming Event", NewsStatus.PUBLISHED, now.minusDays(3), now.plusDays(7));

        when(jpaRepository.findAllByStatusPublishedAndNotExpired()).thenReturn(Arrays.asList(validNews1, validNews2));
        when(mapper.toDomain(validNews1)).thenReturn(news1);
        when(mapper.toDomain(validNews2)).thenReturn(news2);

        // Act
        List<News> result = repositoryAdapter.findPublishedNews();

        // Assert
        assertEquals(2, result.size());
        assertEquals("NEWS-001", result.get(0).getCode());
        assertEquals("NEWS-002", result.get(1).getCode());
        
        // Verify both news items are not expired
        assertFalse(result.get(0).isExpired());
        assertFalse(result.get(1).isExpired());
    }

    @Test
    void shouldReturnEmptyListWhenNoPublishedNews() {
        // Arrange
        when(jpaRepository.findAllByStatusPublishedAndNotExpired()).thenReturn(Arrays.asList());

        // Act
        List<News> result = repositoryAdapter.findPublishedNews();

        // Assert
        assertTrue(result.isEmpty());
    }

    // ============ Helper Methods ============

    private NewsEntity createNewsEntity(Long id, String code, String title, NewsStatus status, LocalDateTime publishDate, LocalDateTime expiryDate) {
        NewsEntity entity = new NewsEntity();
        entity.setId(id);
        entity.setCode(code);
        entity.setTitle(title);
        entity.setContent("Test content for " + title);
        entity.setStatus(status);
        entity.setPublishDate(publishDate);
        entity.setExpiryDate(expiryDate);
        entity.setAuthor("System");
        return entity;
    }

    private News createNews(Long id, String code, String title, NewsStatus status, LocalDateTime publishDate, LocalDateTime expiryDate) {
        // Using reflection or through constructor if available
        // For now, we'll mock the domain object's essential properties
        return new News(
            new com.solusi.erp.core.domain.model.AuditMetadata(id, 0L, null, null, null, null),
            code,
            new com.solusi.erp.common.news.domain.model.NewsContent(title, "Test content for " + title),
            status,
            publishDate,
            expiryDate,
            "System"
        );
    }
}
