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
        News news = News.createNew("Important Update on ERP", "Content of the update...", "Admin");
        
        assertEquals(NewsStatus.DRAFT, news.getStatus());
        assertEquals("Important Update on ERP", news.getTitle());
    }

    @Test
    void shouldThrowExceptionIfTitleIsTooShort() {
        DomainException ex = assertThrows(DomainException.class, () -> 
            News.createNew("Short", "Valid content", "Admin")
        );
        assertEquals("msg.error.news.title.minlength", ex.getKey());
    }

    @Test
    void shouldBePublishedSuccessfully() {
        News news = News.createNew("Valid Title for ERP", "Valid Content", "Admin");
        LocalDateTime now = LocalDateTime.now();
        
        news.submitForApproval(); // Step 1: Submit
        news.publish(now, now.plusDays(7)); // Step 2: Publish
        
        assertEquals(NewsStatus.PUBLISHED, news.getStatus());
        assertEquals(now, news.getPublishDate());
    }

    @Test
    void shouldThrowExceptionIfPublishedWithoutApproval() {
        News news = News.createNew("Valid Title for ERP", "Valid Content", "Admin");
        
        DomainException ex = assertThrows(DomainException.class, () -> 
            news.publish(LocalDateTime.now(), null)
        );
        assertEquals("msg.error.news.publish.not-pending", ex.getKey());
    }

    @Test
    void shouldThrowExceptionIfPublishedTwice() {
        News news = News.createNew("Valid Title for ERP", "Valid Content", "Admin");
        news.submitForApproval();
        news.publish(LocalDateTime.now(), null);
        
        assertThrows(DomainException.class, () -> 
            news.publish(LocalDateTime.now(), null)
        );
    }

    @Test
    void shouldAllowUpdateWhenDraft() {
        News news = News.createNew("Old Title Logic", "Old Content", "Admin");
        news.updateContent("New Title Logic", "New Content Logic");
        
        assertEquals("New Title Logic", news.getTitle());
    }

    @Test
    void shouldThrowExceptionWhenUpdateNonDraft() {
        News news = News.createNew("Valid Title for ERP", "Valid Content", "Admin");
        news.submitForApproval(); // Change to PENDING
        
        DomainException ex = assertThrows(DomainException.class, () -> 
            news.updateContent("New Title", "New Content")
        );
        assertEquals("msg.error.news.update.not-draft", ex.getKey());
    }
}
