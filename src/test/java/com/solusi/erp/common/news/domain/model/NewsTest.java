package com.solusi.erp.common.news.domain.model;

import com.solusi.erp.core.exception.DomainException;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Domain Unit Test for News Aggregate.
 */
class NewsTest {

    @Test
    void shouldCreateNewDraftNews() {
        News news = News.createNew("NEWS-TEST", "Important Update on ERP", "Content of the update...", "Admin", LocalDateTime.now().plusDays(1), null);
        
        assertEquals(NewsStatus.DRAFT, news.getStatus());
        assertEquals("Important Update on ERP", news.getTitle());
    }

    @Test
    void shouldThrowExceptionIfTitleIsTooShort() {
        DomainException ex = assertThrows(DomainException.class, () -> 
            News.createNew("NEWS-TEST", "Short", "Valid content", "Admin", LocalDateTime.now().plusDays(1), null)
        );
        assertEquals("msg.error.news.title.minlength", ex.getKey());
    }

    @Test
    void shouldBePublishedSuccessfully() {
        LocalDateTime publishDate = LocalDateTime.now().plusDays(1);
        News news = News.createNew("NEWS-TEST", "Valid Title for ERP", "Valid Content", "Admin", publishDate, null);
        LocalDateTime now = LocalDateTime.now();
        
        news.submitForApproval(); // Step 1: Submit
        news.publish(now.plusDays(7)); // Step 2: Publish
        
        assertEquals(NewsStatus.PUBLISHED, news.getStatus());
    }

    @Test
    void shouldThrowExceptionIfPublishedWithoutApproval() {
        News news = News.createNew("NEWS-TEST", "Valid Title for ERP", "Valid Content", "Admin", LocalDateTime.now().plusDays(1), null);
        
        DomainException ex = assertThrows(DomainException.class, () -> 
            news.publish(null)
        );
        assertEquals("msg.error.news.publish.not-pending", ex.getKey());
    }

    @Test
    void shouldThrowExceptionIfPublishedTwice() {
        News news = News.createNew("NEWS-TEST", "Valid Title for ERP", "Valid Content", "Admin", LocalDateTime.now().plusDays(1), null);
        news.submitForApproval();
        news.publish(null);
        
        assertThrows(DomainException.class, () -> 
            news.publish(null)
        );
    }

    @Test
    void shouldAllowUpdateWhenDraft() {
        News news = News.createNew("NEWS-TEST", "Old Title Logic", "Old Content", "Admin", LocalDateTime.now().plusDays(1), null);
        news.updateContent("New Title Logic", "New Content Logic", LocalDateTime.now().plusDays(2), null);
        
        assertEquals("New Title Logic", news.getTitle());
    }

    @Test
    void shouldThrowExceptionWhenUpdateNonDraft() {
        News news = News.createNew("NEWS-TEST", "Valid Title for ERP", "Valid Content", "Admin", LocalDateTime.now().plusDays(1), null);
        news.submitForApproval(); // Change to PENDING
        
        DomainException ex = assertThrows(DomainException.class, () -> 
            news.updateContent("New Title", "New Content", LocalDateTime.now().plusDays(2), null)
        );
        assertEquals("msg.error.news.update.not-draft", ex.getKey());
    }

    // ============ New Tests for Expiration Logic (SOL-43) ============

    @Test
    void shouldNotBeExpiredWhenExpiryDateIsNull() {
        LocalDateTime publishDate = LocalDateTime.now().plusDays(1);
        News news = News.createNew("NEWS-TEST", "Valid Title for ERP", "Valid Content", "Admin", publishDate, null);
        
        assertFalse(news.isExpired());
    }

    @Test
    void shouldNotBeExpiredWhenExpiryDateIsInFuture() {
        LocalDateTime publishDate = LocalDateTime.now().plusDays(1);
        LocalDateTime expiryDate = LocalDateTime.now().plusDays(7);
        News news = News.createNew("NEWS-TEST", "Valid Title for ERP", "Valid Content", "Admin", publishDate, expiryDate);
        
        assertFalse(news.isExpired());
    }

    @Test
    void shouldBeExpiredWhenExpiryDateIsInPast() {
        LocalDateTime publishDate = LocalDateTime.now().minusDays(10);
        LocalDateTime expiryDate = LocalDateTime.now().minusDays(1);
        News news = News.createNew("NEWS-TEST", "Valid Title for ERP", "Valid Content", "Admin", publishDate, expiryDate);
        
        assertTrue(news.isExpired());
    }

    @Test
    void shouldBeExpiredWhenExpiryDateEqualsCurrentTime() {
        LocalDateTime publishDate = LocalDateTime.now().minusDays(5);
        LocalDateTime expiryDate = LocalDateTime.now();
        News news = News.createNew("NEWS-TEST", "Valid Title for ERP", "Valid Content", "Admin", publishDate, expiryDate);
        LocalDateTime currentTime = LocalDateTime.now();
        
        assertTrue(news.isExpired(currentTime.plusSeconds(1)));
    }

    @Test
    void shouldNotBeExpiredWhenExpiryDateIsInFutureWithCustomTime() {
        LocalDateTime publishDate = LocalDateTime.now().minusDays(5);
        LocalDateTime expiryDate = LocalDateTime.now().plusDays(10);
        News news = News.createNew("NEWS-TEST", "Valid Title for ERP", "Valid Content", "Admin", publishDate, expiryDate);
        LocalDateTime checkTime = LocalDateTime.now().minusDays(3);
        
        assertFalse(news.isExpired(checkTime));
    }

    @Test
    void shouldBeExpiredWhenCurrentTimeIsAfterExpiry() {
        LocalDateTime publishDate = LocalDateTime.now().minusDays(10);
        LocalDateTime expiryDate = LocalDateTime.now().minusDays(2);
        News news = News.createNew("NEWS-TEST", "Valid Title for ERP", "Valid Content", "Admin", publishDate, expiryDate);
        LocalDateTime checkTime = LocalDateTime.now().plusDays(5);
        
        assertTrue(news.isExpired(checkTime));
    }
}
