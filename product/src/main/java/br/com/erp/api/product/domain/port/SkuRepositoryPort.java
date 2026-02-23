package br.com.erp.api.product.domain.port;

import br.com.erp.api.product.domain.entity.Sku;

import java.util.List;

public interface SkuRepositoryPort {
    void saveAll(Long productId,List<Sku> skus);
}
