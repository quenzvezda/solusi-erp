package com.solusi.erp.accountspayable.vendorbill.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Entity
@Table(name = "ap_vendor_bill_gr_refs")
@IdClass(VendorBillGrRefEntity.Pk.class)
@Getter @Setter @NoArgsConstructor
public class VendorBillGrRefEntity {

    @Id
    @Column(name = "bill_id", nullable = false)
    private Long billId;

    @Id
    @Column(name = "gr_id", nullable = false)
    private Long grId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bill_id", insertable = false, updatable = false)
    private VendorBillEntity bill;

    @Getter @Setter @NoArgsConstructor
    @EqualsAndHashCode
    public static class Pk implements Serializable {
        private Long billId;
        private Long grId;
    }
}
