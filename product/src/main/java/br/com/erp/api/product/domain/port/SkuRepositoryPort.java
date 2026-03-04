package br.com.erp.api.product.domain.port;

import br.com.erp.api.product.domain.entity.Sku;

import java.util.List;
import java.util.Map;

public interface SkuRepositoryPort {
    Map<String, Long> saveAll(Long productId, List<Sku> skus);
}
