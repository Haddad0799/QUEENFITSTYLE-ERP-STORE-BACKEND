package br.com.erp.api.product.application.gateway;

import br.com.erp.api.product.presentation.dto.response.SkuStock;

import java.util.List;

public interface InventoryProvider {
    void initializeStocks(List<StockInitialization> stocks);
    SkuStock getBySkuId(Long skuId);
}