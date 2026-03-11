package br.com.erp.api.product.application.gateway;

import br.com.erp.api.product.application.dto.PresignedUrlResult;

import java.util.List;

public interface StorageGateway {
    List<PresignedUrlResult> generatePresignedUrls(Long productId, Long colorId, List<String> filenames);
    String getPublicUrl(String imageKey);
    void deleteImages(List<String> imageKeys);
}