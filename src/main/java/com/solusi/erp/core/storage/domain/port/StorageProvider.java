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
     * Returns a URL for accessing the stored object.
     *
     * @param bucket target bucket name
     * @param key    object key / path inside the bucket
     * @return accessible URL string
     */
    String getUrl(String bucket, String key);

    /**
     * Delete an object from the given bucket.
     *
     * @param bucket target bucket name
     * @param key    object key / path inside the bucket
     */
    void delete(String bucket, String key);
}
