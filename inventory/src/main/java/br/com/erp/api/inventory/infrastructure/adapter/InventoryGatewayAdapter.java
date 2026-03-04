package br.com.erp.api.inventory.infrastructure.adapter;

import br.com.erp.api.inventory.application.usecase.InitializeStockUseCase;
import br.com.erp.api.product.application.gateway.InventoryGateway;
import br.com.erp.api.product.application.dto.StockInitialization;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class InventoryGatewayAdapter implements InventoryGateway {

    private final InitializeStockUseCase initializeStockUseCase;

    public InventoryGatewayAdapter(
            InitializeStockUseCase initializeStockUseCase

    ) {
        this.initializeStockUseCase = initializeStockUseCase;
    }

    @Override
    public void initializeStocks(List<StockInitialization> stocks) {
        initializeStockUseCase.execute(stocks);
    }

}