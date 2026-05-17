package com.solusi.erp.accountspayable.vendorpayment.domain.repository;

import com.solusi.erp.accountspayable.vendorpayment.domain.model.VendorPayment;
import com.solusi.erp.accountspayable.vendorpayment.domain.model.VendorPaymentStatus;
import com.solusi.erp.core.domain.model.Page;
import com.solusi.erp.core.domain.model.Pageable;

import java.util.Optional;

public interface VendorPaymentRepository {
    Page<VendorPayment> findAll(String keyword, Long vendorId, VendorPaymentStatus status, Pageable pageable);
    VendorPayment save(VendorPayment payment);
    Optional<VendorPayment> findById(Long id);
    void deleteById(Long id);
}
