package br.com.erp.api.product.domain.port;

import br.com.erp.api.product.domain.entity.ProductColorImage;

import java.util.List;

public interface ProductColorImageRepositoryPort {
    void saveAll(List<ProductColorImage> images);
    List<ProductColorImage> findByProductIdAndColorId(Long productId, Long colorId);
    List<Integer> findOrdersByProductIdAndColorId(Long productId, Long colorId);
    List<String> findKeysByProductIdAndColorId(Long productId, Long colorId);

    List<ProductColorImage> findAllByIds(List<Long> imageIds);
    void deleteAllByIds(List<Long> imageIds);
    boolean existsByProductIdAndColorId(Long productId, Long colorId);
}