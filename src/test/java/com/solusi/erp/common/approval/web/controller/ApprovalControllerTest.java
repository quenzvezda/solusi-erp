package com.solusi.erp.common.approval.web.controller;

import com.solusi.erp.common.approval.application.usecase.ProcessApprovalUseCase;
import com.solusi.erp.common.approval.domain.model.ApprovalRequest;
import com.solusi.erp.common.approval.domain.repository.ApprovalRequestRepository;
import com.solusi.erp.common.approval.signature.application.usecase.GetApprovalSignatureUrlUseCase;
import com.solusi.erp.common.approval.signature.application.usecase.SaveApprovalSignatureUseCase;
import com.solusi.erp.common.approval.web.dto.ApprovalHistoryResponse;
import com.solusi.erp.common.approval.web.dto.ApprovalStatusResponse;
import com.solusi.erp.common.approval.web.mapper.ApprovalWebMapper;
import com.solusi.erp.core.domain.model.Page;
import com.solusi.erp.core.domain.model.Pageable;
import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.core.storage.domain.port.StorageProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.PageRequest;
import org.springframework.ui.Model;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApprovalControllerTest {

    @Mock private ProcessApprovalUseCase processApprovalUseCase;
    @Mock private ApprovalRequestRepository approvalRequestRepository;
    @Mock private SaveApprovalSignatureUseCase saveApprovalSignatureUseCase;
    @Mock private GetApprovalSignatureUrlUseCase getApprovalSignatureUrlUseCase;
    @Mock private ApprovalWebMapper webMapper;
    @Mock private MessageSource messageSource;
    @Mock private StorageProvider storageProvider;
    @Mock private Model model;

    @InjectMocks
    private ApprovalController controller;

    private ApprovalRequest buildRequest() {
        return ApprovalRequest.createNew("NEWS", 100L, "NEWS-001", 1L, 2L);
    }

    private ApprovalStatusResponse buildStatusResponse(Long requestId) {
        return ApprovalStatusResponse.builder()
                .requestId(requestId)
                .status("PENDING")
                .referenceType("NEWS")
                .referenceId(100L)
                .referenceCode("NEWS-001")
                .histories(List.of(
                        ApprovalHistoryResponse.builder()
                                .action("REQUESTED")
                                .actorName("John Doe")
                                .actionDate(LocalDateTime.now())
                                .build()
                ))
                .build();
    }

    // ── manage() with keyword search ────────────────────────────────────────

    @Test
    @DisplayName("manage() returns manage view with paginated results (no keyword)")
    void manage_noKeyword_returnsManageView() {
        org.springframework.data.domain.Pageable springPageable = PageRequest.of(0, 10);
        ApprovalRequest req = buildRequest();
        Page<ApprovalRequest> domainPage = new Page<>(List.of(req), 0, 10, 1L);

        when(approvalRequestRepository.findAll(isNull(), any(Pageable.class))).thenReturn(domainPage);
        when(webMapper.toStatusResponse(req)).thenReturn(buildStatusResponse(1L));

        String view = controller.manage(null, springPageable, model);

        assertThat(view).isEqualTo("common/approval/manage");
        verify(model).addAttribute(eq("page"), any());
        verify(model).addAttribute("keyword", null);
    }

    @Test
    @DisplayName("manage() with keyword delegates keyword to repository")
    void manage_withKeyword_delegatesToRepository() {
        org.springframework.data.domain.Pageable springPageable = PageRequest.of(0, 10);
        Page<ApprovalRequest> domainPage = new Page<>(Collections.emptyList(), 0, 10, 0L);

        when(approvalRequestRepository.findAll(eq("NEWS"), any(Pageable.class))).thenReturn(domainPage);

        String view = controller.manage("NEWS", springPageable, model);

        assertThat(view).isEqualTo("common/approval/manage");
        verify(approvalRequestRepository).findAll(eq("NEWS"), any(Pageable.class));
        verify(model).addAttribute("keyword", "NEWS");
    }

    @Test
    @DisplayName("manage() keyword is propagated to model for retention in template")
    void manage_keywordIsAddedToModel() {
        org.springframework.data.domain.Pageable springPageable = PageRequest.of(0, 10);
        Page<ApprovalRequest> domainPage = new Page<>(Collections.emptyList(), 0, 10, 0L);

        when(approvalRequestRepository.findAll(eq("INV"), any(Pageable.class))).thenReturn(domainPage);

        controller.manage("INV", springPageable, model);

        verify(model).addAttribute("keyword", "INV");
    }

    @Test
    @DisplayName("manage() with special chars in keyword does not throw")
    void manage_withSpecialCharsKeyword_doesNotThrow() {
        org.springframework.data.domain.Pageable springPageable = PageRequest.of(0, 10);
        Page<ApprovalRequest> domainPage = new Page<>(Collections.emptyList(), 0, 10, 0L);
        String specialKeyword = "%NEWS-001%";

        when(approvalRequestRepository.findAll(eq(specialKeyword), any(Pageable.class))).thenReturn(domainPage);

        String view = controller.manage(specialKeyword, springPageable, model);

        assertThat(view).isEqualTo("common/approval/manage");
        verify(approvalRequestRepository).findAll(eq(specialKeyword), any(Pageable.class));
    }

    @Test
    @DisplayName("manage() with empty page returns empty table correctly")
    void manage_emptyPage_setsEmptyPageInModel() {
        org.springframework.data.domain.Pageable springPageable = PageRequest.of(0, 10);
        Page<ApprovalRequest> domainPage = new Page<>(Collections.emptyList(), 0, 10, 0L);

        when(approvalRequestRepository.findAll(isNull(), any(Pageable.class))).thenReturn(domainPage);

        String view = controller.manage(null, springPageable, model);

        assertThat(view).isEqualTo("common/approval/manage");
        verify(model).addAttribute(eq("page"), argThat(p ->
                p instanceof org.springframework.data.domain.Page &&
                ((org.springframework.data.domain.Page<?>) p).isEmpty()
        ));
    }

    // ── detail() ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("detail() returns detail view when approval request exists")
    void detail_returnsDetailView() {
        Long requestId = 42L;
        ApprovalRequest req = buildRequest();
        ApprovalStatusResponse response = buildStatusResponse(requestId);

        when(approvalRequestRepository.findById(requestId)).thenReturn(Optional.of(req));
        when(webMapper.toStatusResponse(req)).thenReturn(response);

        String view = controller.detail(requestId, model);

        assertThat(view).isEqualTo("common/approval/detail");
        verify(model).addAttribute("approval", response);
    }

    @Test
    @DisplayName("detail() throws DomainException when approval not found")
    void detail_throwsWhenNotFound() {
        when(approvalRequestRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> controller.detail(999L, model))
                .isInstanceOf(DomainException.class)
                .satisfies(ex -> assertThat(((DomainException) ex).getKey())
                        .isEqualTo("msg.error.approval.not-found"));

        verify(model, never()).addAttribute(eq("approval"), any());
    }

    @Test
    @DisplayName("detail() maps approval request to status response via mapper")
    void detail_usesMapper() {
        Long requestId = 10L;
        ApprovalRequest req = buildRequest();
        ApprovalStatusResponse response = buildStatusResponse(requestId);

        when(approvalRequestRepository.findById(requestId)).thenReturn(Optional.of(req));
        when(webMapper.toStatusResponse(req)).thenReturn(response);

        controller.detail(requestId, model);

        verify(webMapper).toStatusResponse(req);
        verify(model).addAttribute("approval", response);
    }

    @Test
    @DisplayName("detail() calls findById with correct requestId")
    void detail_callsFindByIdWithCorrectId() {
        Long requestId = 77L;
        ApprovalRequest req = buildRequest();

        when(approvalRequestRepository.findById(requestId)).thenReturn(Optional.of(req));
        when(webMapper.toStatusResponse(req)).thenReturn(buildStatusResponse(requestId));

        controller.detail(requestId, model);

        verify(approvalRequestRepository).findById(requestId);
    }

    // ── signatureImage() proxy ──────────────────────────────────────────────

    @Test
    @DisplayName("signatureImage() returns 404 when no signature exists")
    void signatureImage_noSignature_returns404() {
        when(getApprovalSignatureUrlUseCase.findByRequestId(1L)).thenReturn(Optional.empty());

        org.springframework.http.ResponseEntity<byte[]> response = controller.signatureImage(1L);

        assertThat(response.getStatusCode().value()).isEqualTo(404);
    }

    @Test
    @DisplayName("signatureImage() returns image bytes when signature exists")
    void signatureImage_withSignature_returnsImage() {
        var sig = com.solusi.erp.common.approval.signature.domain.model.ApprovalSignature
                .createNew(5L, "signatures/5/abc.png", "approval-signatures", 10L);
        byte[] imageBytes = new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47};

        when(getApprovalSignatureUrlUseCase.findByRequestId(5L)).thenReturn(Optional.of(sig));
        when(storageProvider.getBytes("approval-signatures", "signatures/5/abc.png")).thenReturn(imageBytes);

        org.springframework.http.ResponseEntity<byte[]> response = controller.signatureImage(5L);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getHeaders().getContentType()).isEqualTo(org.springframework.http.MediaType.IMAGE_PNG);
        assertThat(response.getBody()).isEqualTo(imageBytes);
    }

    @Test
    @DisplayName("signatureImage() returns 404 when storage throws exception")
    void signatureImage_storageError_returns404() {
        var sig = com.solusi.erp.common.approval.signature.domain.model.ApprovalSignature
                .createNew(5L, "signatures/5/abc.png", "approval-signatures", 10L);

        when(getApprovalSignatureUrlUseCase.findByRequestId(5L)).thenReturn(Optional.of(sig));
        when(storageProvider.getBytes("approval-signatures", "signatures/5/abc.png"))
                .thenThrow(new RuntimeException("MinIO error"));

        org.springframework.http.ResponseEntity<byte[]> response = controller.signatureImage(5L);

        assertThat(response.getStatusCode().value()).isEqualTo(404);
    }
}
