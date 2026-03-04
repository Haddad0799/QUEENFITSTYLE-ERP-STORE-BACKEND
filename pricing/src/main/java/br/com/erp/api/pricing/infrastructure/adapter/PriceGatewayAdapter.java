package br.com.erp.api.pricing.infrastructure.adapter;

import br.com.erp.api.pricing.application.usecase.InitializePriceUseCase;
import br.com.erp.api.product.application.dto.PriceInitialization;
import br.com.erp.api.product.application.gateway.PriceGateway;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PriceGatewayAdapter implements PriceGateway {

    private final InitializePriceUseCase initializePriceUseCase;

    public PriceGatewayAdapter(InitializePriceUseCase initializePriceUseCase) {
        this.initializePriceUseCase = initializePriceUseCase;
    }

    @Override
    public void initializePrices(List<PriceInitialization> prices) {
        initializePriceUseCase.execute(prices);
    }
}
