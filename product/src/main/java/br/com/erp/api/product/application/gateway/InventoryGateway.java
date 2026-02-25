package br.com.erp.api.product.application.gateway;

import java.util.List;

public interface InventoryGateway {
    void initializeStocks(List<StockInitialization> stocks);

}