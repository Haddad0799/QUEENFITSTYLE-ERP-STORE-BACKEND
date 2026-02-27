package br.com.erp.api.inventory.domain.port;

import br.com.erp.api.inventory.domain.entity.SkuStock;
import java.util.List;
import java.util.Optional;

public interface SkuStockRepositoryPort {
    void saveAll(List<SkuStock> stocks);
    void update(SkuStock stock);
    Optional<SkuStock> findBySkuId(Long skuId);
}