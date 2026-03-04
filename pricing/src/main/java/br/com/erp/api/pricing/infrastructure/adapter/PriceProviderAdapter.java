package br.com.erp.api.pricing.infrastructure.adapter;

import br.com.erp.api.pricing.domain.port.SkuPriceRepositoryPort;
import br.com.erp.api.product.application.provider.PriceProvider;
import br.com.erp.api.product.presentation.dto.response.SkuPriceDTO;
import org.springframework.stereotype.Component;

@Component
public class PriceProviderAdapter implements PriceProvider {

    private final SkuPriceRepositoryPort priceRepository;

    public PriceProviderAdapter(SkuPriceRepositoryPort priceRepository) {
        this.priceRepository = priceRepository;
    }

    @Override
    public SkuPriceDTO getBySkuId(Long skuId) {

        return priceRepository.findBySkuId(skuId)
                .map(p -> new SkuPriceDTO(p.getCostPrice(), p.getSellingPrice()))
                .orElse(new SkuPriceDTO(null, null));
    }
}