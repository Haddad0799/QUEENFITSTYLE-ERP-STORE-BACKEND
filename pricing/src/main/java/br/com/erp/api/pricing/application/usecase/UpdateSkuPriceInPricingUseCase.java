package br.com.erp.api.pricing.application.usecase;

import br.com.erp.api.pricing.domain.entity.SkuPrice;
import br.com.erp.api.pricing.domain.exception.SkuPriceNotFoundException;
import br.com.erp.api.pricing.domain.port.SkuPriceRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class UpdateSkuPriceInPricingUseCase {

    private final SkuPriceRepositoryPort priceRepository;

    public UpdateSkuPriceInPricingUseCase(SkuPriceRepositoryPort priceRepository) {
        this.priceRepository = priceRepository;
    }

    @Transactional
    public void execute(Long skuId, BigDecimal costPrice, BigDecimal sellingPrice) {

        SkuPrice skuPrice = priceRepository.findBySkuId(skuId)
                .orElseThrow(() -> new SkuPriceNotFoundException(skuId));

        skuPrice.updatePrices(costPrice, sellingPrice);

        priceRepository.update(skuPrice);
    }
}