package com.solusi.erp.common.news.integration;

import com.solusi.erp.common.approval.application.usecase.ProcessApprovalUseCase;
import com.solusi.erp.common.approval.domain.model.ApprovalRequest;
import com.solusi.erp.common.approval.domain.repository.ApprovalRequestRepository;
import com.solusi.erp.common.news.application.usecase.command.CreateNewsUseCase;
import com.solusi.erp.common.news.application.usecase.command.SubmitNewsForApprovalUseCase;
import com.solusi.erp.common.news.domain.model.News;
import com.solusi.erp.common.news.domain.model.NewsStatus;
import com.solusi.erp.common.news.domain.repository.NewsRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * POC Integration Test to verify REAL Event-Driven Approval Flow.
 */
@SpringBootTest
@ActiveProfiles("test")
class NewsApprovalIntegrationTest {

    @Autowired
    private CreateNewsUseCase createNewsUseCase;

    @Autowired
    private SubmitNewsForApprovalUseCase submitNewsForApprovalUseCase;

    @Autowired
    private ProcessApprovalUseCase processApprovalUseCase;

    @Autowired
    private NewsRepository newsRepository;

    @Autowired
    private ApprovalRequestRepository approvalRepository;

    @Test
    void shouldCompleteApprovalFlowWithRealModule() {
        // 1. Create News (DRAFT)
        String uniqueTitle = "POC Version Support " + System.currentTimeMillis();
        News news = createNewsUseCase.execute(uniqueTitle, "POC Content", "Author");
        assertNotNull(news.getId());
        assertEquals(NewsStatus.DRAFT, news.getStatus());

        // 2. Submit for Approval (Status -> PENDING_APPROVAL)
        submitNewsForApprovalUseCase.execute(news.getId(), "Author");
        
        // Verify News Status is now PENDING_APPROVAL
        News pendingNews = newsRepository.findById(news.getId()).orElseThrow();
        assertEquals(NewsStatus.PENDING_APPROVAL, pendingNews.getStatus());

        // Verify that an Approval Request was created in the database
        ApprovalRequest appReq = approvalRepository.findByReference("NEWS", news.getId()).orElseThrow();
        assertEquals(com.solusi.erp.common.approval.domain.model.ApprovalStatus.PENDING, appReq.getStatus());

        // 3. Perform Manual Approval (This simulates the Boss clicking "Approve")
        processApprovalUseCase.approve(appReq.getId(), 1L, "Looks good to me!");

        // 4. Verify the final result
        News finalNews = newsRepository.findById(news.getId()).orElseThrow();
        assertEquals(NewsStatus.PUBLISHED, finalNews.getStatus());
        
        System.out.println("POC REAL SUCCESS: News published after manual approval in the Generic Module!");
    }
}
