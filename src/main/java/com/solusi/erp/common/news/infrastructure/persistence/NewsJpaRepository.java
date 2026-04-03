package com.solusi.erp.common.news.infrastructure.persistence;

import com.solusi.erp.common.news.domain.model.NewsStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface NewsJpaRepository extends JpaRepository<NewsEntity, Long> {
    List<NewsEntity> findAllByStatus(NewsStatus status);
    Optional<NewsEntity> findByTitle(String title);
}
