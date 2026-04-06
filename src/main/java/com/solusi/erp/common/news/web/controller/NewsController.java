package com.solusi.erp.common.news.web.controller;

import com.solusi.erp.common.approval.domain.model.ApprovalRequest;
import com.solusi.erp.common.approval.domain.repository.ApprovalRequestRepository;
import com.solusi.erp.common.news.application.usecase.command.CreateNewsUseCase;
import com.solusi.erp.common.news.application.usecase.command.SubmitNewsForApprovalUseCase;
import com.solusi.erp.common.news.application.usecase.command.UpdateNewsUseCase;
import com.solusi.erp.common.news.domain.model.News;
import com.solusi.erp.common.news.domain.repository.NewsRepository;
import com.solusi.erp.common.news.web.dto.NewsSaveRequest;
import com.solusi.erp.common.news.web.dto.NewsDetailResponse;
import com.solusi.erp.common.news.web.mapper.NewsWebMapper;
import com.solusi.erp.core.annotation.DefaultRedirectUrl;
import com.solusi.erp.core.domain.model.Pageable;
import com.solusi.erp.core.dto.ApiResponse;
import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.core.infrastructure.util.PageableMapper;
import com.solusi.erp.security.shared.model.SecurityUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Web Controller for News.
 */
@Controller
@RequestMapping("/common/news")
@RequiredArgsConstructor
@DefaultRedirectUrl
public class NewsController {

    private final CreateNewsUseCase createNewsUseCase;
    private final UpdateNewsUseCase updateNewsUseCase;
    private final SubmitNewsForApprovalUseCase submitNewsForApprovalUseCase;
    private final NewsRepository newsRepository;
    private final ApprovalRequestRepository approvalRequestRepository;
    private final NewsWebMapper webMapper;
    private final MessageSource messageSource;

    @GetMapping
    @PreAuthorize("hasAuthority('NEWS_READ')")
    public String list(@RequestParam(required = false) String keyword,
                       org.springframework.data.domain.Pageable springPageable,
                       Model model) {
        Pageable domainPageable = PageableMapper.toDomain(springPageable);
        com.solusi.erp.core.domain.model.Page<News> domainPage =
                newsRepository.findAll(keyword, domainPageable);

        List<NewsDetailResponse> content = domainPage.content().stream()
                .map(webMapper::toResponse)
                .collect(Collectors.toList());

        Page<NewsDetailResponse> springPage = new PageImpl<>(content, springPageable, domainPage.totalElements());
        model.addAttribute("page", springPage);
        model.addAttribute("keyword", keyword);
        return "common/news/list";
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('NEWS_READ')")
    public String detail(@PathVariable Long id, Model model,
                         @AuthenticationPrincipal UserDetails principal) {
        News news = newsRepository.findById(id)
                .orElseThrow(() -> new DomainException("msg.error.news.not-found"));
        model.addAttribute("news", webMapper.toResponse(news));

        Optional<ApprovalRequest> approvalRequest =
                approvalRequestRepository.findByReference("NEWS", id);
        approvalRequest.ifPresent(req -> {
            model.addAttribute("approvalRequestId", req.getId());
            model.addAttribute("approvalStatus", req.getStatus());
            boolean isCurrentApprover = false;
            if (principal instanceof SecurityUser securityUser) {
                Long partyId = securityUser.user().getPartyId();
                isCurrentApprover = partyId != null && partyId.equals(req.getCurrentApproverId())
                        && req.getStatus() == com.solusi.erp.common.approval.domain.model.ApprovalStatus.PENDING;
                model.addAttribute("currentPartyId", partyId);
            }
            model.addAttribute("isCurrentApprover", isCurrentApprover);
        });

        return "common/news/detail";
    }

    @GetMapping("/create")
    @PreAuthorize("hasAuthority('NEWS_CREATE')")
    public String createForm(Model model) {
        model.addAttribute("news", new NewsSaveRequest());
        return "common/news/form";
    }

    @GetMapping("/edit/{id}")
    @PreAuthorize("hasAuthority('NEWS_UPDATE')")
    public String editForm(@PathVariable Long id, Model model) {
        News news = newsRepository.findById(id)
                .orElseThrow(() -> new DomainException("msg.error.news.not-found"));
        model.addAttribute("news", webMapper.toRequest(news));
        model.addAttribute("isEdit", true);
        return "common/news/form";
    }

    @PostMapping("/create")
    @PreAuthorize("hasAuthority('NEWS_CREATE')")
    @ResponseBody
    public ResponseEntity<ApiResponse<NewsDetailResponse>> create(@Valid @RequestBody NewsSaveRequest request) {
        String author = SecurityContextHolder.getContext().getAuthentication().getName();
        News domain = createNewsUseCase.execute(request.getTitle(), request.getContent(), author, request.getPublishDate(), request.getExpiryDate());
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
        News domain = updateNewsUseCase.execute(id, request.getTitle(), request.getContent(), request.getPublishDate(), request.getExpiryDate());
        NewsDetailResponse response = webMapper.toResponse(domain);
        String msg = messageSource.getMessage("msg.success.update", null, LocaleContextHolder.getLocale());
        return ResponseEntity.ok(ApiResponse.success(msg, response));
    }

    @PostMapping("/{id}/submit-for-approval")
    @PreAuthorize("hasAuthority('NEWS_UPDATE')")
    @ResponseBody
    public ResponseEntity<ApiResponse<NewsDetailResponse>> submitForApproval(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, Long> body,
            @AuthenticationPrincipal SecurityUser securityUser) {
        Long requesterId = securityUser.user().getPartyId();
        Long approverId = (body != null) ? body.get("approverId") : null;
        News domain = submitNewsForApprovalUseCase.execute(id, requesterId, approverId);
        NewsDetailResponse response = webMapper.toResponse(domain);
        String msg = messageSource.getMessage("msg.success.update", null, LocaleContextHolder.getLocale());
        return ResponseEntity.ok(ApiResponse.success(msg, response));
    }
}
