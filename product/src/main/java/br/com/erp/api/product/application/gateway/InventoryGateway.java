package br.com.erp.api.product.application.gateway;

import br.com.erp.api.product.application.dto.StockInitialization;

import java.util.List;

public interface InventoryGateway {
    void initializeStocks(List<StockInitialization> stocks);
    void registerMovement(Long skuId, String type, int quantity, String reason);
}