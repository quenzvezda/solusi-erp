package com.solusi.erp.accountspayable.vendorbill.infrastructure.persistence;

import com.solusi.erp.accountspayable.vendorbill.domain.model.VendorBillDocumentStatus;
import com.solusi.erp.accountspayable.vendorbill.domain.model.VendorBillSettlementStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface VendorBillJpaRepository extends JpaRepository<VendorBillEntity, Long> {

    @Query("""
            select vb from VendorBillEntity vb
            where (:keyword is null or :keyword = '' or lower(vb.code) like lower(concat('%', :keyword, '%'))
               or lower(vb.vendorInvoiceNumber) like lower(concat('%', :keyword, '%')))
              and (:vendorId is null or vb.vendorId = :vendorId)
              and (:documentStatus is null or vb.documentStatus = :documentStatus)
              and (:settlementStatus is null or vb.settlementStatus = :settlementStatus)
            """)
    Page<VendorBillEntity> findAllFiltered(@Param("keyword") String keyword,
                                           @Param("vendorId") Long vendorId,
                                            @Param("documentStatus") VendorBillDocumentStatus documentStatus,
                                            @Param("settlementStatus") VendorBillSettlementStatus settlementStatus,
                                            Pageable pageable);
}
