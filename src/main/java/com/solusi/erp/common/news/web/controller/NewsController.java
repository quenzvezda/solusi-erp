package com.solusi.erp.common.news.web.controller;

import com.solusi.erp.common.news.application.usecase.command.CreateNewsUseCase;
import com.solusi.erp.common.news.application.usecase.command.UpdateNewsUseCase;
import com.solusi.erp.common.news.domain.model.News;
import com.solusi.erp.common.news.web.dto.NewsSaveRequest;
import com.solusi.erp.common.news.web.dto.NewsDetailResponse;
import com.solusi.erp.common.news.web.mapper.NewsWebMapper;
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
 * Web Controller for News.
 */
@Controller
@RequestMapping("/common/news")
@RequiredArgsConstructor
public class NewsController {

    private final CreateNewsUseCase createNewsUseCase;
    private final UpdateNewsUseCase updateNewsUseCase;
    private final NewsWebMapper webMapper;
    private final MessageSource messageSource;

    @GetMapping("/create")
    @PreAuthorize("hasAuthority('NEWS_CREATE')")
    public String createForm(Model model) {
        model.addAttribute("news", new NewsSaveRequest());
        return "common/news/form";
    }

    @PostMapping("/create")
    @PreAuthorize("hasAuthority('NEWS_CREATE')")
    @ResponseBody
    public ResponseEntity<ApiResponse<NewsDetailResponse>> create(@Valid @RequestBody NewsSaveRequest request) {
        String author = SecurityContextHolder.getContext().getAuthentication().getName();
        
        // Panggil Use Case
        News domain = createNewsUseCase.execute(
                request.getTitle(), 
                request.getContent(), 
                author
        );
        
        NewsDetailResponse response = webMapper.toResponse(domain);
        String msg = messageSource.getMessage("msg.success.create", null, LocaleContextHolder.getLocale());
        
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(msg, response));
    }

    @PostMapping("/update/{id}")
    @PreAuthorize("hasAuthority('NEWS_UPDATE')")
    @ResponseBody
    public ResponseEntity<ApiResponse<NewsDetailResponse>> update(
            @PathVariable Long id, 
            @Valid @RequestBody NewsSaveRequest request) {
        
        News domain = updateNewsUseCase.execute(id, request.getTitle(), request.getContent());
        
        NewsDetailResponse response = webMapper.toResponse(domain);
        String msg = messageSource.getMessage("msg.success.update", null, LocaleContextHolder.getLocale());
        
        return ResponseEntity.ok(ApiResponse.success(msg, response));
    }
}
