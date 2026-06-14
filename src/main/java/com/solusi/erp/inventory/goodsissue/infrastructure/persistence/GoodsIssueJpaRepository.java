package com.solusi.erp.inventory.goodsissue.infrastructure.persistence;

import com.solusi.erp.inventory.goodsissue.domain.model.GoodsIssueReferenceType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GoodsIssueJpaRepository extends JpaRepository<GoodsIssueEntity, Long> {

    @Query("SELECT DISTINCT g FROM GoodsIssueEntity g LEFT JOIN FETCH g.lines WHERE g.id = :id")
    Optional<GoodsIssueEntity> findByIdWithLines(@Param("id") Long id);

    @Query(value = "SELECT DISTINCT g FROM GoodsIssueEntity g LEFT JOIN FETCH g.lines WHERE " +
           "LOWER(g.code) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(g.referenceCode) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(g.note) LIKE LOWER(CONCAT('%', :keyword, '%'))",
           countQuery = "SELECT COUNT(g) FROM GoodsIssueEntity g WHERE " +
           "LOWER(g.code) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(g.referenceCode) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(g.note) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<GoodsIssueEntity> search(@Param("keyword") String keyword, Pageable pageable);

    @Query(value = "SELECT DISTINCT g FROM GoodsIssueEntity g LEFT JOIN FETCH g.lines WHERE " +
           "g.referenceType = :referenceType AND g.referenceId = :referenceId AND (" +
           "LOWER(g.code) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(g.referenceCode) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(g.note) LIKE LOWER(CONCAT('%', :keyword, '%'))) ",
           countQuery = "SELECT COUNT(g) FROM GoodsIssueEntity g WHERE " +
           "g.referenceType = :referenceType AND g.referenceId = :referenceId AND (" +
           "LOWER(g.code) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(g.referenceCode) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(g.note) LIKE LOWER(CONCAT('%', :keyword, '%'))) ")
    Page<GoodsIssueEntity> searchByReference(@Param("keyword") String keyword,
                                             @Param("referenceType") GoodsIssueReferenceType referenceType,
                                             @Param("referenceId") Long referenceId,
                                             Pageable pageable);

    @Query(value = "SELECT DISTINCT g FROM GoodsIssueEntity g LEFT JOIN FETCH g.lines",
           countQuery = "SELECT COUNT(g) FROM GoodsIssueEntity g")
    Page<GoodsIssueEntity> findAllWithLines(Pageable pageable);

    @Query(value = "SELECT DISTINCT g FROM GoodsIssueEntity g LEFT JOIN FETCH g.lines WHERE " +
           "g.referenceType = :referenceType AND g.referenceId = :referenceId",
           countQuery = "SELECT COUNT(g) FROM GoodsIssueEntity g WHERE " +
           "g.referenceType = :referenceType AND g.referenceId = :referenceId")
    Page<GoodsIssueEntity> findAllWithLinesByReference(@Param("referenceType") GoodsIssueReferenceType referenceType,
                                                       @Param("referenceId") Long referenceId,
                                                       Pageable pageable);

    boolean existsByCode(String code);

    @Query("SELECT COUNT(g) > 0 FROM GoodsIssueEntity g WHERE g.referenceType = ?1 AND g.referenceId = ?2")
    boolean existsByReference(GoodsIssueReferenceType referenceType, Long referenceId);
}
