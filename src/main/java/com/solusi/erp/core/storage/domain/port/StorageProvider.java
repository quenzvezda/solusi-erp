package com.solusi.erp.core.storage.domain.port;

/**
 * Port for storing and retrieving binary objects (e.g., signature images).
 * Implementations are in the infrastructure layer (e.g., MinioStorageAdapter).
 */
public interface StorageProvider {

    /**
     * Store binary data into the given bucket under the given key.
     *
     * @param bucket      target bucket name
     * @param key         object key / path inside the bucket
     * @param data        binary content to store
     * @param contentType MIME type (e.g., "image/png")
     */
    void store(String bucket, String key, byte[] data, String contentType);

    /**
     * Returns a direct (non-signed) URL for the stored object.
     * Note: requires the bucket to be publicly accessible. Prefer
     * {@link #getPresignedUrl} for private buckets.
     *
     * @param bucket target bucket name
     * @param key    object key / path inside the bucket
     * @return accessible URL string
     */
    String getUrl(String bucket, String key);

    /**
     * Returns a time-limited presigned URL for accessing the stored object.
     * The URL embeds authentication credentials in query parameters so the
     * browser (or any HTTP client) can access the object directly without
     * additional auth — even in private buckets. This is the S3/MinIO
     * best-practice for serving user-specific binary objects.
     *
     * @param bucket        target bucket name
     * @param key           object key / path inside the bucket
     * @param expirySeconds URL validity in seconds (e.g. 3600 for 1 hour)
     * @return presigned URL string valid for {@code expirySeconds}
     */
    String getPresignedUrl(String bucket, String key, int expirySeconds);

    /**
     * Retrieve the raw bytes of a stored object.
     *
     * @param bucket target bucket name
     * @param key    object key / path inside the bucket
     * @return binary content of the object
     */
    byte[] getBytes(String bucket, String key);

    /**
     * Delete an object from the given bucket.
     *
     * @param bucket target bucket name
     * @param key    object key / path inside the bucket
     */
    void delete(String bucket, String key);
}
