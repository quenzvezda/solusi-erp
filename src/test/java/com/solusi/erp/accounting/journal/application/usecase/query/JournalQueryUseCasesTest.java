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
        
        Optional<JournalEntry> result = useCase.execute(1L);
        
        assertThat(result).isEmpty();
        verify(queryPort).getJournalEntryDetail(1L);
    }
}