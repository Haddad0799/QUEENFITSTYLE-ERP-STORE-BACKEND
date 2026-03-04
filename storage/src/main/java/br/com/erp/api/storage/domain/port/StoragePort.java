package br.com.erp.api.storage.domain.port;

public interface StoragePort {
    String generatePresignedUploadUrl(String imageKey);  // para upload
    String getPublicUrl(String imageKey);
}
