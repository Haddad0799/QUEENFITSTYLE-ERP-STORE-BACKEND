package br.com.erp.api.product.application.provider;

public interface ImageProvider {
    int countByProductIdAndColorId(Long productId, Long colorId);
}