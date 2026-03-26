package br.com.erp.api.product.application.usecase;

import br.com.erp.api.product.application.exception.ProductNotFoundException;
import br.com.erp.api.product.application.port.ProductCatalogPort;
import br.com.erp.api.product.application.service.ProductCatalogPublisher;
import br.com.erp.api.product.domain.entity.Product;
import br.com.erp.api.product.domain.entity.Sku;
import br.com.erp.api.product.domain.port.ProductRepositoryPort;
import br.com.erp.api.product.domain.port.SkuRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class EvaluateProductStatusUseCase {

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

        boolean hasPublishedOrBlocked = skus.stream()
                .anyMatch(s -> s.isPublished() || s.isBlocked());

        boolean hasReady = skus.stream()
                .anyMatch(Sku::isReady);

        if (product.isPublished() && !hasPublishedOrBlocked) {
            product.markAsDraft();
            productRepository.updateStatus(product);
            productCatalogPublisher.unpublish(productId);
            return;
        }

        if (product.isPublished() && hasReady) {
            productCatalogPublisher.publish(productId);
            return;
        }

        if (!product.isPublished()) {
            if (hasReady) {
                product.markAsReadyForSale();
            } else {
                product.markAsDraft();
            }

            productRepository.updateStatus(product);
        }
    }
}