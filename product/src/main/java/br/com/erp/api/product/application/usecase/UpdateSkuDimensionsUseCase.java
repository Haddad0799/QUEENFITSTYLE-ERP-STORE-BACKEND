package br.com.erp.api.product.application.usecase;

import br.com.erp.api.product.application.command.UpdateSkuDimensionsCommand;
import br.com.erp.api.product.domain.entity.Sku;
import br.com.erp.api.product.domain.exception.SkuNotFoundException;
import br.com.erp.api.product.domain.port.SkuRepositoryPort;
import org.springframework.stereotype.Service;

@Service
public class UpdateSkuDimensionsUseCase {

    private final SkuRepositoryPort skuRepository;

    public UpdateSkuDimensionsUseCase(SkuRepositoryPort skuRepository) {
        this.skuRepository = skuRepository;
    }

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
    }
}