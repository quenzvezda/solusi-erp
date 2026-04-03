package com.solusi.erp.core.web.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controller for the main dashboard (landing page logged in user).
 */
@Controller
public class DashboardController {

    @GetMapping("/dashboard")
    @PreAuthorize("hasAuthority('DASHBOARD_READ')")
    public String dashboard() {
        return "dashboard/index";
    }
}
