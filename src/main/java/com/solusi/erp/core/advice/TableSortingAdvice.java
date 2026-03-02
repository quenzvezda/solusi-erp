package com.solusi.erp.core.advice;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Controller Advice to automatically inject sorting information into the Model.
 * This avoids boilerplate in every controller method that uses Pageable.
 */
@ControllerAdvice
public class TableSortingAdvice {

    @ModelAttribute
    public void addSortingAttributes(HttpServletRequest request, Pageable pageable, Model model) {
        // Automatically inject current URI for generic link building in fragments
        model.addAttribute("currentUri", request.getRequestURI());

        if (pageable != null && pageable.getSort().isSorted()) {
            Sort.Order order = pageable.getSort().iterator().next();
            model.addAttribute("sortField", order.getProperty());
            model.addAttribute("sortDir", order.getDirection().name().toLowerCase());
        } else {
            // Default values if no sorting is applied
            model.addAttribute("sortField", "");
            model.addAttribute("sortDir", "asc");
        }
    }
}
