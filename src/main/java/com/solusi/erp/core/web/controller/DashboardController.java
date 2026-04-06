package com.solusi.erp.core.web.controller;

import com.solusi.erp.common.approval.domain.repository.ApprovalRequestRepository;
import com.solusi.erp.common.news.domain.model.News;
import com.solusi.erp.common.news.domain.repository.NewsRepository;
import com.solusi.erp.common.news.web.dto.NewsDetailResponse;
import com.solusi.erp.common.news.web.mapper.NewsWebMapper;
import com.solusi.erp.security.shared.model.SecurityUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller for the main dashboard (landing page logged in user).
 */
@Controller
@RequiredArgsConstructor
public class DashboardController {

    private final NewsRepository newsRepository;
    private final ApprovalRequestRepository approvalRequestRepository;
    private final NewsWebMapper newsWebMapper;

    @GetMapping("/dashboard")
    @PreAuthorize("hasAuthority('DASHBOARD_READ')")
    public String dashboard(Model model, Authentication authentication) {
        if (hasAuthority(authentication, "DASHBOARD_NEWS")) {
            List<NewsDetailResponse> latestNews = newsRepository.findPublishedNews().stream()
                    .sorted(Comparator.comparing(News::getPublishDate, Comparator.nullsLast(Comparator.reverseOrder())))
                    .limit(5)
                    .map(newsWebMapper::toResponse)
                    .collect(Collectors.toList());
            model.addAttribute("latestNews", latestNews);
        }

        if (hasAuthority(authentication, "DASHBOARD_APPROVAL")) {
            Long approverPartyId = null;
            if (authentication.getPrincipal() instanceof SecurityUser securityUser) {
                approverPartyId = securityUser.user().getPartyId();
            }
            if (approverPartyId != null) {
                model.addAttribute("pendingApprovalCount", approvalRequestRepository.countPendingApprovalsForApprover(approverPartyId));
            } else {
                model.addAttribute("pendingApprovalCount", approvalRequestRepository.countPendingApprovals());
            }
        }

        return "dashboard/index";
    }

    private boolean hasAuthority(Authentication auth, String authority) {
        return auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(authority));
    }
}
