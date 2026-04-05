package com.solusi.erp.common.approval.infrastructure.listener;

import com.solusi.erp.common.approval.application.usecase.CreateApprovalRequestUseCase;
import com.solusi.erp.core.event.ApprovalRequestedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Global Listener for any ApprovalRequestedEvent.
 * This is the entry point for the Approval system from other modules.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OnApprovalRequestedListener {

    private final CreateApprovalRequestUseCase createApprovalRequestUseCase;

    @EventListener
    public void handle(ApprovalRequestedEvent event) {
        log.info("Approval requested for {} ID {}", event.getReferenceType(), event.getReferenceId());
        
        createApprovalRequestUseCase.execute(
            event.getReferenceType(), 
            event.getReferenceId(),
            event.getReferenceCode(),
            event.getRequesterId(),
            event.getApproverId()
        );
    }
}
