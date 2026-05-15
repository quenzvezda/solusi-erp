package com.solusi.erp.accountspayable.vendorpayment.infrastructure.config;

import com.solusi.erp.accountspayable.vendorpayment.domain.port.PayableVendorBillQueryPort;
import com.solusi.erp.accountspayable.vendorpayment.domain.port.VendorBillPaymentUpdatePort;
import com.solusi.erp.accountspayable.vendorpayment.domain.repository.VendorPaymentRepository;
import com.solusi.erp.accountspayable.vendorpayment.infrastructure.adapter.PayableVendorBillQueryAdapter;
import com.solusi.erp.accountspayable.vendorpayment.infrastructure.adapter.VendorBillPaymentUpdateAdapter;
import com.solusi.erp.accountspayable.vendorpayment.infrastructure.adapter.VendorPaymentRepositoryImpl;
import com.solusi.erp.accountspayable.vendorpayment.infrastructure.persistence.VendorPaymentJpaRepository;
import com.solusi.erp.accountspayable.vendorpayment.infrastructure.persistence.VendorPaymentPersistenceMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

@Configuration
public class VendorPaymentConfig {

    @Bean
    public VendorPaymentRepository vendorPaymentRepository(VendorPaymentJpaRepository jpaRepository,
                                                            VendorPaymentPersistenceMapper mapper) {
        return new VendorPaymentRepositoryImpl(jpaRepository, mapper);
    }

    @Bean
    public PayableVendorBillQueryPort payableVendorBillQueryPort(NamedParameterJdbcTemplate jdbcTemplate) {
        return new PayableVendorBillQueryAdapter(jdbcTemplate);
    }

    @Bean
    public VendorBillPaymentUpdatePort vendorBillPaymentUpdatePort(NamedParameterJdbcTemplate jdbcTemplate) {
        return new VendorBillPaymentUpdateAdapter(jdbcTemplate);
    }
}
