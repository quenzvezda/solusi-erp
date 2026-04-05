package com.solusi.erp.common.news.domain.model;

import com.solusi.erp.core.domain.model.AuditMetadata;
import com.solusi.erp.core.exception.DomainException;

import java.time.LocalDateTime;

/**
 * Aggregate Root: News.
 */
public class News {
    private final AuditMetadata metadata;
    private final String code;
    private NewsContent content;
    private NewsStatus status;
    private LocalDateTime publishDate;
    private LocalDateTime expiryDate;
    private final String author;

    public News(AuditMetadata metadata, String code, NewsContent content, NewsStatus status, LocalDateTime publishDate, LocalDateTime expiryDate, String author) {
        this.metadata = metadata;
        this.code = code;
        this.content = content;
        this.status = status;
        this.publishDate = publishDate;
        this.expiryDate = expiryDate;
        this.author = author;
    }

    public static News createNew(String code, String title, String content, String author, LocalDateTime publishDate, LocalDateTime expiryDate) {
        if (publishDate == null) {
            throw new DomainException("msg.error.news.publish-date.required");
        }
        return new News(
            AuditMetadata.empty(),
            code,
            new NewsContent(title, content),
            NewsStatus.DRAFT,
            publishDate,
            expiryDate,
            author
        );
    }


    public void submitForApproval() {
        if (this.status != NewsStatus.DRAFT) {
            throw new DomainException("msg.error.news.submit.not-draft");
        }
        if (this.publishDate == null) {
            throw new DomainException("msg.error.news.publish-date.required");
        }
        this.status = NewsStatus.PENDING_APPROVAL;
    }

    public void publish(LocalDateTime expiryDateOverride) {
        if (this.status != NewsStatus.PENDING_APPROVAL) {
            throw new DomainException("msg.error.news.publish.not-pending");
        }

        if (this.publishDate == null) {
            throw new DomainException("msg.error.news.publish-date.required");
        }

        LocalDateTime effectiveExpiry = expiryDateOverride != null ? expiryDateOverride : this.expiryDate;
        if (effectiveExpiry != null && effectiveExpiry.isBefore(this.publishDate)) {
            throw new DomainException("msg.error.news.publish.invalid-expiry");
        }

        this.status = NewsStatus.PUBLISHED;
        if (effectiveExpiry != null) {
            this.expiryDate = effectiveExpiry;
        }
    }

    public void archive() {
        this.status = NewsStatus.ARCHIVED;
    }

    public void updateContent(String newTitle, String newContent, LocalDateTime publishDate, LocalDateTime expiryDate) {
        if (this.status != NewsStatus.DRAFT) {
            throw new DomainException("msg.error.news.update.not-draft");
        }
        this.content = new NewsContent(newTitle, newContent);
        this.publishDate = publishDate;
        this.expiryDate = expiryDate;
    }

    public String getCode() { return code; }
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
}
