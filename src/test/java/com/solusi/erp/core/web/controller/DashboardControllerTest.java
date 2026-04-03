package com.solusi.erp.core.web.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("DashboardController — Unit Test")
class DashboardControllerTest {

    private final DashboardController controller = new DashboardController();

    @Test
    @DisplayName("dashboard() returns dashboard/index view name")
    void dashboardReturnsCorrectViewName() {
        String view = controller.dashboard();
        assertEquals("dashboard/index", view);
    }
}
