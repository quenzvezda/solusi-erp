package com.solusi.erp.common.news.web.dto;

import com.solusi.erp.common.news.domain.model.NewsStatus;
import com.solusi.erp.core.dto.BaseAuditResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Detail Response DTO untuk News (Web Layer).
 * Menggunakan Intent-Based Naming: "Detail" mencerminkan view lengkap objek News.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class NewsDetailResponse extends BaseAuditResponse {
    private String title;
    private String content;
    private NewsStatus status;
    private String author;
    private LocalDateTime publishDate;
    private LocalDateTime expiryDate;
}
