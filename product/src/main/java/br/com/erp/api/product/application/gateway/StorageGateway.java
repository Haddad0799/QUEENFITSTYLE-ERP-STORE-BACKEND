package br.com.erp.api.product.application.gateway;

import br.com.erp.api.product.application.dto.PresignedUrlResult;

import java.util.List;

public interface StorageGateway {
    List<PresignedUrlResult> generatePresignedUrls(Long productId, Long colorId, int quantity);
    String getPublicUrl(String imageKey);
}