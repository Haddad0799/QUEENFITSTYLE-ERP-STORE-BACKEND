package br.com.erp.api.inventory.application.usecase;

import br.com.erp.api.inventory.application.port.out.InventoryRepositoryPort;
import br.com.erp.api.inventory.domain.enumerated.MovementType;
import org.springframework.stereotype.Service;

@Service
public class AdjustStockUseCase {

    private final RegisterStockMovementUseCase registerStockMovementUseCase;

    public AdjustStockUseCase(RegisterStockMovementUseCase registerStockMovementUseCase) {
        this.registerStockMovementUseCase = registerStockMovementUseCase;
    }

    public void adjust(Long skuId, int newQuantity, String reason) {
        if (newQuantity < 0) throw new IllegalArgumentException("A nova quantidade não pode ser negativa");
        registerStockMovementUseCase.execute(skuId, MovementType.ADJUSTMENT, newQuantity, reason);
    }
}

