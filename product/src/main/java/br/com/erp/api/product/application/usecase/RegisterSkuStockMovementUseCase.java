package br.com.erp.api.product.application.usecase;

import br.com.erp.api.product.application.command.RegisterSkuStockMovementCommand;
import br.com.erp.api.product.application.gateway.InventoryGateway;
import br.com.erp.api.product.domain.exception.SkuNotFoundException;
import br.com.erp.api.product.domain.port.SkuRepositoryPort;
import org.springframework.stereotype.Service;

@Service
public class RegisterSkuStockMovementUseCase {

    private final SkuRepositoryPort skuRepository;
    private final InventoryGateway inventoryGateway;

    public RegisterSkuStockMovementUseCase(
            SkuRepositoryPort skuRepository,
            InventoryGateway inventoryGateway
    ) {
        this.skuRepository = skuRepository;
        this.inventoryGateway = inventoryGateway;
    }

    public void execute(RegisterSkuStockMovementCommand command) {

        if (!skuRepository.existsByProductIdAndSkuId(command.productId(), command.skuId())) {
            throw new SkuNotFoundException(command.skuId());
        }

        inventoryGateway.registerMovement(
                command.skuId(),
                command.type(),
                command.quantity(),
                command.reason()
        );
    }
}