package br.com.erp.api.product.application.usecase;

import br.com.erp.api.product.application.provider.ImageProvider;
import br.com.erp.api.product.domain.entity.Sku;
import br.com.erp.api.product.domain.exception.SkuNotFoundException;
import br.com.erp.api.product.domain.port.SkuRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EvaluateSkuCompletenessUseCase {

    private final SkuRepositoryPort skuRepository;
    private final ImageProvider imageProvider;

    public EvaluateSkuCompletenessUseCase(
            SkuRepositoryPort skuRepository,
            ImageProvider imageProvider
    ) {
        this.skuRepository = skuRepository;
        this.imageProvider = imageProvider;
    }

    @Transactional
    public void execute(Long skuId) {

        Sku sku = skuRepository.findById(skuId)
                .orElseThrow(() -> new SkuNotFoundException(skuId));

        if (imageProvider.hasImage(skuId)) {
            sku.markAsReady();
        } else {
            sku.markAsIncomplete();
        }

        skuRepository.updateStatus(sku);
    }
}