package com.solusi.erp.core.storage.infrastructure.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;
import java.util.Optional;

/**
 * Binds the {@code minio.*} properties from application.yaml.
 * Supports multi-bucket configuration via a {@code buckets} map.
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "minio")
public class MinioProperties {

    private String endpoint;
    private String presignedEndpoint;
    private String accessKey;
    private String secretKey;
    private Map<String, String> buckets;

    /**
     * Returns the bucket name for the given logical key.
     *
     * @param key logical bucket key (e.g. "signatures")
     * @return the configured bucket name
     * @throws IllegalStateException if the key is not configured
     */
    public String getBucket(String key) {
        return Optional.ofNullable(buckets.get(key))
                .orElseThrow(() -> new IllegalStateException(
                        "MinIO bucket not configured for key: " + key));
    }
}
