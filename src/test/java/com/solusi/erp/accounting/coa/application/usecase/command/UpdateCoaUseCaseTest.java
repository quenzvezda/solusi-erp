package com.solusi.erp.accounting.coa.application.usecase.command;

import com.solusi.erp.accounting.coa.domain.model.AccountType;
import com.solusi.erp.accounting.coa.domain.model.ChartOfAccount;
import com.solusi.erp.accounting.coa.domain.model.NormalBalance;
import com.solusi.erp.accounting.coa.domain.repository.CoaRepository;
import com.solusi.erp.core.domain.model.AuditMetadata;
import com.solusi.erp.core.exception.DomainException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UpdateCoaUseCase Tests")
class UpdateCoaUseCaseTest {

    @Mock private CoaRepository repository;
    private UpdateCoaUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        useCase = new UpdateCoaUseCaseImpl(repository);
    }

    @Test
    @DisplayName("execute updates and returns COA")
    void execute_updatesAndReturnsCoa() {
        AuditMetadata meta = new AuditMetadata(1L, 1L, null, null, null, null);
        ChartOfAccount existing = new ChartOfAccount(meta, "1000", "Cash",
                AccountType.ASSET, NormalBalance.DEBIT, null, 1, false, null, true);
        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(repository.save(any(ChartOfAccount.class))).thenAnswer(i -> i.getArgument(0));

        ChartOfAccount result = useCase.execute(1L, "Updated Cash", AccountType.ASSET,
                null, 1, false, "Updated", true);

        assertThat(result.getName()).isEqualTo("Updated Cash");
        assertThat(result.getNote()).isEqualTo("Updated");
        verify(repository).save(any(ChartOfAccount.class));
    }

    @Test
    @DisplayName("execute throws DomainException when COA not found")
    void execute_throwsWhenNotFound() {
        when(repository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(999L, "Name", AccountType.ASSET,
                null, 1, false, null, true))
                .isInstanceOf(DomainException.class);

        verify(repository, never()).save(any());
    }
}
