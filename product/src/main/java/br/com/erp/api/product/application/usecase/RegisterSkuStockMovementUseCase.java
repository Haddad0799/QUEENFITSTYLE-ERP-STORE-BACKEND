package br.com.erp.api.product.application.usecase;

import br.com.erp.api.product.application.command.RegisterSkuStockMovementCommand;
import br.com.erp.api.product.application.gateway.InventoryGateway;
import br.com.erp.api.product.application.port.ProductCatalogPort;
import br.com.erp.api.product.domain.exception.SkuNotFoundException;
import br.com.erp.api.product.domain.port.SkuRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RegisterSkuStockMovementUseCase {

    private final SkuRepositoryPort skuRepository;
    private final InventoryGateway inventoryGateway;
    private final ProductCatalogPort productCatalogPublisher;

    public RegisterSkuStockMovementUseCase(
            SkuRepositoryPort skuRepository,
            InventoryGateway inventoryGateway, ProductCatalogPort productCatalogPublisher
    ) {
        this.skuRepository = skuRepository;
        this.inventoryGateway = inventoryGateway;
        this.productCatalogPublisher = productCatalogPublisher;
    }
    @Transactional
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

        productCatalogPublisher.publishIfPublished(command.productId());
    }
}