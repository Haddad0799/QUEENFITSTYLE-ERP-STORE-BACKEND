package br.com.erp.api.product.application.usecase;

import br.com.erp.api.product.application.dto.ProductSnapshot;
import br.com.erp.api.product.application.event.ProductPublishedEvent;
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
    private final SnapshotAssembler snapshotAssembler;

    public EvaluateProductStatusUseCase(
            ProductRepositoryPort productRepository,
            SkuRepositoryPort skuRepository,
            ApplicationEventPublisher eventPublisher, SnapshotAssembler snapshotAssembler
    ) {
        this.productRepository = productRepository;
        this.skuRepository = skuRepository;
        this.eventPublisher = eventPublisher;
        this.snapshotAssembler = snapshotAssembler;
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

        // 🔴 CASO 1: Produto publicado sem SKUs ativos → despublica
        if (product.isPublished() && !hasPublishedOrBlocked) {
            product.markAsDraft();
            productRepository.updateStatus(product);

            eventPublisher.publishEvent(new ProductUnpublishedEvent(productId));
            return;
        }

        // 🟢 CASO 2: Produto publicado e tem SKUs READY → republica snapshot
        if (product.isPublished() && hasReady) {
            ProductSnapshot snapshot = snapshotAssembler.assemble(productId);
            eventPublisher.publishEvent(new ProductPublishedEvent(productId, snapshot));
            return;
        }

        // 🟡 CASO 3: Produto NÃO publicado → só atualiza estado interno
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