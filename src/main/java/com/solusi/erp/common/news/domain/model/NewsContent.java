package com.solusi.erp.common.news.domain.model;

/**
 * Value Object representing the content of a News item.
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
}
