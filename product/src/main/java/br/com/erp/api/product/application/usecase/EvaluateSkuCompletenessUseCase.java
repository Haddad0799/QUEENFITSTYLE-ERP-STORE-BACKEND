package br.com.erp.api.product.application.usecase;

import br.com.erp.api.product.application.provider.ImageProvider;
import br.com.erp.api.product.domain.entity.Product;
import br.com.erp.api.product.domain.entity.Sku;
import br.com.erp.api.product.domain.exception.SkuNotFoundException;
import br.com.erp.api.product.domain.port.ProductRepositoryPort;
import br.com.erp.api.product.domain.port.SkuRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class EvaluateSkuCompletenessUseCase {

    private final SkuRepositoryPort skuRepository;
    private final ProductRepositoryPort productRepository;
    private final ImageProvider imageProvider;
    private final EvaluateProductStatusUseCase evaluateProductStatus;

    public EvaluateSkuCompletenessUseCase(
            SkuRepositoryPort skuRepository,
            ProductRepositoryPort productRepository,
            ImageProvider imageProvider,
            EvaluateProductStatusUseCase evaluateProductStatus
    ) {
        this.skuRepository = skuRepository;
        this.productRepository = productRepository;
        this.imageProvider = imageProvider;
        this.evaluateProductStatus = evaluateProductStatus;
    }

    @Transactional
    public void execute(Long skuId) {
        execute(skuId, true);
    }

    @Transactional
    public void execute(Long skuId, boolean evaluateProduct) {

        Sku sku = skuRepository.findById(skuId)
                .orElseThrow(() -> new SkuNotFoundException(skuId));

        boolean hasImage = imageProvider.hasImage(skuId);

        boolean productPublished = productRepository.findById(sku.getProductId())
                .map(Product::isPublished)
                .orElse(false);

        evaluateSkuStatus(sku, hasImage, productPublished);

        skuRepository.updateStatus(sku);

        if (evaluateProduct) {
            evaluateProductStatus.execute(sku.getProductId());
        }
    }

    @Transactional
    public void executeBatch(List<Long> skuIds) {
        if (skuIds == null || skuIds.isEmpty()) {
            return;
        }

        Long productId = null;
        boolean productPublished = false;

        for (Long skuId : skuIds) {

            Sku sku = skuRepository.findById(skuId)
                    .orElseThrow(() -> new SkuNotFoundException(skuId));

            if (productId == null) {
                productId = sku.getProductId();

                productPublished = productRepository.findById(productId)
                        .map(Product::isPublished)
                        .orElse(false);
            }

            boolean hasImage = imageProvider.hasImage(skuId);

            evaluateSkuStatus(sku, hasImage, productPublished);

            skuRepository.updateStatus(sku);
        }

        if (productId != null) {
            evaluateProductStatus.execute(productId);
        }
    }

    /**
     * 🔥 REGRA DE NEGÓCIO CENTRALIZADA
     */
    private void evaluateSkuStatus(Sku sku, boolean hasImage, boolean productPublished) {

        if (hasImage) {

            //Se já está publicado → continua publicado
            if (sku.isPublished()) {
                return;
            }

            //Se estava incompleto
            if (sku.isIncomplete()) {

                if (productPublished) {
                    sku.markAsPublished();
                } else {
                    sku.markAsReady();
                }

                return;
            }

            //Caso padrão
            sku.markAsReady();

        } else {

            //Perdeu imagem → sempre vira incompleto
            sku.markAsIncomplete();
        }
    }
}