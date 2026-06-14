package com.solusi.erp.inventory.goodsissue.infrastructure.persistence;

import com.solusi.erp.core.model.BaseModel;
import com.solusi.erp.inventory.goodsissue.domain.model.GoodsIssuePartyType;
import com.solusi.erp.inventory.goodsissue.domain.model.GoodsIssueReferenceType;
import com.solusi.erp.inventory.goodsissue.domain.model.GoodsIssueStatus;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "inv_goods_issues")
@Getter @Setter @NoArgsConstructor
public class GoodsIssueEntity extends BaseModel {

    @Column(nullable = false, unique = true, length = 30)
    private String code;

    @Column(name = "issue_date", nullable = false)
    private LocalDate issueDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "reference_type", nullable = false, length = 40)
    private GoodsIssueReferenceType referenceType;

    @Column(name = "reference_id")
    private Long referenceId;

    @Column(name = "reference_code", length = 60)
    private String referenceCode;

    @Column(name = "party_id")
    private Long partyId;

    @Enumerated(EnumType.STRING)
    @Column(name = "party_type", length = 40)
    private GoodsIssuePartyType partyType;

    @Column(name = "facility_id", nullable = false)
    private Long facilityId;

    @Column(name = "currency_id")
    private Long currencyId;

    @Column(name = "exchange_rate", precision = 19, scale = 6)
    private BigDecimal exchangeRate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private GoodsIssueStatus status;

    @Column(name = "cancelled_date")
    private LocalDate cancelledDate;

    @Column(name = "cancel_reason", columnDefinition = "TEXT")
    private String cancelReason;

    @Column(columnDefinition = "TEXT")
    private String note;

    @OneToMany(mappedBy = "header", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<GoodsIssueLineEntity> lines = new ArrayList<>();
}
