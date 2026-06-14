package com.solusi.erp.common.approval.web.mapper;

import com.solusi.erp.common.approval.domain.model.ApprovalRequest;
import com.solusi.erp.common.approval.signature.domain.model.ApprovalSignature;
import com.solusi.erp.common.approval.web.dto.ApprovalSignatureResponse;
import com.solusi.erp.common.approval.web.dto.ApprovalStatusResponse;
import com.solusi.erp.core.mapper.AuditMapperHelper;
import com.solusi.erp.core.storage.domain.port.StorageProvider;
import com.solusi.erp.master.party.domain.port.PartyLookupProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApprovalWebMapperTest {

    @Mock private StorageProvider storageProvider;
    @Mock private PartyLookupProvider partyLookupProvider;
    @Mock private AuditMapperHelper auditMapperHelper;

    @InjectMocks
    private ApprovalWebMapper mapper;

    // ── toSignatureResponse() ──────────────────────────────────────────────

    @Test
    @DisplayName("toSignatureResponse() calls getPresignedUrl with correct bucket, key and 3600s expiry")
    void toSignatureResponse_callsGetPresignedUrl() {
        ApprovalSignature sig = ApprovalSignature.createNew(
                5L, "signatures/5/abc.png", "approval-signatures", 10L);
        when(storageProvider.getPresignedUrl(
                eq("approval-signatures"), eq("signatures/5/abc.png"), anyInt()))
                .thenReturn("https://minio/presigned?sig=xyz");

        mapper.toSignatureResponse(sig);

        verify(storageProvider).getPresignedUrl("approval-signatures", "signatures/5/abc.png", 3600);
    }

    @Test
    @DisplayName("toSignatureResponse() maps all fields correctly")
    void toSignatureResponse_mapsAllFields() {
        ApprovalSignature sig = ApprovalSignature.createNew(
                5L, "signatures/5/abc.png", "approval-signatures", 10L);
        String presignedUrl = "https://minio/presigned?sig=xyz&expires=1234";
        when(storageProvider.getPresignedUrl(
                eq("approval-signatures"), eq("signatures/5/abc.png"), anyInt()))
                .thenReturn(presignedUrl);

        ApprovalSignatureResponse response = mapper.toSignatureResponse(sig);

        assertThat(response.getRequestId()).isEqualTo(5L);
        assertThat(response.getSignatureUrl()).isEqualTo(presignedUrl);
        assertThat(response.getSignerUserId()).isEqualTo(10L);
        assertThat(response.getSignedAt()).isNotNull();
    }

    @Test
    @DisplayName("toSignatureResponse() presigned URL is returned verbatim as signatureUrl")
    void toSignatureResponse_usesPresignedNotPlainUrl() {
        ApprovalSignature sig = ApprovalSignature.createNew(
                7L, "signatures/7/def.png", "approval-signatures", 3L);
        String presignedUrl = "http://localhost:9000/approval-signatures/signatures/7/def.png"
                + "?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Credential=minioadmin";
        when(storageProvider.getPresignedUrl(
                "approval-signatures", "signatures/7/def.png", 3600))
                .thenReturn(presignedUrl);

        ApprovalSignatureResponse response = mapper.toSignatureResponse(sig);

        assertThat(response.getSignatureUrl()).isEqualTo(presignedUrl);
    }

    @Test
    @DisplayName("toStatusResponse() maps Purchase Requisition approval to PR view URL")
    void toStatusResponse_mapsPurchaseRequisitionDocumentUrl() {
        ApprovalRequest request = ApprovalRequest.createNew(
                "PURCHASE_REQUISITION", 17L, "PR-202604-00001", 1L, 2L);

        ApprovalStatusResponse response = mapper.toStatusResponse(request);

        assertThat(response.getDocumentUrl())
                .isEqualTo("/purchasing/purchase-requisitions/view/17");
    }

    @Test
    @DisplayName("toStatusResponse() maps Purchase Order approval to PO view URL")
    void toStatusResponse_mapsPurchaseOrderDocumentUrl() {
        ApprovalRequest request = ApprovalRequest.createNew(
                "PURCHASE_ORDER", 23L, "PO-202604-00001", 1L, 2L);

        ApprovalStatusResponse response = mapper.toStatusResponse(request);

        assertThat(response.getDocumentUrl())
                .isEqualTo("/purchasing/purchase-orders/view/23");
    }

    @Test
    @DisplayName("toStatusResponse() maps Purchase Return approval to PRT view URL")
    void toStatusResponse_mapsPurchaseReturnDocumentUrl() {
        ApprovalRequest request = ApprovalRequest.createNew(
                "PURCHASE_RETURN", 31L, "PRT-202606-00001", 1L, 2L);

        ApprovalStatusResponse response = mapper.toStatusResponse(request);

        assertThat(response.getDocumentUrl())
                .isEqualTo("/purchasing/purchase-returns/view/31");
    }

    @Test
    @DisplayName("toStatusResponse() keeps NEWS mapped to its detail URL")
    void toStatusResponse_keepsNewsDocumentUrl() {
        ApprovalRequest request = ApprovalRequest.createNew(
                "NEWS", 9L, "NEWS-0009", 1L, 2L);

        ApprovalStatusResponse response = mapper.toStatusResponse(request);

        assertThat(response.getDocumentUrl()).isEqualTo("/common/news/9");
    }
}
