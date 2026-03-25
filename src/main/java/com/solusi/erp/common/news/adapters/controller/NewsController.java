package com.solusi.erp.common.news.adapters.controller;

import com.solusi.erp.common.news.adapters.dto.NewsRequest;
import com.solusi.erp.common.news.adapters.dto.NewsResponse;
import com.solusi.erp.common.news.adapters.mapper.NewsWebMapper;
import com.solusi.erp.common.news.entities.News;
import com.solusi.erp.common.news.usecases.CreateNewsInputBoundary;
import com.solusi.erp.core.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

/**
 * Clean Architecture: Web Controller.
 * Menghubungkan HTTP Request ke Input Boundary (Use Case).
 */
@Controller
@RequestMapping("/common/news")
@RequiredArgsConstructor
public class NewsController {

    private final CreateNewsInputBoundary createNewsInputBoundary;
    private final NewsWebMapper webMapper;
    private final MessageSource messageSource;

    @GetMapping("/create")
    @PreAuthorize("hasAuthority('NEWS_CREATE')")
    public String createForm(Model model) {
        model.addAttribute("news", new NewsRequest());
        return "common/news/form";
    }

    @PostMapping("/create")
    @PreAuthorize("hasAuthority('NEWS_CREATE')")
    @ResponseBody
    public ResponseEntity<ApiResponse<NewsResponse>> create(@Valid @RequestBody NewsRequest request) {
        // Ambil author dari session/security context
        String author = SecurityContextHolder.getContext().getAuthentication().getName();
        
        // Panggil Use Case Interactor melalui Input Boundary
        News domain = createNewsInputBoundary.execute(
                request.getTitle(), 
                request.getContent(), 
                author
        );
        
        NewsResponse response = webMapper.toResponse(domain);
        String msg = messageSource.getMessage("msg.success.create", null, LocaleContextHolder.getLocale());
        
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(msg, response));
    }
}
