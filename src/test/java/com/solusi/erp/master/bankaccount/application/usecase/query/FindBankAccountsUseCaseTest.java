package com.solusi.erp.master.bankaccount.application.usecase.query;

import com.solusi.erp.core.domain.model.Page;
import com.solusi.erp.core.domain.model.Pageable;
import com.solusi.erp.master.bankaccount.domain.model.BankAccount;
import com.solusi.erp.master.bankaccount.domain.repository.BankAccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("FindBankAccountsUseCase Tests")
class FindBankAccountsUseCaseTest {

    @Mock
    private BankAccountRepository repository;

    private FindBankAccountsUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        useCase = new FindBankAccountsUseCaseImpl(repository);
    }

    @Test
    @DisplayName("execute delegates keyword and pageable to repository and returns its result")
    void execute_delegatesKeywordAndPageableToRepository() {
        Pageable pageable = Pageable.of(0, 20);
        BankAccount account = BankAccount.createNew("BA-001", "Bank BCA", "Sudirman",
                "John Doe", "1234567890", "BANK", "Note", 1L, 2L, true);
        Page<BankAccount> expectedPage = new Page<>(List.of(account), 0, 20, 1L);

        when(repository.findAll("bca", pageable)).thenReturn(expectedPage);

        Page<BankAccount> result = useCase.execute("bca", pageable);

        assertThat(result).isEqualTo(expectedPage);
    }
}
