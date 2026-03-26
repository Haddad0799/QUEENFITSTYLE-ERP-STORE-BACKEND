package br.com.erp.api.product.application.usecase;

import br.com.erp.api.product.application.exception.ProductNotFoundException;
import br.com.erp.api.product.application.service.ProductCatalogPublisher;
import br.com.erp.api.product.domain.entity.Product;
import br.com.erp.api.product.domain.entity.Sku;
import br.com.erp.api.product.domain.enumerated.SkuStatus;
import br.com.erp.api.product.domain.exception.ProductNotReadyForSaleException;
import br.com.erp.api.product.domain.port.ProductRepositoryPort;
import br.com.erp.api.product.domain.port.SkuRepositoryPort;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PublishProductUseCase {

    private final ProductRepositoryPort productRepository;
    private final SkuRepositoryPort skuRepository;
    private final ProductCatalogPublisher productCatalogPublisher;

    public PublishProductUseCase(
            ProductRepositoryPort productRepository,
            SkuRepositoryPort skuRepository,
            ProductCatalogPublisher productCatalogPublisher
    ) {
        this.productRepository = productRepository;
        this.skuRepository = skuRepository;
        this.productCatalogPublisher = productCatalogPublisher;
    }

    @Transactional
    public void execute(Long productId) {

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));

        List<Sku> readySkus = skuRepository.findByProductIdAndStatus(productId, SkuStatus.READY);

        if (readySkus.isEmpty()) {
            throw new ProductNotReadyForSaleException();
        }

        // Atualiza produto para PUBLISHED
        product.publish();
        productRepository.updateStatus(product);

        // Atualiza todos os SKUs READY para PUBLISHED em lote
        List<Long> skuIds = readySkus.stream().map(Sku::getId).toList();
        skuRepository.updateStatusBatch(skuIds, SkuStatus.PUBLISHED);

        // Monta snapshot completo com os dados atuais
        // O AFTER_COMMIT garante que o listener lê os dados já commitados
        productCatalogPublisher.publish(productId);
    }
}