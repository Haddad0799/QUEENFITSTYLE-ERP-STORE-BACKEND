package br.com.erp.api.product.application.gateway;

import br.com.erp.api.product.application.dto.PriceInitialization;

import java.util.List;

public interface PriceGateway {
    void initializePrices(List<PriceInitialization> prices);
}
