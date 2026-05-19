package br.com.erp.api.catalog.infrastructure.listener;

import br.com.erp.api.catalog.application.usecase.CatalogSyncService;
import br.com.erp.api.product.application.assembler.SnapshotAssembler;
import br.com.erp.api.product.application.dto.ProductSnapshot;
import br.com.erp.api.product.domain.port.SkuRepositoryPort;
import br.com.erp.api.shared.event.InventoryReservationChangedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

// ...existing code...
import java.util.concurrent.*;

@Component
public class InventoryReservationListener {

    private static final Logger log = LoggerFactory.getLogger(InventoryReservationListener.class);

    private final CatalogSyncService catalogSyncService;
    private final SkuRepositoryPort skuRepository;
    private final SnapshotAssembler snapshotAssembler;

    // executor and pending tasks for debounce/coalesce
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "catalog-inv-listener");
        t.setDaemon(true);
        return t;
    });
    private final ConcurrentHashMap<Long, ScheduledFuture<?>> pending = new ConcurrentHashMap<>();

    public InventoryReservationListener(CatalogSyncService catalogSyncService,
                                        SkuRepositoryPort skuRepository,
                                        SnapshotAssembler snapshotAssembler) {
        this.catalogSyncService = catalogSyncService;
        this.skuRepository = skuRepository;
        this.snapshotAssembler = snapshotAssembler;
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onInventoryReservationChanged(InventoryReservationChangedEvent event) {
        Long skuId = event.skuId();
        if (skuId == null) {
            log.debug("Inventory event without skuId: {} - skipping sync", event.reservationId());
            return;
        }

        // Resolve productId via SkuRepository
        skuRepository.findById(skuId).ifPresent(sku -> {
            Long productId = sku.getProductId();
            log.info("Scheduling sync product {} from sku {} (event={})", productId, skuId, event.action());
            // schedule/coalesce a publish in 500ms
            ScheduledFuture<?> existing = pending.get(productId);
            if (existing != null && !existing.isDone()) {
                existing.cancel(false);
            }

            ScheduledFuture<?> future = scheduler.schedule(() -> {
                try {
                    log.info("Syncing product {} due to inventory event {}", productId, event.action());
                    ProductSnapshot snapshot = snapshotAssembler.assemble(productId);
                    catalogSyncService.publishProduct(snapshot);
                } catch (Exception e) {
                    log.error("Erro ao sincronizar produto {} a partir de evento de inventário", productId, e);
                } finally {
                    pending.remove(productId);
                }
            }, 500, TimeUnit.MILLISECONDS);

            pending.put(productId, future);
        });
    }
}

