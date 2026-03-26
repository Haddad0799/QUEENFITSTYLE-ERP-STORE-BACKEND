package br.com.erp.api.catalog.infrastructure.listener;

import br.com.erp.api.catalog.application.usecase.CatalogSyncService;
import br.com.erp.api.product.application.event.ProductPublishedEvent;
import br.com.erp.api.product.application.event.ProductUnpublishedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.concurrent.ConcurrentHashMap;

import static org.springframework.transaction.event.TransactionPhase.AFTER_COMMIT;

@Component
public class CatalogEventListener {

    private static final Logger log = LoggerFactory.getLogger(CatalogEventListener.class);

    private final CatalogSyncService catalogSyncService;
    private final ConcurrentHashMap<Long, Object> productLocks = new ConcurrentHashMap<>();

    public CatalogEventListener(CatalogSyncService catalogSyncService) {
        this.catalogSyncService = catalogSyncService;
    }

    /**
     * Processa evento de publicação de produto de forma assíncrona.
     * Usa sincronização por productId para evitar race conditions quando
     * múltiplos eventos do mesmo produto chegam simultaneamente.
     */
    @Async
    @TransactionalEventListener(phase = AFTER_COMMIT)
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void onProductPublished(ProductPublishedEvent event) {
        Long productId = event.snapshot().productId();

        log.debug("Recebido evento ProductPublishedEvent para produto {}", productId);

        // Obtém ou cria um lock específico para este produto
        Object lock = productLocks.computeIfAbsent(productId, k -> new Object());

        synchronized (lock) {
            try {
                log.info("Publicando produto {} no catálogo", productId);
                catalogSyncService.publishProduct(event.snapshot());
                log.info("Produto {} publicado com sucesso no catálogo", productId);
            } catch (Exception e) {
                log.error("Erro ao publicar produto {} no catálogo", productId, e);
                throw e;
            } finally {
                // Remove o lock após um breve delay para evitar acúmulo de memória
                // mas mantém proteção caso eventos duplicados cheguem muito próximos
                scheduleLockRemoval(productId);
            }
        }
    }

    @Async
    @TransactionalEventListener(phase = AFTER_COMMIT)
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void onProductUnpublished(ProductUnpublishedEvent event) {
        Long productId = event.productId();

        log.debug("Recebido evento ProductUnpublishedEvent para produto {}", productId);

        Object lock = productLocks.computeIfAbsent(productId, k -> new Object());

        synchronized (lock) {
            try {
                log.info("Despublicando produto {} do catálogo", productId);
                catalogSyncService.unpublishProduct(productId);
                log.info("Produto {} despublicado com sucesso do catálogo", productId);
            } catch (Exception e) {
                log.error("Erro ao despublicar produto {} do catálogo", productId, e);
                throw e;
            } finally {
                scheduleLockRemoval(productId);
            }
        }
    }

    /**
     * Agenda a remoção do lock após 1 segundo.
     * Isso evita que a memória acumule locks de produtos que não serão mais processados,
     * mas mantém proteção contra eventos duplicados que chegam em rajada.
     */
    private void scheduleLockRemoval(Long productId) {
        new Thread(() -> {
            try {
                Thread.sleep(1000);
                productLocks.remove(productId);
                log.trace("Lock removido para produto {}", productId);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("Thread de remoção de lock interrompida para produto {}", productId);
            }
        }).start();
    }
}