package br.com.erp.api.product.domain.port;

import br.com.erp.api.product.domain.entity.Sku;
import br.com.erp.api.product.domain.enumerated.SkuStatus;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface SkuRepositoryPort {
    Map<String, Long> saveAll(Long productId, List<Sku> skus);

    boolean existsByProductIdAndColorId(Long productId, Long colorId);

    List<Sku> findByProductIdAndStatus(Long productId, SkuStatus status);

    List<Long> findIdsByProductIdAndColorId(Long productId, Long colorId);

    Optional<Sku> findById(Long skuId);

    void updateStatus(Sku sku);

    List<Sku> findByProductId(Long productId);

    void updateStatusByProductIdAndColorId(Long productId, Long colorId, SkuStatus skuStatus);
    Optional<Sku> findByProductIdAndSkuId(Long productId, Long skuId);
    void updateDimensions(Sku sku);

    boolean existsByProductIdAndSkuId(Long productId, Long skuId);

    void updateStatusBatch(List<Long> skuIds, SkuStatus skuStatus);

    List<Sku> findByProductIdAndStatusIn(Long productId, List<SkuStatus> ready);

}
