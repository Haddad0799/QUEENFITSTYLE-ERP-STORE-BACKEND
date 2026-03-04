package br.com.erp.api.pricing.domain.port;

import br.com.erp.api.pricing.domain.entity.SkuPrice;

import java.util.List;
import java.util.Optional;

public interface SkuPriceRepositoryPort {
    void saveAll(List<SkuPrice> prices);
    Optional<SkuPrice> findBySkuId(Long skuId);
}