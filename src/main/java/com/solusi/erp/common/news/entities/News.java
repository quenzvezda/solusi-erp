package com.solusi.erp.common.news.entities;

import java.time.LocalDateTime;

/**
 * Aggregate Root: News.
 * Now using Value Objects to keep the code clean even as logic grows.
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
        // Validation is now inside the NewsContent constructor
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
            throw new com.solusi.erp.core.exception.DomainException("msg.error.news.publish.not-draft");
        }
        
        if (publishDate == null) {
            publishDate = LocalDateTime.now();
        }
        
        if (expiryDate != null && expiryDate.isBefore(publishDate)) {
            throw new com.solusi.erp.core.exception.DomainException("msg.error.news.publish.invalid-expiry");
        }

        this.status = NewsStatus.PUBLISHED;
        this.publishDate = publishDate;
        this.expiryDate = expiryDate;
    }

    public void archive() {
        this.status = NewsStatus.ARCHIVED;
    }

    public void updateContent(String newTitle, String newContent) {
        if (this.status == NewsStatus.ARCHIVED) {
            throw new com.solusi.erp.core.exception.DomainException("msg.error.news.update.archived");
        }
        
        // CEO approach: NewsContent object validates itself. 
        // News just accepts the result if it's not ARCHIVED.
        this.content = new NewsContent(newTitle, newContent);
    }

    // Delegation to Value Object
    public String getTitle() { return content.getTitle(); }
    public String getContentText() { return content.getContent(); }
    
    // Original Getters
    public Long getId() { return id; }
    public NewsContent getContent() { return content; }
    public NewsStatus getStatus() { return status; }
    public LocalDateTime getPublishDate() { return publishDate; }
    public LocalDateTime getExpiryDate() { return expiryDate; }
    public String getAuthor() { return author; }
}
