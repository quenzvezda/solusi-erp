package com.solusi.erp.accounting.schema.infrastructure.persistence;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "acc_schema_lines")
@Getter
@Setter
public class AccountingSchemaLineEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schema_id", nullable = false)
    private AccountingSchema schema;

    @Column(name = "variable", nullable = false)
    private String variable;

    @Column(name = "account_id", nullable = false)
    private Long accountId;

    @Column(name = "position", nullable = false)
    private String position;
}