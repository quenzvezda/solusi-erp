package com.solusi.erp.security.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controller for handling login and logout views.
 */
@Controller
public class AuthController {

    @GetMapping("/login")
    public String login() {
        return "security/login"; // Refers to src/main/resources/templates/security/login.html
    }
}
