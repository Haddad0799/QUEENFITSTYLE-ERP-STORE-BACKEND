package br.com.erp.api.product.application.usecase;

import br.com.erp.api.product.application.provider.ImageProvider;
import br.com.erp.api.product.domain.entity.Sku;
import br.com.erp.api.product.domain.exception.SkuNotFoundException;
import br.com.erp.api.product.domain.port.SkuRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class EvaluateSkuCompletenessUseCase {

    private final SkuRepositoryPort skuRepository;
    private final ImageProvider imageProvider;
    private final EvaluateProductStatusUseCase evaluateProductStatus;

    public EvaluateSkuCompletenessUseCase(
            SkuRepositoryPort skuRepository,
            ImageProvider imageProvider,
            EvaluateProductStatusUseCase evaluateProductStatus
    ) {
        this.skuRepository = skuRepository;
        this.imageProvider = imageProvider;
        this.evaluateProductStatus = evaluateProductStatus;
    }

    /**
     * Avalia um único SKU e reavalia o status do produto
     */
    @Transactional
    public void execute(Long skuId) {
        execute(skuId, true);
    }

    /**
     * Avalia um único SKU com opção de reavaliar ou não o produto
     * @param skuId ID do SKU
     * @param evaluateProduct Se true, reavalia o produto após processar o SKU
     */
    @Transactional
    public void execute(Long skuId, boolean evaluateProduct) {
        Sku sku = skuRepository.findById(skuId)
                .orElseThrow(() -> new SkuNotFoundException(skuId));

        if (imageProvider.hasImage(skuId)) {
            sku.markAsReady();
        } else {
            sku.markAsIncomplete();
        }

        skuRepository.updateStatus(sku);

        if (evaluateProduct) {
            evaluateProductStatus.execute(sku.getProductId());
        }
    }

    /**
     * Avalia múltiplos SKUs em lote e reavalia o produto UMA ÚNICA VEZ no final.
     * Evita disparar múltiplos eventos quando processando SKUs da mesma cor/produto.
     * @param skuIds Lista de IDs dos SKUs a serem avaliados
     */
    @Transactional
    public void executeBatch(List<Long> skuIds) {
        if (skuIds == null || skuIds.isEmpty()) {
            return;
        }

        Long productId = null;

        for (Long skuId : skuIds) {
            Sku sku = skuRepository.findById(skuId)
                    .orElseThrow(() -> new SkuNotFoundException(skuId));

            // Captura o productId do primeiro SKU
            if (productId == null) {
                productId = sku.getProductId();
            }

            if (imageProvider.hasImage(skuId)) {
                sku.markAsReady();
            } else {
                sku.markAsIncomplete();
            }

            skuRepository.updateStatus(sku);
        }

        // Reavalia o produto UMA ÚNICA VEZ após processar todos os SKUs
        if (productId != null) {
            evaluateProductStatus.execute(productId);
        }
    }
}