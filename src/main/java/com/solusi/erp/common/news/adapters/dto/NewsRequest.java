package com.solusi.erp.common.news.adapters.dto;

import com.solusi.erp.common.news.entities.NewsStatus;
import com.solusi.erp.core.dto.BaseAuditResponse;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Request DTO untuk News.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class NewsRequest extends BaseAuditResponse {

    @NotBlank(message = "{label.news.title} {validation.notnull.suffix}")
    @Size(min = 10, message = "{label.news.title.minlength}")
    private String title;

    @NotBlank(message = "{label.news.content} {validation.notnull.suffix}")
    private String content;

    private String author;
    
    private NewsStatus status;
    private LocalDateTime publishDate;
    private LocalDateTime expiryDate;
}
