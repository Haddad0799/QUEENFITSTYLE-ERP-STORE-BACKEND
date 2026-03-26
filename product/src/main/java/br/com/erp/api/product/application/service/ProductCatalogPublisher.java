package br.com.erp.api.product.application.service;

import br.com.erp.api.product.application.dto.ProductSnapshot;
import br.com.erp.api.product.application.event.ProductPublishedEvent;
import br.com.erp.api.product.application.event.ProductUnpublishedEvent;
import br.com.erp.api.product.application.usecase.SnapshotAssembler;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
public class ProductCatalogPublisher {

    private final ApplicationEventPublisher eventPublisher;
    private final SnapshotAssembler snapshotAssembler;

    public ProductCatalogPublisher(
            ApplicationEventPublisher eventPublisher,
            SnapshotAssembler snapshotAssembler
    ) {
        this.eventPublisher = eventPublisher;
        this.snapshotAssembler = snapshotAssembler;
    }

    public void publish(Long productId) {
        ProductSnapshot snapshot = snapshotAssembler.assemble(productId);
        eventPublisher.publishEvent(new ProductPublishedEvent(productId, snapshot));
    }

    public void unpublish(Long productId) {
        eventPublisher.publishEvent(new ProductUnpublishedEvent(productId));
    }
}