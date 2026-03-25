package com.solusi.erp.common.news.adapters.dto;

import com.solusi.erp.common.news.entities.NewsStatus;
import com.solusi.erp.core.dto.BaseAuditResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO untuk News.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class NewsResponse extends BaseAuditResponse {
    private String title;
    private String content;
    private NewsStatus status;
    private String author;
    private LocalDateTime publishDate;
    private LocalDateTime expiryDate;
}
