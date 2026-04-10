package com.solusi.erp.common.news.infrastructure.persistence;

import com.solusi.erp.common.news.domain.model.NewsStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface NewsJpaRepository extends JpaRepository<NewsEntity, Long> {
    List<NewsEntity> findAllByStatus(NewsStatus status);
    Optional<NewsEntity> findByTitle(String title);

    @Query("SELECT n FROM NewsEntity n WHERE n.status = com.solusi.erp.common.news.domain.model.NewsStatus.PUBLISHED AND (n.expiryDate IS NULL OR n.expiryDate > CURRENT_TIMESTAMP)")
    List<NewsEntity> findAllByStatusPublishedAndNotExpired();

    @Query("SELECT n FROM NewsEntity n WHERE (:keyword IS NULL OR LOWER(n.title) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<NewsEntity> findAllByKeyword(@Param("keyword") String keyword, Pageable pageable);
}
