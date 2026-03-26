package br.com.erp.api.product.application.service;

import br.com.erp.api.product.application.dto.ProductSnapshot;
import br.com.erp.api.product.application.event.ProductPublishedEvent;
import br.com.erp.api.product.application.event.ProductUnpublishedEvent;
import br.com.erp.api.product.application.port.ProductCatalogPort;
import br.com.erp.api.product.application.assembler.SnapshotAssembler;
import br.com.erp.api.product.domain.entity.Product;
import br.com.erp.api.product.domain.port.ProductRepositoryPort;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
public class ProductCatalogPublisher implements ProductCatalogPort {

    private final ProductRepositoryPort productRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final SnapshotAssembler snapshotAssembler;

    public ProductCatalogPublisher(
            ProductRepositoryPort productRepository,
            ApplicationEventPublisher eventPublisher,
            SnapshotAssembler snapshotAssembler
    ) {
        this.productRepository = productRepository;
        this.eventPublisher = eventPublisher;
        this.snapshotAssembler = snapshotAssembler;
    }

    /**
     *Publica snapshot SEMPRE (usado quando já sabemos que está publicado)
     */
    @Override
    public void publish(Long productId) {
        ProductSnapshot snapshot = snapshotAssembler.assemble(productId);

        eventPublisher.publishEvent(
                new ProductPublishedEvent(productId, snapshot)
        );
    }

    /**
     * 🛡️ Publica SOMENTE se o produto estiver publicado
     */
    @Override
    public void publishIfPublished(Long productId) {

        boolean isPublished = productRepository
                .findById(productId)
                .map(Product::isPublished)
                .orElse(false);

        if (!isPublished) {
            return;
        }

        publish(productId);
    }

    /**
     * Despublica
     */
    @Override
    public void unpublish(Long productId) {
        eventPublisher.publishEvent(
                new ProductUnpublishedEvent(productId)
        );
    }
}