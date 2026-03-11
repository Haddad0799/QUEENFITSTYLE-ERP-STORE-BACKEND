package br.com.erp.api.storage.domain.port;

import java.util.List;

public interface StoragePort {
    String generatePresignedUploadUrl(String imageKey);  // para upload
    String getPublicUrl(String imageKey);
    void deleteImages(List<String> imageKeys);
}
