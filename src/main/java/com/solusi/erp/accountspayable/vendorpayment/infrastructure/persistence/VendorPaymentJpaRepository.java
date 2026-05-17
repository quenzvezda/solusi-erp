package com.solusi.erp.accountspayable.vendorpayment.infrastructure.persistence;

import com.solusi.erp.accountspayable.vendorpayment.domain.model.VendorPaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface VendorPaymentJpaRepository extends JpaRepository<VendorPaymentEntity, Long> {

    @Query("""
            SELECT vp FROM VendorPaymentEntity vp
            WHERE (:keyword IS NULL OR vp.code LIKE CONCAT('%', :keyword, '%'))
            AND (:vendorId IS NULL OR vp.vendorId = :vendorId)
            AND (:status IS NULL OR vp.status = :status)
            ORDER BY vp.id DESC
            """)
    Page<VendorPaymentEntity> findAllFiltered(
            @Param("keyword") String keyword,
            @Param("vendorId") Long vendorId,
            @Param("status") VendorPaymentStatus status,
            Pageable pageable);
}
