package br.com.erp.api.product.application.usecase;

import br.com.erp.api.product.application.command.UpdateSkuPriceCommand;
import br.com.erp.api.product.application.gateway.PriceGateway;
import br.com.erp.api.product.domain.exception.SkuNotFoundException;
import br.com.erp.api.product.domain.port.SkuRepositoryPort;
import org.springframework.stereotype.Service;

@Service
public class UpdateSkuPriceUseCase {

    private final SkuRepositoryPort skuRepository;
    private final PriceGateway priceGateway;

    public UpdateSkuPriceUseCase(
            SkuRepositoryPort skuRepository,
            PriceGateway priceGateway
    ) {
        this.skuRepository = skuRepository;
        this.priceGateway = priceGateway;
    }

    public void execute(UpdateSkuPriceCommand command) {

        if (!skuRepository.existsByProductIdAndSkuId(command.productId(), command.skuId())) {
            throw new SkuNotFoundException(command.skuId());
        }

        priceGateway.updatePrice(
                command.skuId(),
                command.costPrice(),
                command.sellingPrice()
        );
    }
}