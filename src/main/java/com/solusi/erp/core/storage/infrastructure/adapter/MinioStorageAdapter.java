package com.solusi.erp.core.storage.infrastructure.adapter;

import com.solusi.erp.core.storage.domain.port.StorageProvider;
import com.solusi.erp.core.storage.infrastructure.config.MinioProperties;
import io.minio.*;
import io.minio.errors.MinioException;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * MinIO-backed implementation of StorageProvider.
 * Ensures all configured buckets exist at application startup.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MinioStorageAdapter implements StorageProvider {

    private final MinioClient minioClient;
    private final MinioProperties minioProperties;

    @PostConstruct
    public void ensureBucketsExist() {
        minioProperties.getBuckets().forEach((key, bucketName) -> {
            try {
                boolean exists = minioClient.bucketExists(
                        BucketExistsArgs.builder().bucket(bucketName).build()
                );
                if (!exists) {
                    minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
                    log.info("MinIO bucket created: {}", bucketName);
                } else {
                    log.debug("MinIO bucket already exists: {}", bucketName);
                }
            } catch (MinioException | InvalidKeyException | NoSuchAlgorithmException | IOException e) {
                log.error("Failed to verify/create MinIO bucket '{}': {}", bucketName, e.getMessage(), e);
                throw new IllegalStateException("MinIO bucket initialization failed for '" + bucketName + "'. Check MinIO connectivity.", e);
            }
        });
    }

    @Override
    public void store(String bucket, String key, byte[] data, String contentType) {
        try {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucket)
                            .object(key)
                            .stream(new ByteArrayInputStream(data), data.length, -1)
                            .contentType(contentType)
                            .build()
            );
            log.debug("Stored object '{}' in bucket '{}'.", key, bucket);
        } catch (MinioException | InvalidKeyException | NoSuchAlgorithmException | IOException e) {
            throw new StorageException("Failed to store object '" + key + "' in bucket '" + bucket + "'", e);
        }
    }

    @Override
    public String getUrl(String bucket, String key) {
        return minioProperties.getEndpoint() + "/" + bucket + "/" + key;
    }

    @Override
    public void delete(String bucket, String key) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder().bucket(bucket).object(key).build()
            );
            log.debug("Deleted object '{}' from bucket '{}'.", key, bucket);
        } catch (MinioException | InvalidKeyException | NoSuchAlgorithmException | IOException e) {
            throw new StorageException("Failed to delete object '" + key + "' from bucket '" + bucket + "'", e);
        }
    }

    public static class StorageException extends RuntimeException {
        public StorageException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
