package com.solusi.erp.accounting.coa.application.usecase.command;

import com.solusi.erp.accounting.coa.domain.model.AccountType;
import com.solusi.erp.accounting.coa.domain.model.ChartOfAccount;
import com.solusi.erp.accounting.coa.domain.model.NormalBalance;
import com.solusi.erp.accounting.coa.domain.port.CoaInUseChecker;
import com.solusi.erp.accounting.coa.domain.repository.CoaRepository;
import com.solusi.erp.core.domain.model.AuditMetadata;
import com.solusi.erp.core.domain.model.DeleteResult;
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
@DisplayName("DeleteCoaUseCase Tests")
class DeleteCoaUseCaseTest {

    @Mock private CoaRepository repository;
    @Mock private CoaInUseChecker inUseChecker;
    private DeleteCoaUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        useCase = new DeleteCoaUseCaseImpl(repository, inUseChecker);
    }

    @Test
    @DisplayName("execute hard-deletes when COA is not in use")
    void execute_hardDeletesWhenNotInUse() {
        AuditMetadata meta = new AuditMetadata(1L, 1L, null, null, null, null);
        ChartOfAccount coa = new ChartOfAccount(meta, "1000", "Cash",
                AccountType.ASSET, NormalBalance.DEBIT, null, 1, false, null, true);
        when(repository.findById(1L)).thenReturn(Optional.of(coa));
        when(inUseChecker.isInUse(1L)).thenReturn(false);

        DeleteResult result = useCase.execute(1L);

        assertThat(result).isEqualTo(DeleteResult.HARD_DELETED);
        verify(repository).delete(1L);
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("execute soft-deletes when COA is in use")
    void execute_softDeletesWhenInUse() {
        AuditMetadata meta = new AuditMetadata(1L, 1L, null, null, null, null);
        ChartOfAccount coa = new ChartOfAccount(meta, "1000", "Cash",
                AccountType.ASSET, NormalBalance.DEBIT, null, 1, false, null, true);
        when(repository.findById(1L)).thenReturn(Optional.of(coa));
        when(inUseChecker.isInUse(1L)).thenReturn(true);

        DeleteResult result = useCase.execute(1L);

        assertThat(result).isEqualTo(DeleteResult.SOFT_DELETED);
        verify(repository).save(any(ChartOfAccount.class));
        verify(repository, never()).delete(any());
    }

    @Test
    @DisplayName("execute throws DomainException when COA not found")
    void execute_throwsWhenNotFound() {
        when(repository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(999L))
                .isInstanceOf(DomainException.class);

        verify(repository, never()).delete(any());
        verify(repository, never()).save(any());
    }
}
