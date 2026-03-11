package br.com.erp.api.inventory.infrastructure.adapter;

import br.com.erp.api.inventory.application.usecase.InitializeStockUseCase;
import br.com.erp.api.inventory.application.usecase.RegisterStockMovementUseCase;
import br.com.erp.api.inventory.domain.enumerated.MovementType;
import br.com.erp.api.product.application.dto.StockInitialization;
import br.com.erp.api.product.application.gateway.InventoryGateway;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class InventoryGatewayAdapter implements InventoryGateway {

    private final InitializeStockUseCase initializeStockUseCase;
    private final RegisterStockMovementUseCase registerStockMovementUseCase;

    public InventoryGatewayAdapter(
            InitializeStockUseCase initializeStockUseCase,
            RegisterStockMovementUseCase registerStockMovementUseCase
    ) {
        this.initializeStockUseCase = initializeStockUseCase;
        this.registerStockMovementUseCase = registerStockMovementUseCase;
    }

    @Override
    public void initializeStocks(List<StockInitialization> stocks) {
        initializeStockUseCase.execute(stocks);
    }

    @Override
    public void registerMovement(Long skuId, String type, int quantity, String reason) {
        registerStockMovementUseCase.execute(skuId, MovementType.valueOf(type), quantity, reason);
    }


}