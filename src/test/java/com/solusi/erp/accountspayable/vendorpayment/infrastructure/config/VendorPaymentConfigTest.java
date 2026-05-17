package com.solusi.erp.accountspayable.vendorpayment.infrastructure.config;

import com.solusi.erp.accounting.journal.application.usecase.command.PostJournalForEventUseCase;
import com.solusi.erp.accountspayable.vendorpayment.application.usecase.command.*;
import com.solusi.erp.accountspayable.vendorpayment.application.usecase.query.*;
import com.solusi.erp.accountspayable.vendorpayment.domain.port.PayableVendorBillQueryPort;
import com.solusi.erp.accountspayable.vendorpayment.domain.port.VendorBillPaymentUpdatePort;
import com.solusi.erp.accountspayable.vendorpayment.domain.repository.VendorPaymentRepository;
import com.solusi.erp.accountspayable.vendorpayment.infrastructure.persistence.VendorPaymentJpaRepository;
import com.solusi.erp.accountspayable.vendorpayment.infrastructure.persistence.VendorPaymentPersistenceMapper;
import com.solusi.erp.core.infrastructure.sequence.SequenceGeneratorService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.PlatformTransactionManager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {VendorPaymentConfig.class, VendorPaymentConfigTest.MocksConfig.class})
class VendorPaymentConfigTest {

    @Autowired private VendorPaymentRepository vendorPaymentRepository;
    @Autowired private PayableVendorBillQueryPort payableVendorBillQueryPort;
    @Autowired private VendorBillPaymentUpdatePort vendorBillPaymentUpdatePort;
    @Autowired private CreateVendorPaymentUseCase createVendorPaymentUseCase;
    @Autowired private UpdateVendorPaymentUseCase updateVendorPaymentUseCase;
    @Autowired private ConfirmVendorPaymentUseCase confirmVendorPaymentUseCase;
    @Autowired private CancelVendorPaymentUseCase cancelVendorPaymentUseCase;
    @Autowired private DeleteVendorPaymentUseCase deleteVendorPaymentUseCase;
    @Autowired private GetVendorPaymentListUseCase getVendorPaymentListUseCase;
    @Autowired private GetVendorPaymentDetailUseCase getVendorPaymentDetailUseCase;
    @Autowired private GetPayableVendorBillsUseCase getPayableVendorBillsUseCase;

    @Test
    void should_register_all_vendor_payment_beans() {
        assertThat(vendorPaymentRepository).isNotNull();
        assertThat(payableVendorBillQueryPort).isNotNull();
        assertThat(vendorBillPaymentUpdatePort).isNotNull();
        assertThat(createVendorPaymentUseCase).isNotNull();
        assertThat(updateVendorPaymentUseCase).isNotNull();
        assertThat(confirmVendorPaymentUseCase).isNotNull();
        assertThat(cancelVendorPaymentUseCase).isNotNull();
        assertThat(deleteVendorPaymentUseCase).isNotNull();
        assertThat(getVendorPaymentListUseCase).isNotNull();
        assertThat(getVendorPaymentDetailUseCase).isNotNull();
        assertThat(getPayableVendorBillsUseCase).isNotNull();
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

        @Bean
        SequenceGeneratorService sequenceGeneratorService() {
            return mock(SequenceGeneratorService.class);
        }

        @Bean
        PostJournalForEventUseCase postJournalForEventUseCase() {
            return mock(PostJournalForEventUseCase.class);
        }

        @Bean
        PlatformTransactionManager platformTransactionManager() {
            return mock(PlatformTransactionManager.class);
        }
    }
}
