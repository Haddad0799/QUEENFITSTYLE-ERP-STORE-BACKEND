package br.com.erp.api.product.domain.port;

import br.com.erp.api.product.domain.entity.ProductColorImage;

import java.util.List;
import java.util.Optional;

public interface ProductColorImageRepositoryPort {
    void saveAll(List<ProductColorImage> images);
    List<Long> saveAllReturningIds(List<ProductColorImage> images);
    List<ProductColorImage> findByProductIdAndColorId(Long productId, Long colorId);
    List<Integer> findOrdersByProductIdAndColorId(Long productId, Long colorId);
    List<String> findKeysByProductIdAndColorId(Long productId, Long colorId);

    List<ProductColorImage> findAllByIds(List<Long> imageIds);
    void deleteAllByIds(List<Long> imageIds);
    boolean existsByProductIdAndColorId(Long productId, Long colorId);

    Optional<ProductColorImage> findFirstByProductIdExcluding(Long productId, List<Long> excludedIds);
    List<ProductColorImage> findAllByProductIdGroupedByColor(Long productId);
}