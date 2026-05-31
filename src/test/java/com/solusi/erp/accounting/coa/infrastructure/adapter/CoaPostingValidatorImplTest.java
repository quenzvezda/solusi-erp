package com.solusi.erp.accounting.coa.infrastructure.adapter;

import com.solusi.erp.accounting.coa.infrastructure.persistence.ChartOfAccount;
import com.solusi.erp.accounting.coa.infrastructure.persistence.CoaJpaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CoaPostingValidatorImplTest {

    @Mock
    private CoaJpaRepository coaJpaRepository;

    @InjectMocks
    private CoaPostingValidatorImpl validator;

    @Test
    void isPostable_returnsTrueForActiveLeafAccount() {
        when(coaJpaRepository.findById(1L)).thenReturn(Optional.of(coa(true, false)));

        assertThat(validator.isPostable(1L)).isTrue();
    }

    @Test
    void isPostable_returnsFalseForInactiveAccount() {
        when(coaJpaRepository.findById(1L)).thenReturn(Optional.of(coa(false, false)));

        assertThat(validator.isPostable(1L)).isFalse();
    }

    @Test
    void isPostable_returnsFalseForHeaderAccount() {
        when(coaJpaRepository.findById(1L)).thenReturn(Optional.of(coa(true, true)));

        assertThat(validator.isPostable(1L)).isFalse();
    }

    @Test
    void isPostable_returnsFalseForMissingAccount() {
        when(coaJpaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThat(validator.isPostable(99L)).isFalse();
    }

    @Test
    void isPostable_returnsFalseForNullId() {
        assertThat(validator.isPostable(null)).isFalse();

        verifyNoInteractions(coaJpaRepository);
    }

    private static ChartOfAccount coa(boolean active, boolean header) {
        ChartOfAccount account = new ChartOfAccount();
        account.setIsActive(active);
        account.setIsHeader(header);
        return account;
    }
}
