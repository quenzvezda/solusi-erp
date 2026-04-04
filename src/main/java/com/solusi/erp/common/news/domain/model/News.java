package com.solusi.erp.common.news.domain.model;

import com.solusi.erp.core.domain.model.AuditMetadata;
import com.solusi.erp.core.exception.DomainException;

import java.time.LocalDateTime;

/**
 * Aggregate Root: News.
 */
public class News {
    private final AuditMetadata metadata;
    private NewsContent content;
    private NewsStatus status;
    private LocalDateTime publishDate;
    private LocalDateTime expiryDate;
    private final String author;
    private String createdByName;
    private String updatedByName;

    public News(AuditMetadata metadata, NewsContent content, NewsStatus status, LocalDateTime publishDate, LocalDateTime expiryDate, String author) {
        this.metadata = metadata;
        this.content = content;
        this.status = status;
        this.publishDate = publishDate;
        this.expiryDate = expiryDate;
        this.author = author;
    }

    public static News createNew(String title, String content, String author) {
        return new News(
            AuditMetadata.empty(),
            new NewsContent(title, content),
            NewsStatus.DRAFT,
            null,
            null,
            author
        );
    }


    public void submitForApproval() {
        if (this.status != NewsStatus.DRAFT) {
            throw new DomainException("msg.error.news.submit.not-draft");
        }
        this.status = NewsStatus.PENDING_APPROVAL;
    }

    public void publish(LocalDateTime publishDate, LocalDateTime expiryDate) {
        if (this.status != NewsStatus.PENDING_APPROVAL) {
            throw new DomainException("msg.error.news.publish.not-pending");
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
    
    public Long getId() { return metadata.id(); }
    public NewsContent getContent() { return content; }
    public NewsStatus getStatus() { return status; }
    public LocalDateTime getPublishDate() { return publishDate; }
    public LocalDateTime getExpiryDate() { return expiryDate; }
    public String getAuthor() { return author; }
    public Long getVersion() { return metadata.version(); }
    public AuditMetadata getMetadata() { return metadata; }
    public String getCreatedByName() { return createdByName; }
    public String getUpdatedByName() { return updatedByName; }

    public void setCreatedByName(String createdByName) { this.createdByName = createdByName; }
    public void setUpdatedByName(String updatedByName) { this.updatedByName = updatedByName; }
}
