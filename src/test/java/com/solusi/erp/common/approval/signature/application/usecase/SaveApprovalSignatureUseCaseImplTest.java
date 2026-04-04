package com.solusi.erp.common.approval.signature.application.usecase;

import com.solusi.erp.common.approval.domain.model.ApprovalRequest;
import com.solusi.erp.common.approval.domain.repository.ApprovalRequestRepository;
import com.solusi.erp.common.approval.signature.domain.model.ApprovalSignature;
import com.solusi.erp.common.approval.signature.domain.repository.ApprovalSignatureRepository;
import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.core.storage.domain.port.StorageProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Base64;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SaveApprovalSignatureUseCaseImplTest {

    @Mock
    private ApprovalSignatureRepository signatureRepository;

    @Mock
    private ApprovalRequestRepository approvalRequestRepository;

    @Mock
    private StorageProvider storageProvider;

    private SaveApprovalSignatureUseCaseImpl useCase;

    private static final String BUCKET = "approval-signatures";
    private static final Long REQUEST_ID = 1L;
    private static final Long SIGNER_ID = 10L;

    @BeforeEach
    void setUp() {
        useCase = new SaveApprovalSignatureUseCaseImpl(
                signatureRepository,
                approvalRequestRepository,
                storageProvider,
                BUCKET
        );
    }

    @Test
    @DisplayName("execute with valid base64 PNG should store image and save metadata")
    void execute_withValidBase64_storesAndSavesMetadata() {
        byte[] fakeImage = "fake-png-data".getBytes();
        String base64 = Base64.getEncoder().encodeToString(fakeImage);

        ApprovalRequest mockRequest = ApprovalRequest.createNew("NEWS", 1L, 99L, 50L);
        when(approvalRequestRepository.findById(REQUEST_ID)).thenReturn(Optional.of(mockRequest));
        when(signatureRepository.save(any(ApprovalSignature.class))).thenAnswer(inv -> inv.getArgument(0));

        ApprovalSignature result = useCase.execute(REQUEST_ID, base64, SIGNER_ID);

        assertNotNull(result);
        assertEquals(REQUEST_ID, result.getRequestId());
        assertEquals(BUCKET, result.getBucketName());
        assertEquals(SIGNER_ID, result.getSignerUserId());
        assertNotNull(result.getStoredAt());

        verify(storageProvider).store(eq(BUCKET), anyString(), any(byte[].class), eq("image/png"));
        verify(signatureRepository).save(any(ApprovalSignature.class));
    }

    @Test
    @DisplayName("execute with data URI prefix should strip prefix before decoding")
    void execute_withDataUriPrefix_shouldStripAndDecode() {
        byte[] fakeImage = "fake-png-data".getBytes();
        String base64Raw = Base64.getEncoder().encodeToString(fakeImage);
        String dataUri = "data:image/png;base64," + base64Raw;

        ApprovalRequest mockRequest = ApprovalRequest.createNew("NEWS", 1L, 99L, 50L);
        when(approvalRequestRepository.findById(REQUEST_ID)).thenReturn(Optional.of(mockRequest));
        when(signatureRepository.save(any(ApprovalSignature.class))).thenAnswer(inv -> inv.getArgument(0));

        ApprovalSignature result = useCase.execute(REQUEST_ID, dataUri, SIGNER_ID);

        assertNotNull(result);
        ArgumentCaptor<byte[]> bytesCaptor = ArgumentCaptor.forClass(byte[].class);
        verify(storageProvider).store(eq(BUCKET), anyString(), bytesCaptor.capture(), eq("image/png"));
        assertArrayEquals(fakeImage, bytesCaptor.getValue());
    }

    @Test
    @DisplayName("execute with empty string signature should throw DomainException")
    void execute_withEmptySignature_throwsDomainException() {
        DomainException ex = assertThrows(DomainException.class,
                () -> useCase.execute(REQUEST_ID, "", SIGNER_ID));

        assertEquals("msg.error.approval.signature.empty", ex.getKey());
        verify(approvalRequestRepository, never()).findById(any());
        verify(storageProvider, never()).store(any(), any(), any(), any());
    }

    @Test
    @DisplayName("execute with null signature should throw DomainException")
    void execute_withNullSignature_throwsDomainException() {
        DomainException ex = assertThrows(DomainException.class,
                () -> useCase.execute(REQUEST_ID, null, SIGNER_ID));

        assertEquals("msg.error.approval.signature.empty", ex.getKey());
        verify(approvalRequestRepository, never()).findById(any());
        verify(storageProvider, never()).store(any(), any(), any(), any());
    }

    @Test
    @DisplayName("execute with blank whitespace-only signature should throw DomainException")
    void execute_withBlankSignature_throwsDomainException() {
        DomainException ex = assertThrows(DomainException.class,
                () -> useCase.execute(REQUEST_ID, "   ", SIGNER_ID));

        assertEquals("msg.error.approval.signature.empty", ex.getKey());
        verify(storageProvider, never()).store(any(), any(), any(), any());
    }

    @Test
    @DisplayName("execute with invalid base64 string should throw DomainException with invalid-format key")
    void execute_withInvalidBase64_throwsInvalidFormatException() {
        String invalidBase64 = "NOT!!!VALID!!!BASE64!!!@@@";

        when(approvalRequestRepository.findById(REQUEST_ID))
                .thenReturn(Optional.of(ApprovalRequest.createNew("NEWS", 1L, 99L, 50L)));

        DomainException ex = assertThrows(DomainException.class,
                () -> useCase.execute(REQUEST_ID, invalidBase64, SIGNER_ID));

        assertEquals("msg.error.approval.signature.invalid-format", ex.getKey());
        verify(storageProvider, never()).store(any(), any(), any(), any());
    }

    @Test
    @DisplayName("execute on non-existent approvalRequest should throw DomainException with not-found key")
    void execute_onNonExistentApprovalRequest_throwsNotFoundException() {
        String base64 = Base64.getEncoder().encodeToString("data".getBytes());
        when(approvalRequestRepository.findById(REQUEST_ID)).thenReturn(Optional.empty());

        DomainException ex = assertThrows(DomainException.class,
                () -> useCase.execute(REQUEST_ID, base64, SIGNER_ID));

        assertEquals("msg.error.approval.not-found", ex.getKey());
        verify(storageProvider, never()).store(any(), any(), any(), any());
        verify(signatureRepository, never()).save(any());
    }

    @Test
    @DisplayName("execute when storageProvider throws should propagate exception without saving metadata")
    void execute_whenStorageFails_doesNotSaveMetadata() {
        String base64 = Base64.getEncoder().encodeToString("data".getBytes());
        when(approvalRequestRepository.findById(REQUEST_ID))
                .thenReturn(Optional.of(ApprovalRequest.createNew("NEWS", 1L, 99L, 50L)));
        doThrow(new RuntimeException("MinIO connection refused"))
                .when(storageProvider).store(any(), any(), any(), any());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> useCase.execute(REQUEST_ID, base64, SIGNER_ID));

        assertEquals("MinIO connection refused", ex.getMessage());
        verify(signatureRepository, never()).save(any());
    }

    @Test
    @DisplayName("execute should generate unique storageKey per call")
    void execute_shouldGenerateUniqueStorageKey() {
        byte[] fakeImage = "some-bytes".getBytes();
        String base64 = Base64.getEncoder().encodeToString(fakeImage);

        when(approvalRequestRepository.findById(REQUEST_ID))
                .thenReturn(Optional.of(ApprovalRequest.createNew("NEWS", 1L, 99L, 50L)));
        when(signatureRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        useCase.execute(REQUEST_ID, base64, SIGNER_ID);
        useCase.execute(REQUEST_ID, base64, SIGNER_ID);

        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        verify(storageProvider, times(2)).store(eq(BUCKET), keyCaptor.capture(), any(), any());

        String key1 = keyCaptor.getAllValues().get(0);
        String key2 = keyCaptor.getAllValues().get(1);
        assertNotEquals(key1, key2, "Each upload should generate a unique storage key");
    }
}
