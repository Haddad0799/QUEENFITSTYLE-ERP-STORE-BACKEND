package br.com.erp.api.pricing.infrastructure;

import br.com.erp.api.pricing.domain.entity.SkuPrice;
import br.com.erp.api.pricing.domain.port.SkuPriceRepositoryPort;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class SkuPriceJdbiRepository implements SkuPriceRepositoryPort {
    @Override
    public void saveAll(List<SkuPrice> prices) {

    }

    @Override
    public Optional<SkuPrice> findBySkuId(Long skuId) {
        return Optional.empty();
    }
}
