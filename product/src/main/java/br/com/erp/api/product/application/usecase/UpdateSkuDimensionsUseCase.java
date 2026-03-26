package br.com.erp.api.product.application.usecase;

import br.com.erp.api.product.application.command.UpdateSkuDimensionsCommand;
import br.com.erp.api.product.application.port.ProductCatalogPort;
import br.com.erp.api.product.domain.entity.Sku;
import br.com.erp.api.product.domain.exception.SkuNotFoundException;
import br.com.erp.api.product.domain.port.SkuRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UpdateSkuDimensionsUseCase {

    private final SkuRepositoryPort skuRepository;
    private final ProductCatalogPort productCatalogPublisher;

    public UpdateSkuDimensionsUseCase(SkuRepositoryPort skuRepository, ProductCatalogPort productCatalogPublisher) {
        this.skuRepository = skuRepository;
        this.productCatalogPublisher = productCatalogPublisher;
    }

    @Transactional
    public void execute(UpdateSkuDimensionsCommand command) {
        Sku sku = skuRepository.findByProductIdAndSkuId(command.productId(), command.skuId())
                .orElseThrow(() -> new SkuNotFoundException(command.skuId()));

        sku.changeDimensions(
                command.width(),
                command.height(),
                command.length(),
                command.weight()
        );

        skuRepository.updateDimensions(sku);
        productCatalogPublisher.publishIfPublished(command.productId());
    }
}