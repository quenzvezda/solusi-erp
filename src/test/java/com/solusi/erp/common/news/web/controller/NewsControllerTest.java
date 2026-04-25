package com.solusi.erp.common.news.web.controller;

import com.solusi.erp.common.approval.application.usecase.query.FindApprovalRequestByReferenceUseCase;
import com.solusi.erp.common.news.application.usecase.command.CreateNewsUseCase;
import com.solusi.erp.common.news.application.usecase.command.SubmitNewsForApprovalUseCase;
import com.solusi.erp.common.news.application.usecase.command.UpdateNewsUseCase;
import com.solusi.erp.common.news.domain.model.News;
import com.solusi.erp.common.news.domain.repository.NewsRepository;
import com.solusi.erp.common.news.web.dto.NewsDetailResponse;
import com.solusi.erp.common.news.web.mapper.NewsWebMapper;
import com.solusi.erp.core.domain.model.AuditMetadata;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.MessageSource;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("NewsController Tests")
public class NewsControllerTest {

    private CreateNewsUseCase createUc;
    private UpdateNewsUseCase updateUc;
    private SubmitNewsForApprovalUseCase submitUc;
    private NewsRepository newsRepository;
    private FindApprovalRequestByReferenceUseCase findApprovalRequestByReferenceUseCase;
    private NewsWebMapper webMapper;
    private MessageSource messageSource;
    private NewsController controller;

    @BeforeEach
    void setUp() {
        createUc = mock(CreateNewsUseCase.class);
        updateUc = mock(UpdateNewsUseCase.class);
        submitUc = mock(SubmitNewsForApprovalUseCase.class);
        newsRepository = mock(NewsRepository.class);
        findApprovalRequestByReferenceUseCase = mock(FindApprovalRequestByReferenceUseCase.class);
        webMapper = mock(NewsWebMapper.class);
        messageSource = mock(MessageSource.class);

        controller = new NewsController(
            createUc, updateUc, submitUc, newsRepository,
            findApprovalRequestByReferenceUseCase, webMapper, messageSource
        );
    }

    private News buildDraftNews() {
        AuditMetadata metadata = new AuditMetadata(1L, 1L, null, null, null, null);
        com.solusi.erp.common.news.domain.model.NewsContent content = 
            new com.solusi.erp.common.news.domain.model.NewsContent("Test News Title for Approval", "Test Content for News");
        return new News(metadata, "NEWS-001", content,
            com.solusi.erp.common.news.domain.model.NewsStatus.DRAFT,
            java.time.LocalDateTime.of(2026, 7, 1, 0, 0),
            java.time.LocalDateTime.of(2026, 12, 31, 23, 59),
            "admin");
    }

    @Test
    @DisplayName("list returns list view with page model")
    void listShouldReturnListViewAndModel() {
        News news = buildDraftNews();
        com.solusi.erp.core.domain.model.Page<News> domainPage =
            new com.solusi.erp.core.domain.model.Page<>(List.of(news), 0, 20, 1L);
        when(newsRepository.findAll(any(), any())).thenReturn(domainPage);

        NewsDetailResponse response = new NewsDetailResponse();
        response.setId(1L);
        response.setTitle("Test News Title for Approval");
        when(webMapper.toResponse(any(News.class))).thenReturn(response);

        org.springframework.data.domain.Pageable springPageable =
            org.springframework.data.domain.PageRequest.of(0, 20);
        Model model = new ExtendedModelMap();

        String view = controller.list(null, springPageable, model);

        assertEquals("common/news/list", view);
        Object pageObj = model.getAttribute("page");
        assertThat(pageObj).isInstanceOf(org.springframework.data.domain.Page.class);
        org.springframework.data.domain.Page<?> springPage = (org.springframework.data.domain.Page<?>) pageObj;
        assertEquals(1, springPage.getTotalElements());
    }

    @Test
    @DisplayName("detail returns detail view with news")
    void detailShouldReturnDetailViewWithNews() {
        News news = buildDraftNews();
        when(newsRepository.findById(1L)).thenReturn(Optional.of(news));

        NewsDetailResponse response = new NewsDetailResponse();
        response.setId(1L);
        response.setTitle("Test News Title for Approval");
        when(webMapper.toResponse(any(News.class))).thenReturn(response);

        Model model = new ExtendedModelMap();
        String view = controller.detail(1L, model, null);

        assertEquals("common/news/detail", view);
        Object newsObj = model.getAttribute("news");
        assertThat(newsObj).isNotNull().isInstanceOf(NewsDetailResponse.class);
        assertEquals("Test News Title for Approval", ((NewsDetailResponse) newsObj).getTitle());
    }

    @Test
    @DisplayName("createForm returns form view")
    void createFormShouldReturnFormView() {
        Model model = new ExtendedModelMap();
        String view = controller.createForm(model);

        assertEquals("common/news/form", view);
        assertThat(model.getAttribute("news")).isNotNull();
    }

    @Test
    @DisplayName("editForm returns form view with news")
    void editFormShouldReturnFormViewWithNews() {
        News news = buildDraftNews();
        when(newsRepository.findById(1L)).thenReturn(Optional.of(news));
        when(webMapper.toRequest(any(News.class))).thenReturn(new com.solusi.erp.common.news.web.dto.NewsSaveRequest());

        Model model = new ExtendedModelMap();
        String view = controller.editForm(1L, model);

        assertEquals("common/news/form", view);
        assertThat(model.getAttribute("news")).isNotNull();
        assertThat(model.getAttribute("isEdit")).isEqualTo(true);
    }
}
