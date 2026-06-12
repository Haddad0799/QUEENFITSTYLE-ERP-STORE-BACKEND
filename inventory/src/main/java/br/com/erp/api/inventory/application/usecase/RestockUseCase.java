package br.com.erp.api.inventory.application.usecase;

import br.com.erp.api.inventory.domain.enumerated.MovementType;
import org.springframework.stereotype.Service;

@Service
public class RestockUseCase {

    private final RegisterStockMovementUseCase registerStockMovementUseCase;

    public RestockUseCase(RegisterStockMovementUseCase registerStockMovementUseCase) {
        this.registerStockMovementUseCase = registerStockMovementUseCase;
    }

    public void restock(Long skuId, int quantity) {
        if (quantity <= 0) throw new IllegalArgumentException("A quantidade de abastecimento deve ser positiva");
        registerStockMovementUseCase.execute(skuId, MovementType.INBOUND, quantity, null);
    }
}
