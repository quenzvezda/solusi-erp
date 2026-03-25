package com.solusi.erp.common.news.frameworks.persistence;

import com.solusi.erp.common.news.entities.NewsStatus;
import com.solusi.erp.core.model.BaseModel;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

/**
 * JPA Entity for News. 
 * Tetap extend BaseModel untuk auditing & versioning.
 */
@Entity
@Table(name = "common_news")
@Getter
@Setter
public class NewsEntity extends BaseModel {

    @Column(nullable = false, length = 255)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private NewsStatus status;

    @Column(name = "publish_date")
    private LocalDateTime publishDate;

    @Column(name = "expiry_date")
    private LocalDateTime expiryDate;

    @Column(length = 100)
    private String author;
}
