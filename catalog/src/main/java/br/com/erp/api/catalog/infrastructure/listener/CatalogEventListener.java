package br.com.erp.api.catalog.infrastructure.listener;

import br.com.erp.api.catalog.application.usecase.CatalogSyncService;
import br.com.erp.api.product.application.event.ProductPublishedEvent;
import br.com.erp.api.product.application.event.ProductUnpublishedEvent;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class CatalogEventListener {

    private final CatalogSyncService syncService;

    public CatalogEventListener(CatalogSyncService syncService) {
        this.syncService = syncService;
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onProductPublished(ProductPublishedEvent event) {
        syncService.publishProduct(event.snapshot());
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onProductUnpublished(ProductUnpublishedEvent event) {
        syncService.unpublishProduct(event.productId());
    }
}