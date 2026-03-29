package com.solusi.erp.core.infrastructure.sequence;

import com.solusi.erp.security.user.infrastructure.persistence.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Entity for Generic System Sequence Generator.
 * Used for auto-generating unique codes across modules.
 */
@Entity
@Table(name = "system_sequences")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class SystemSequence {

    @Id
    @Column(name = "module_code", length = 50)
    private String moduleCode;

    @Column(name = "format_pattern", nullable = false, length = 100)
    private String formatPattern;

    @Column(name = "current_value", nullable = false)
    private Long currentValue = 0L;

    @Column(name = "pad_length", nullable = false)
    private Integer padLength = 4;

    @Enumerated(EnumType.STRING)
    @Column(name = "reset_cycle", nullable = false)
    private ResetCycle resetCycle = ResetCycle.NEVER;

    @Column(name = "last_reset_date")
    private LocalDateTime lastResetDate;

    @LastModifiedBy
    @Column(name = "updated_by_user_id")
    private Long updatedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by_user_id", insertable = false, updatable = false)
    private User updatedByUser;

    @LastModifiedDate
    @Column(name = "updated_date")
    private LocalDateTime updatedDate;

    @Version
    private Integer version = 1;

    public enum ResetCycle {
        DAILY, MONTHLY, YEARLY, NEVER
    }
}
