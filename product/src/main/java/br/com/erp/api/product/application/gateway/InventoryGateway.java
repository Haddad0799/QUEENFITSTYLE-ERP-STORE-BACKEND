package br.com.erp.api.product.application.gateway;

import br.com.erp.api.product.application.dto.StockInitialization;

import java.util.List;

public interface InventoryGateway {
    void initializeStocks(List<StockInitialization> stocks);
}