package com.solusi.erp.common.news.frameworks.persistence;

import com.solusi.erp.common.news.entities.NewsStatus;
import com.solusi.erp.common.news.frameworks.persistence.NewsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface NewsJpaRepository extends JpaRepository<NewsEntity, Long> {
    List<NewsEntity> findAllByStatus(NewsStatus status);
}
