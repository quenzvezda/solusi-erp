package com.solusi.erp.accounting.journal.web.controller;

import com.solusi.erp.accounting.journal.application.usecase.query.FindJournalEntriesUseCase;
import com.solusi.erp.accounting.journal.application.usecase.query.GetJournalEntryDetailUseCase;
import com.solusi.erp.accounting.journal.domain.model.JournalEntry;
import com.solusi.erp.accounting.journal.domain.model.JournalLine;
import com.solusi.erp.accounting.journal.domain.model.JournalStatus;
import com.solusi.erp.accounting.schema.domain.model.SchemaEventType;
import com.solusi.erp.core.domain.model.AuditMetadata;
import com.solusi.erp.core.domain.model.Page;
import com.solusi.erp.core.domain.model.Pageable;
import com.solusi.erp.accounting.journal.web.mapper.JournalEntryWebMapper;
import com.solusi.erp.accounting.journal.web.dto.JournalEntryDetailResponse;
import com.solusi.erp.accounting.journal.web.dto.JournalEntryListResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.ui.ExtendedModelMap;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JournalEntryControllerTest {

    @Mock private FindJournalEntriesUseCase findUseCase;
    @Mock private GetJournalEntryDetailUseCase getDetailUseCase;
    @Mock private JournalEntryWebMapper webMapper;

    private JournalEntryController controller;
    private JournalEntry sampleEntry;

    @BeforeEach
    void setUp() {
        controller = new JournalEntryController(findUseCase, getDetailUseCase, webMapper);
        sampleEntry = new JournalEntry(
                new AuditMetadata(1L, 1L, null, null, null, null),
                SchemaEventType.GOODS_RECEIPT, "GOODS_RECEIPT", 101L, "GR-001",
                LocalDate.now(), "Test", JournalStatus.POSTED,
                List.of(JournalLine.debit(1L, BigDecimal.TEN), JournalLine.credit(2L, BigDecimal.TEN))
        );
    }

    @Test
    void list_returnsCorrectViewAndModel() {
        when(findUseCase.execute(any(), any(Pageable.class))).thenReturn(new Page<>(List.of(sampleEntry), 1, 10, 1L));
        when(webMapper.toListResponse(any())).thenReturn(new JournalEntryListResponse());
        
        ExtendedModelMap model = new ExtendedModelMap();
        String view = controller.list(null, null, null, null, null, PageRequest.of(0, 10), model);
        
        assertThat(view).isEqualTo("accounting/journal/journal-entry-list");
        assertThat(model.get("page")).isNotNull();
        assertThat(model.get("filter")).isNotNull();
        assertThat(model.get("eventTypes")).isEqualTo(SchemaEventType.values());
    }

    @Test
    void detail_returnsCorrectViewAndModel() {
        when(getDetailUseCase.execute(1L)).thenReturn(Optional.of(sampleEntry));
        when(webMapper.toDetailResponse(any())).thenReturn(new JournalEntryDetailResponse());
        
        ExtendedModelMap model = new ExtendedModelMap();
        String view = controller.detail(1L, model);
        
        assertThat(view).isEqualTo("accounting/journal/journal-entry-detail");
        assertThat(model.get("journal")).isNotNull();
    }
}