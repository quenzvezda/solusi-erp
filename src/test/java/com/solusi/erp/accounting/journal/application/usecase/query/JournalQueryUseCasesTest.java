package com.solusi.erp.accounting.journal.application.usecase.query;

import com.solusi.erp.accounting.journal.domain.model.JournalEntry;
import com.solusi.erp.accounting.journal.domain.model.JournalEntryFilter;
import com.solusi.erp.accounting.journal.domain.port.JournalEntryQueryPort;
import com.solusi.erp.core.domain.model.Page;
import com.solusi.erp.core.domain.model.Pageable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.solusi.erp.accounting.journal.domain.model.JournalStatus;
import com.solusi.erp.accounting.journal.domain.model.JournalLine;
import com.solusi.erp.core.domain.model.AuditMetadata;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JournalQueryUseCasesTest {
    @Mock private JournalEntryQueryPort queryPort;

    @Test
    void findJournalEntries_delegatesToPort() {
        FindJournalEntriesUseCase useCase = new FindJournalEntriesUseCaseImpl(queryPort);
        JournalEntryFilter filter = new JournalEntryFilter(null, null, null, null, null);
        Pageable pageable = new Pageable(1, 10, null, null);
        when(queryPort.findJournalEntries(filter, pageable)).thenReturn(new Page<>(List.of(), 1, 10, 0L));
        
        Page<JournalEntry> result = useCase.execute(filter, pageable);
        
        assertThat(result).isNotNull();
        verify(queryPort).findJournalEntries(filter, pageable);
    }

    @Test
    void getDetail_delegatesToPort() {
        GetJournalEntryDetailUseCase useCase = new GetJournalEntryDetailUseCaseImpl(queryPort);
        when(queryPort.getJournalEntryDetail(1L)).thenReturn(Optional.empty());
        
        Optional<JournalEntryDetailView> result = useCase.execute(1L);
        
        assertThat(result).isEmpty();
        verify(queryPort).getJournalEntryDetail(1L);
    }

    @Test
    void getDetail_forOriginalIncludesReversedById() {
        GetJournalEntryDetailUseCase useCase = new GetJournalEntryDetailUseCaseImpl(queryPort);
        JournalEntry original = manualPosted(1L, null);
        JournalEntry reversal = manualPosted(2L, 1L);
        when(queryPort.getJournalEntryDetail(1L)).thenReturn(Optional.of(original));
        when(queryPort.findReversalOf(1L)).thenReturn(Optional.of(reversal));

        Optional<JournalEntryDetailView> result = useCase.execute(1L);

        assertThat(result).isPresent();
        assertThat(result.get().entry()).isEqualTo(original);
        assertThat(result.get().reversedById()).isEqualTo(2L);
    }

    @Test
    void getDetail_forReversalKeepsReversalOfAndNoReversedBy() {
        GetJournalEntryDetailUseCase useCase = new GetJournalEntryDetailUseCaseImpl(queryPort);
        JournalEntry reversal = manualPosted(2L, 1L);
        when(queryPort.getJournalEntryDetail(2L)).thenReturn(Optional.of(reversal));
        when(queryPort.findReversalOf(2L)).thenReturn(Optional.empty());

        Optional<JournalEntryDetailView> result = useCase.execute(2L);

        assertThat(result).isPresent();
        assertThat(result.get().entry().getReversalOfId()).isEqualTo(1L);
        assertThat(result.get().reversedById()).isNull();
    }

    private static JournalEntry manualPosted(Long id, Long reversalOfId) {
        return new JournalEntry(
                new AuditMetadata(id, 1L, null, null, null, null),
                "MANUAL",
                "MANUAL",
                null,
                null,
                1L,
                BigDecimal.ONE,
                null,
                reversalOfId,
                LocalDate.of(2026, 5, 31),
                "Manual",
                JournalStatus.POSTED,
                List.of(
                        JournalLine.debit(101L, BigDecimal.TEN),
                        JournalLine.credit(201L, BigDecimal.TEN)
                )
        );
    }
}
