package br.com.erp.api.pricing.application.usecase;

import br.com.erp.api.pricing.domain.entity.SkuPrice;
import br.com.erp.api.pricing.domain.port.SkuPriceRepositoryPort;
import br.com.erp.api.product.application.dto.PriceInitialization;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class InitializePriceUseCase {

    private final SkuPriceRepositoryPort priceRepository;

    public InitializePriceUseCase(SkuPriceRepositoryPort priceRepository) {
        this.priceRepository = priceRepository;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void execute(List<PriceInitialization> initializations) {

        List<SkuPrice> prices = initializations.stream()
                .map(init -> new SkuPrice(
                        init.skuId(),
                        init.costPrice(),
                        init.sellingPrice()
                ))
                .toList();

        priceRepository.saveAll(prices);
    }
}