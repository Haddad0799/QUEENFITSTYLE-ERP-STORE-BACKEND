package br.com.erp.api.product.application.gateway;

import br.com.erp.api.product.application.dto.PriceInitialization;

import java.math.BigDecimal;
import java.util.List;

public interface PriceGateway {
    void initializePrices(List<PriceInitialization> prices);
    void updatePrice(Long skuId, BigDecimal costPrice, BigDecimal sellingPrice);
}
