package br.com.erp.api.pricing.infrastructure.adapter;

import br.com.erp.api.pricing.application.usecase.InitializePriceUseCase;
import br.com.erp.api.pricing.application.usecase.UpdateSkuPriceInPricingUseCase;
import br.com.erp.api.product.application.dto.PriceInitialization;
import br.com.erp.api.product.application.gateway.PriceGateway;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class PriceGatewayAdapter implements PriceGateway {

    private final InitializePriceUseCase initializePriceUseCase;
    private final UpdateSkuPriceInPricingUseCase updateSkuPriceUseCase;

    public PriceGatewayAdapter(InitializePriceUseCase initializePriceUseCase, UpdateSkuPriceInPricingUseCase updateSkuPriceUseCase) {
        this.initializePriceUseCase = initializePriceUseCase;
        this.updateSkuPriceUseCase = updateSkuPriceUseCase;
    }

    @Override
    public void initializePrices(List<PriceInitialization> prices) {
        initializePriceUseCase.execute(prices);
    }

    @Override
    public void updatePrice(Long skuId, BigDecimal costPrice, BigDecimal sellingPrice) {
    updateSkuPriceUseCase.execute(skuId, costPrice, sellingPrice);
    }
}
