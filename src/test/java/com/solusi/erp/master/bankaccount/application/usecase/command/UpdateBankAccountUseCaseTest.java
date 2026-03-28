package com.solusi.erp.master.bankaccount.application.usecase.command;

import com.solusi.erp.core.domain.model.AuditMetadata;
import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.master.bankaccount.domain.model.BankAccount;
import com.solusi.erp.master.bankaccount.domain.repository.BankAccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("UpdateBankAccountUseCase Tests")
class UpdateBankAccountUseCaseTest {

    @Mock
    private BankAccountRepository repository;

    private UpdateBankAccountUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        useCase = new UpdateBankAccountUseCaseImpl(repository);
    }

    @Test
    @DisplayName("execute updates mutable fields of an existing bank account")
    void execute_updatesMutableFieldsOfExistingBankAccount() {
        AuditMetadata metadata = new AuditMetadata(1L, 1L, null, null, null, null);
        BankAccount existing = new BankAccount(metadata, "BA-001", "Old Bank", "Old Branch",
                "Old Name", "000", "CASH", "Old Note", 1L, "Jakarta", 2L, "PT ABC", true);

        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(repository.save(any(BankAccount.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BankAccount result = useCase.execute(1L, "New Bank", "New Branch", "New Name",
                "999", "BANK", "New Note", 3L, 4L, false);

        assertThat(result.getBankName()).isEqualTo("New Bank");
        assertThat(result.getBranch()).isEqualTo("New Branch");
        assertThat(result.getAccountName()).isEqualTo("New Name");
        assertThat(result.getAccountNo()).isEqualTo("999");
        assertThat(result.getCode()).isEqualTo("BA-001");
    }

    @Test
    @DisplayName("execute throws DomainException when bank account is not found")
    void execute_throwsDomainException_whenBankAccountNotFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(DomainException.class,
                () -> useCase.execute(99L, "Name", "Branch", "AccName", "000", "BANK", "Note", 1L, 2L, true));
    }
}
