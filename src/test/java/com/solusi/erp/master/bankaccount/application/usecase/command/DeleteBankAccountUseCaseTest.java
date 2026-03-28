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

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("DeleteBankAccountUseCase Tests")
class DeleteBankAccountUseCaseTest {

    @Mock
    private BankAccountRepository repository;

    private DeleteBankAccountUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        useCase = new DeleteBankAccountUseCaseImpl(repository);
    }

    @Test
    @DisplayName("execute deletes an existing bank account by id")
    void execute_deletesExistingBankAccount() {
        AuditMetadata metadata = new AuditMetadata(1L, 1L, null, null, null, null);
        BankAccount existing = new BankAccount(metadata, "BA-001", "Bank BCA", "Sudirman",
                "John Doe", "1234567890", "BANK", "Note", 1L, "Jakarta", 2L, "PT ABC", true);

        when(repository.findById(1L)).thenReturn(Optional.of(existing));

        useCase.execute(1L);

        verify(repository).delete(1L);
    }

    @Test
    @DisplayName("execute throws DomainException when bank account is not found")
    void execute_throwsDomainException_whenBankAccountNotFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(DomainException.class, () -> useCase.execute(99L));
    }
}
