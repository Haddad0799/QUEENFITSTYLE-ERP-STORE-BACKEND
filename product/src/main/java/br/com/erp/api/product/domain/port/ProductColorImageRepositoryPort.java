package br.com.erp.api.product.domain.port;

import br.com.erp.api.product.domain.entity.ProductColorImage;

import java.util.List;

public interface ProductColorImageRepositoryPort {
    void saveAll(List<ProductColorImage> images);
    int countByProductIdAndColorId(Long productId, Long colorId);
    List<ProductColorImage> findByProductIdAndColorId(Long productId, Long colorId);
}