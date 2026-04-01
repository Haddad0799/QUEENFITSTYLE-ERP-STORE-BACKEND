package br.com.erp.api.product.application.usecase;

import br.com.erp.api.product.application.exception.ProductNotFoundException;
import br.com.erp.api.product.application.port.ProductCatalogPort;
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
public class EvaluateProductStatusUseCase {

    private static final Logger log = LoggerFactory.getLogger(EvaluateProductStatusUseCase.class);

    private final ProductRepositoryPort productRepository;
    private final SkuRepositoryPort skuRepository;
    private final ProductCatalogPort productCatalogPublisher;

    public EvaluateProductStatusUseCase(
            ProductRepositoryPort productRepository,
            SkuRepositoryPort skuRepository,
            ProductCatalogPort productCatalogPublisher
    ) {
        this.productRepository = productRepository;
        this.skuRepository = skuRepository;
        this.productCatalogPublisher = productCatalogPublisher;
    }

    @Transactional
    public void execute(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));

        List<Sku> skus = skuRepository.findByProductId(productId);

        boolean hasPublishedSkus = skus.stream().anyMatch(Sku::isPublished);
        boolean hasBlockedSkus = skus.stream().anyMatch(Sku::isBlocked);
        boolean hasPublishedOrBlockedSkus = hasPublishedSkus || hasBlockedSkus;
        boolean hasReadySkus = skus.stream().anyMatch(Sku::isReady);

        if (product.isPublished()) {
            if (!hasPublishedOrBlockedSkus) {

                product.markAsDraft();
                productRepository.updateStatus(product);
                productCatalogPublisher.unpublish(productId);
                return;
            }

                productCatalogPublisher.publish(productId);
                return;
        }
        // Produto não publicado: avaliar se deve ficar pronto para venda ou rascunho
        if (hasReadySkus || hasPublishedSkus) {
            product.markAsReadyForSale();
        } else {
            product.markAsDraft();
        }
        productRepository.updateStatus(product);
    }
}
