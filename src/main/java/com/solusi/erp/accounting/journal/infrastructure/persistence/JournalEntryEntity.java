package com.solusi.erp.accounting.journal.infrastructure.persistence;

import com.solusi.erp.core.model.BaseModel;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "acc_journal_entries")
@Getter
@Setter
public class JournalEntryEntity extends BaseModel {

    @Column(name = "event_type", nullable = false, length = 50)
    private String eventType;

    @Column(name = "source_type", nullable = false, length = 50)
    private String sourceType;

    @Column(name = "source_id", nullable = false)
    private Long sourceId;

    @Column(name = "source_code", length = 60)
    private String sourceCode;

    @Column(name = "posting_date", nullable = false)
    private LocalDate postingDate;

    @Column(length = 255)
    private String description;

    @Column(nullable = false, length = 20)
    private String status;

    @OneToMany(mappedBy = "journalEntry", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("lineNo ASC")
    private List<JournalLineEntity> lines = new ArrayList<>();
}
