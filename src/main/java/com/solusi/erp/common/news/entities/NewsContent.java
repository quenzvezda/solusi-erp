package com.solusi.erp.common.news.entities;

/**
 * Value Object representing the content of a News item.
 * Using a Record for immutability and concise definition.
 */
public record NewsContent(String title, String content) {
    
    public NewsContent {
        if (title == null || title.trim().length() < 10) {
            throw new com.solusi.erp.core.exception.DomainException("msg.error.news.title.minlength");
        }
        if (content == null || content.trim().isEmpty()) {
            throw new com.solusi.erp.core.exception.DomainException("msg.error.news.content.empty");
        }
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }
}
