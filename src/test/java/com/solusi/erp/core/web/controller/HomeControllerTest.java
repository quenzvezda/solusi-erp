package com.solusi.erp.core.web.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("HomeController — Unit Test")
class HomeControllerTest {

    private final HomeController controller = new HomeController();

    @Test
    @DisplayName("index() returns home view name")
    void indexReturnsCorrectViewName() {
        String view = controller.index();
        assertEquals("home", view);
    }
}
