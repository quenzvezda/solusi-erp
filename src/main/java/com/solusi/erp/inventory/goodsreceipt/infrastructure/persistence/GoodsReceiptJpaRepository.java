package com.solusi.erp.inventory.goodsreceipt.infrastructure.persistence;

import com.solusi.erp.inventory.goodsreceipt.domain.model.GoodsReceiptReferenceType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GoodsReceiptJpaRepository extends JpaRepository<GoodsReceiptEntity, Long> {

    @Query("SELECT DISTINCT g FROM GoodsReceiptEntity g LEFT JOIN FETCH g.lines WHERE g.id = :id")
    Optional<GoodsReceiptEntity> findByIdWithLines(@Param("id") Long id);

    @Query(value = "SELECT DISTINCT g FROM GoodsReceiptEntity g LEFT JOIN FETCH g.lines WHERE " +
           "LOWER(g.code) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(g.note) LIKE LOWER(CONCAT('%', :keyword, '%'))",
           countQuery = "SELECT COUNT(g) FROM GoodsReceiptEntity g WHERE " +
           "LOWER(g.code) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(g.note) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<GoodsReceiptEntity> search(@Param("keyword") String keyword, Pageable pageable);

    @Query(value = "SELECT DISTINCT g FROM GoodsReceiptEntity g LEFT JOIN FETCH g.lines WHERE " +
           "g.referenceType = :referenceType AND g.referenceId = :referenceId AND (" +
           "LOWER(g.code) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(g.note) LIKE LOWER(CONCAT('%', :keyword, '%'))) ",
           countQuery = "SELECT COUNT(g) FROM GoodsReceiptEntity g WHERE " +
           "g.referenceType = :referenceType AND g.referenceId = :referenceId AND (" +
           "LOWER(g.code) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(g.note) LIKE LOWER(CONCAT('%', :keyword, '%'))) ")
    Page<GoodsReceiptEntity> searchByReference(@Param("keyword") String keyword,
                                               @Param("referenceType") GoodsReceiptReferenceType referenceType,
                                               @Param("referenceId") Long referenceId,
                                               Pageable pageable);

    @Query(value = "SELECT DISTINCT g FROM GoodsReceiptEntity g LEFT JOIN FETCH g.lines",
           countQuery = "SELECT COUNT(g) FROM GoodsReceiptEntity g")
    Page<GoodsReceiptEntity> findAllWithLines(Pageable pageable);

    @Query(value = "SELECT DISTINCT g FROM GoodsReceiptEntity g LEFT JOIN FETCH g.lines WHERE " +
           "g.referenceType = :referenceType AND g.referenceId = :referenceId",
           countQuery = "SELECT COUNT(g) FROM GoodsReceiptEntity g WHERE " +
           "g.referenceType = :referenceType AND g.referenceId = :referenceId")
    Page<GoodsReceiptEntity> findAllWithLinesByReference(@Param("referenceType") GoodsReceiptReferenceType referenceType,
                                                         @Param("referenceId") Long referenceId,
                                                         Pageable pageable);

    @Query("SELECT COUNT(g) FROM GoodsReceiptEntity g WHERE g.referenceType = ?1 AND g.referenceId = ?2")
    Long countByReference(GoodsReceiptReferenceType referenceType, Long referenceId);
}
