package com.solusi.erp.common.news.infrastructure.listener;

import com.solusi.erp.common.news.application.usecase.command.PublishNewsUseCase;
import com.solusi.erp.core.event.ApprovalCompletedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Listener for ApprovalCompletedEvent.
 * Reacts when a News item is approved by the Generic Approval System.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OnNewsApprovedListener {

    private final PublishNewsUseCase publishNewsUseCase;

    @EventListener(condition = "#event.referenceType == 'NEWS'")
    public void handle(ApprovalCompletedEvent event) {
        log.info("News item approved: ID {}. Finalizing publication...", event.getReferenceId());
        
        // Finalize publication
        publishNewsUseCase.execute(event.getReferenceId(), null, null);
    }
}
