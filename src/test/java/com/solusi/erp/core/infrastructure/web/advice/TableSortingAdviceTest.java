package com.solusi.erp.core.infrastructure.web.advice;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;

class TableSortingAdviceTest {

    private final TableSortingAdvice advice = new TableSortingAdvice();

    @Test
    void addSortingAttributes_withSortedPageable_setsSortFieldAndDir() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/items");
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "name"));
        Model model = new ExtendedModelMap();

        advice.addSortingAttributes(request, pageable, model);

        assertThat(model.getAttribute("sortField")).isEqualTo("name");
        assertThat(model.getAttribute("sortDir")).isEqualTo("desc");
    }

    @Test
    void addSortingAttributes_withUnsortedPageable_setsDefaults() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/items");
        Pageable pageable = PageRequest.of(0, 10);
        Model model = new ExtendedModelMap();

        advice.addSortingAttributes(request, pageable, model);

        assertThat(model.getAttribute("sortField")).isEqualTo("");
        assertThat(model.getAttribute("sortDir")).isEqualTo("asc");
    }

    @SuppressWarnings("unchecked")
    @Test
    void addSortingAttributes_populatesUrlBuilders() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/items");
        request.setServerName("localhost");
        request.setScheme("http");
        request.setServerPort(8080);
        Pageable pageable = PageRequest.of(0, 10);
        Model model = new ExtendedModelMap();

        advice.addSortingAttributes(request, pageable, model);

        assertThat(model.getAttribute("pageUrlBuilder")).isInstanceOf(Function.class);
        assertThat(model.getAttribute("sortUrlBuilder")).isInstanceOf(Function.class);
        assertThat(model.getAttribute("currentUri")).isEqualTo("/items");

        Function<Integer, String> pageUrlBuilder = (Function<Integer, String>) model.getAttribute("pageUrlBuilder");
        assertThat(pageUrlBuilder.apply(2)).contains("page=2");

        Function<String, String> sortUrlBuilder = (Function<String, String>) model.getAttribute("sortUrlBuilder");
        assertThat(sortUrlBuilder.apply("name,asc")).contains("sort=name,asc");
    }
}
