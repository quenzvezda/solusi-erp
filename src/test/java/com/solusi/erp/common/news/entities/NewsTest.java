package com.solusi.erp.common.news.entities;

import com.solusi.erp.core.exception.DomainException;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class NewsTest {

    @Test
    void shouldCreateNewDraftNews() {
        News news = News.createNew("Important Update on ERP", "Content of the update...", "Admin");
        
        assertEquals(NewsStatus.DRAFT, news.getStatus());
        assertEquals("Important Update on ERP", news.getTitle());
    }

    @Test
    void shouldThrowExceptionIfTitleIsTooShort() {
        // Updated to expect DomainException and verify the i18n key
        DomainException ex = assertThrows(DomainException.class, () -> 
            News.createNew("Short", "Valid content", "Admin")
        );
        assertEquals("msg.error.news.title.minlength", ex.getKey());
    }

    @Test
    void shouldBePublishedSuccessfully() {
        News news = News.createNew("Valid Title for ERP", "Valid Content", "Admin");
        LocalDateTime now = LocalDateTime.now();
        
        news.publish(now, now.plusDays(7));
        
        assertEquals(NewsStatus.PUBLISHED, news.getStatus());
        assertEquals(now, news.getPublishDate());
    }

    @Test
    void shouldThrowExceptionIfPublishedTwice() {
        News news = News.createNew("Valid Title for ERP", "Valid Content", "Admin");
        news.publish(LocalDateTime.now(), null);
        
        // Updated to expect DomainException
        assertThrows(DomainException.class, () -> 
            news.publish(LocalDateTime.now(), null)
        );
    }

    @Test
    void shouldNotAllowEditWhenArchived() {
        News news = News.createNew("Valid Title for ERP", "Valid Content", "Admin");
        news.archive();
        
        // Updated to expect DomainException and verify key
        DomainException ex = assertThrows(DomainException.class, () -> 
            news.updateContent("New Title", "New Content")
        );
        assertEquals("msg.error.news.update.archived", ex.getKey());
    }
}
