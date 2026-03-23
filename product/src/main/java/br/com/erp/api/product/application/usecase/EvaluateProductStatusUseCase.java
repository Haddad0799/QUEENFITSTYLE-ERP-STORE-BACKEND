package br.com.erp.api.product.application.usecase;

import br.com.erp.api.product.application.event.ProductUnpublishedEvent;
import br.com.erp.api.product.application.exception.ProductNotFoundException;
import br.com.erp.api.product.domain.entity.Product;
import br.com.erp.api.product.domain.entity.Sku;
import br.com.erp.api.product.domain.port.ProductRepositoryPort;
import br.com.erp.api.product.domain.port.SkuRepositoryPort;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class EvaluateProductStatusUseCase {

    private final ProductRepositoryPort productRepository;
    private final SkuRepositoryPort skuRepository;
    private final ApplicationEventPublisher eventPublisher;

    public EvaluateProductStatusUseCase(
            ProductRepositoryPort productRepository,
            SkuRepositoryPort skuRepository,
            ApplicationEventPublisher eventPublisher
    ) {
        this.productRepository = productRepository;
        this.skuRepository = skuRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public void execute(Long productId) {

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));

        List<Sku> skus = skuRepository.findByProductId(productId);

        if (product.isPublished()) {
            boolean hasActiveSku = skus.stream()
                    .anyMatch(s -> s.isPublished() || s.isBlocked());

            if (!hasActiveSku) {
                product.markAsDraft();
                productRepository.updateStatus(product);
                eventPublisher.publishEvent(new ProductUnpublishedEvent(productId));
            }
        } else {

            boolean hasReadySku = skus.stream().anyMatch(Sku::isReady);
            if (hasReadySku) {
                product.markAsReadyForSale();
            } else {
                product.markAsDraft();
            }

            productRepository.updateStatus(product);
        }
    }
}