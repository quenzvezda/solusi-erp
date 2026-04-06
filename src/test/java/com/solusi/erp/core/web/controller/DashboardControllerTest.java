package com.solusi.erp.core.web.controller;

import com.solusi.erp.common.approval.domain.repository.ApprovalRequestRepository;
import com.solusi.erp.common.news.domain.repository.NewsRepository;
import com.solusi.erp.common.news.web.mapper.NewsWebMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.ui.Model;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("DashboardController — Unit Test")
class DashboardControllerTest {

    private final NewsRepository newsRepository = mock(NewsRepository.class);
    private final ApprovalRequestRepository approvalRequestRepository = mock(ApprovalRequestRepository.class);
    private final NewsWebMapper newsWebMapper = mock(NewsWebMapper.class);
    private final DashboardController controller =
            new DashboardController(newsRepository, approvalRequestRepository, newsWebMapper);

    @Test
    @DisplayName("dashboard() returns dashboard/index view name with no authorities")
    void dashboardReturnsCorrectViewName() {
        Model model = mock(Model.class);
        Authentication auth = mock(Authentication.class);
        Mockito.doReturn(new ArrayList<GrantedAuthority>()).when(auth).getAuthorities();
        when(newsRepository.findPublishedNews()).thenReturn(List.of());

        String view = controller.dashboard(model, auth);
        assertEquals("dashboard/index", view);
    }

    @Test
    @DisplayName("dashboard() loads news when user has DASHBOARD_NEWS authority")
    void dashboardLoadsNewsForAuthorizedUser() {
        Model model = mock(Model.class);
        Authentication auth = mock(Authentication.class);
        ArrayList<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(() -> "DASHBOARD_NEWS");
        Mockito.doReturn(authorities).when(auth).getAuthorities();
        when(newsRepository.findPublishedNews()).thenReturn(List.of());

        String view = controller.dashboard(model, auth);
        assertEquals("dashboard/index", view);
    }
}
