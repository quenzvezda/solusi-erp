package com.solusi.erp.common.news.domain.model;

import com.solusi.erp.core.exception.DomainException;

import java.time.LocalDateTime;

/**
 * Aggregate Root: News.
 */
public class News {
    private final Long id;
    private NewsContent content; // Value Object
    private NewsStatus status;
    private LocalDateTime publishDate;
    private LocalDateTime expiryDate;
    private String author;

    public News(Long id, NewsContent content, NewsStatus status, LocalDateTime publishDate, LocalDateTime expiryDate, String author) {
        this.id = id;
        this.content = content;
        this.status = status;
        this.publishDate = publishDate;
        this.expiryDate = expiryDate;
        this.author = author;
    }

    public static News createNew(String title, String content, String author) {
        return new News(
            null,
            new NewsContent(title, content),
            NewsStatus.DRAFT,
            null,
            null,
            author
        );
    }

    public void publish(LocalDateTime publishDate, LocalDateTime expiryDate) {
        if (this.status != NewsStatus.DRAFT) {
            throw new DomainException("msg.error.news.publish.not-draft");
        }
        
        if (publishDate == null) {
            publishDate = LocalDateTime.now();
        }
        
        if (expiryDate != null && expiryDate.isBefore(publishDate)) {
            throw new DomainException("msg.error.news.publish.invalid-expiry");
        }

        this.status = NewsStatus.PUBLISHED;
        this.publishDate = publishDate;
        this.expiryDate = expiryDate;
    }

    public void archive() {
        this.status = NewsStatus.ARCHIVED;
    }

    public void updateContent(String newTitle, String newContent) {
        // ATURAN BISNIS: Hanya boleh update jika masih DRAFT
        if (this.status != NewsStatus.DRAFT) {
            throw new DomainException("msg.error.news.update.not-draft");
        }
        this.content = new NewsContent(newTitle, newContent);
    }

    public String getTitle() { return content.title(); }
    public String getContentText() { return content.content(); }
    
    public Long getId() { return id; }
    public NewsContent getContent() { return content; }
    public NewsStatus getStatus() { return status; }
    public LocalDateTime getPublishDate() { return publishDate; }
    public LocalDateTime getExpiryDate() { return expiryDate; }
    public String getAuthor() { return author; }
}
