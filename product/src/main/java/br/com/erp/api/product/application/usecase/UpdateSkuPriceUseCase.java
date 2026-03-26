package br.com.erp.api.product.application.usecase;

import br.com.erp.api.product.application.command.UpdateSkuPriceCommand;
import br.com.erp.api.product.application.gateway.PriceGateway;
import br.com.erp.api.product.application.port.ProductCatalogPort;
import br.com.erp.api.product.domain.exception.SkuNotFoundException;
import br.com.erp.api.product.domain.port.SkuRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UpdateSkuPriceUseCase {

    private final SkuRepositoryPort skuRepository;
    private final PriceGateway priceGateway;
    private final ProductCatalogPort productCatalogPublisher;

    public UpdateSkuPriceUseCase(
            SkuRepositoryPort skuRepository,
            PriceGateway priceGateway, ProductCatalogPort productCatalogPublisher
    ) {
        this.skuRepository = skuRepository;
        this.priceGateway = priceGateway;
        this.productCatalogPublisher = productCatalogPublisher;
    }

    @Transactional
    public void execute(UpdateSkuPriceCommand command) {

        if (!skuRepository.existsByProductIdAndSkuId(command.productId(), command.skuId())) {
            throw new SkuNotFoundException(command.skuId());
        }

        priceGateway.updatePrice(
                command.skuId(),
                command.costPrice(),
                command.sellingPrice()
        );

        productCatalogPublisher.publishIfPublished(command.productId());
    }
}