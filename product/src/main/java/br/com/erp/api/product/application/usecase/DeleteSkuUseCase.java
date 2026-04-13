package br.com.erp.api.product.application.usecase;

import br.com.erp.api.product.application.exception.ProductNotFoundException;
import br.com.erp.api.product.domain.entity.Product;
import br.com.erp.api.product.domain.entity.Sku;
import br.com.erp.api.product.domain.port.ProductRepositoryPort;
import br.com.erp.api.product.domain.port.SkuRepositoryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DeleteSkuUseCase {

    private static final Logger log = LoggerFactory.getLogger(DeleteSkuUseCase.class);

    private final ProductRepositoryPort productRepository;
    private final SkuRepositoryPort skuRepository;
    private final EvaluateProductStatusUseCase evaluateProductStatus;

    public DeleteSkuUseCase(
            ProductRepositoryPort productRepository,
            SkuRepositoryPort skuRepository,
            EvaluateProductStatusUseCase evaluateProductStatus
    ) {
        this.productRepository = productRepository;
        this.skuRepository = skuRepository;
        this.evaluateProductStatus = evaluateProductStatus;
    }

    @Transactional
    public void execute(Long productId, List<Long> skuIds) {

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));

        // Não permite excluir SKU de produto publicado
        product.assertDeletable();

        // Valida que todos os SKUs pertencem ao produto
        List<Sku> skus = skuIds.stream()
                .map(skuId -> skuRepository.findByProductIdAndSkuId(productId, skuId)
                        .orElseThrow(() -> new IllegalArgumentException(
                                "SKU %d não encontrado para o produto %d".formatted(skuId, productId)
                        )))
                .toList();

        // DELETE em cascata: skus → sku_price, sku_stock → stock_movement
        skuRepository.deleteByIds(skuIds);

        // Reavalia o status do produto (pode voltar para DRAFT se não restar SKU pronto)
        evaluateProductStatus.execute(productId);

        skus.forEach(sku ->
                log.info("SKU {} (code={}) excluído do produto {}", sku.getId(), sku.getCode().value(), productId)
        );
    }
}

