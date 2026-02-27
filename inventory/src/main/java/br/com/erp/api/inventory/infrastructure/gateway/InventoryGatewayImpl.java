package br.com.erp.api.inventory.infrastructure.gateway;

import br.com.erp.api.inventory.application.usecase.InitializeStockUseCase;
import br.com.erp.api.product.application.gateway.InventoryGateway;
import br.com.erp.api.product.application.gateway.StockInitialization;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class InventoryGatewayImpl implements InventoryGateway {

    private final InitializeStockUseCase initializeStockUseCase;

    public InventoryGatewayImpl(InitializeStockUseCase initializeStockUseCase) {
        this.initializeStockUseCase = initializeStockUseCase;
    }

    @Override
    public void initializeStocks(List<StockInitialization> stocks) {
        initializeStockUseCase.execute(stocks);
    }
}