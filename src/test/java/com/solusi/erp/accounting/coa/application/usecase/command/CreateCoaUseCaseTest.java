package com.solusi.erp.accounting.coa.application.usecase.command;

import com.solusi.erp.accounting.coa.domain.model.AccountType;
import com.solusi.erp.accounting.coa.domain.model.ChartOfAccount;
import com.solusi.erp.accounting.coa.domain.repository.CoaRepository;
import com.solusi.erp.core.exception.DomainException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CreateCoaUseCase Tests")
class CreateCoaUseCaseTest {

    @Mock private CoaRepository repository;
    private CreateCoaUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        useCase = new CreateCoaUseCaseImpl(repository);
    }

    @Test
    @DisplayName("execute saves and returns ChartOfAccount")
    void execute_savesAndReturnsCoa() {
        when(repository.existsByCode("1000")).thenReturn(false);
        when(repository.save(any(ChartOfAccount.class))).thenAnswer(i -> i.getArgument(0));

        ChartOfAccount result = useCase.execute("1000", "Cash", AccountType.ASSET,
                null, 1, false, "Note", true);

        assertThat(result.getCode()).isEqualTo("1000");
        assertThat(result.getName()).isEqualTo("Cash");
        verify(repository).save(any(ChartOfAccount.class));
    }

    @Test
    @DisplayName("execute throws DomainException when code already exists")
    void execute_throwsWhenDuplicateCode() {
        when(repository.existsByCode("1000")).thenReturn(true);

        assertThatThrownBy(() -> useCase.execute("1000", "Cash", AccountType.ASSET,
                null, 1, false, null, true))
                .isInstanceOf(DomainException.class);

        verify(repository, never()).save(any());
    }
}
