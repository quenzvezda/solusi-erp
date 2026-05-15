package com.solusi.erp.accountspayable.vendorpayment.infrastructure.config;

import com.solusi.erp.accountspayable.vendorpayment.domain.port.PayableVendorBillQueryPort;
import com.solusi.erp.accountspayable.vendorpayment.domain.port.VendorBillPaymentUpdatePort;
import com.solusi.erp.accountspayable.vendorpayment.domain.repository.VendorPaymentRepository;
import com.solusi.erp.accountspayable.vendorpayment.infrastructure.persistence.VendorPaymentJpaRepository;
import com.solusi.erp.accountspayable.vendorpayment.infrastructure.persistence.VendorPaymentPersistenceMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {VendorPaymentConfig.class, VendorPaymentConfigTest.MocksConfig.class})
class VendorPaymentConfigTest {

    @Autowired
    private VendorPaymentRepository vendorPaymentRepository;

    @Autowired
    private PayableVendorBillQueryPort payableVendorBillQueryPort;

    @Autowired
    private VendorBillPaymentUpdatePort vendorBillPaymentUpdatePort;

    @Test
    void should_register_vendor_payment_beans() {
        assertThat(vendorPaymentRepository).isNotNull();
        assertThat(payableVendorBillQueryPort).isNotNull();
        assertThat(vendorBillPaymentUpdatePort).isNotNull();
    }

    @Configuration
    static class MocksConfig {

        @Bean
        VendorPaymentJpaRepository vendorPaymentJpaRepository() {
            return mock(VendorPaymentJpaRepository.class);
        }

        @Bean
        VendorPaymentPersistenceMapper vendorPaymentPersistenceMapper() {
            return mock(VendorPaymentPersistenceMapper.class);
        }

        @Bean
        NamedParameterJdbcTemplate namedParameterJdbcTemplate() {
            return mock(NamedParameterJdbcTemplate.class);
        }
    }
}
