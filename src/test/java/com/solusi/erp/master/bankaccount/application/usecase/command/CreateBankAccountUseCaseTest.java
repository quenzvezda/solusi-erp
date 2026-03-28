package com.solusi.erp.master.bankaccount.application.usecase.command;

import com.solusi.erp.core.domain.model.AuditMetadata;
import com.solusi.erp.core.service.SequenceGeneratorService;
import com.solusi.erp.master.bankaccount.domain.model.BankAccount;
import com.solusi.erp.master.bankaccount.domain.repository.BankAccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("CreateBankAccountUseCase Tests")
class CreateBankAccountUseCaseTest {

    @Mock
    private BankAccountRepository repository;

    @Mock
    private SequenceGeneratorService sequenceGeneratorService;

    private CreateBankAccountUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        useCase = new CreateBankAccountUseCaseImpl(repository, sequenceGeneratorService);
    }

    @Test
    @DisplayName("execute generates code from sequence and saves bank account")
    void execute_generatesCodeAndSavesBankAccount() {
        when(sequenceGeneratorService.generate("BANK_ACCOUNT")).thenReturn("BA-001");
        when(repository.save(any(BankAccount.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BankAccount result = useCase.execute("Bank BCA", "Sudirman", "John Doe", "1234567890",
                "BANK", "Note", 1L, 2L, true);

        assertThat(result.getCode()).isEqualTo("BA-001");
        assertThat(result.getBankName()).isEqualTo("Bank BCA");
    }

    @Test
    @DisplayName("execute returns result from repository (with persisted id)")
    void execute_returnsResultFromRepository() {
        AuditMetadata metadata = new AuditMetadata(10L, 1L, null, null, null, null);
        BankAccount persisted = new BankAccount(metadata, "BA-001", "Bank BCA", "Sudirman",
                "John Doe", "1234567890", "BANK", "Note", 1L, "Jakarta", 2L, "PT ABC", true);

        when(sequenceGeneratorService.generate("BANK_ACCOUNT")).thenReturn("BA-001");
        when(repository.save(any(BankAccount.class))).thenReturn(persisted);

        BankAccount result = useCase.execute("Bank BCA", "Sudirman", "John Doe", "1234567890",
                "BANK", "Note", 1L, 2L, true);

        assertThat(result.getId()).isEqualTo(10L);
    }
}
