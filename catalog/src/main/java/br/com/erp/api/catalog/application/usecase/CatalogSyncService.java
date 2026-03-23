package br.com.erp.api.catalog.application.usecase;

import br.com.erp.api.catalog.domain.port.CatalogRepositoryPort;
import br.com.erp.api.product.application.dto.ProductSnapshot;
import br.com.erp.api.product.application.dto.SkuSnapshot;
import br.com.erp.api.product.application.usecase.SnapshotAssembler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class CatalogSyncService {

    private final CatalogRepositoryPort catalogRepository;
    private final SnapshotAssembler snapshotAssembler;

    public CatalogSyncService(
            CatalogRepositoryPort catalogRepository,
            SnapshotAssembler snapshotAssembler
    ) {
        this.catalogRepository = catalogRepository;
        this.snapshotAssembler = snapshotAssembler;
    }

    // Chamado pelo ProductPublishedEvent — carga completa
    @Transactional
    public void publishProduct(ProductSnapshot snapshot) {
        if (catalogRepository.existsByProductId(snapshot.productId())) {
            catalogRepository.unpublishByProductId(snapshot.productId());
        }
        catalogRepository.publishProduct(snapshot);
    }

    // Chamado pelo ProductUnpublishedEvent
    @Transactional
    public void unpublishProduct(Long productId) {
        catalogRepository.unpublishByProductId(productId);
    }

    // Chamado pelo ProductAlteredEvent
    @Transactional
    public void syncProductInfo(Long productId) {
        if (!catalogRepository.existsByProductId(productId)) return;
        ProductSnapshot snapshot = snapshotAssembler.assemble(productId);
        catalogRepository.updateProductInfo(snapshot);
    }

    // Chamado pelo SkuPriceUpdatedEvent
    @Transactional
    public void syncSkuPrice(Long productId, Long skuId) {
        if (!catalogRepository.existsByProductId(productId)) return;
        BigDecimal sellingPrice = snapshotAssembler.assembleSkuSellingPrice(skuId);
        catalogRepository.updateSkuPrice(skuId, sellingPrice);
        catalogRepository.recalculateMinPrice(productId);
    }

    // Chamado pelo StockMovementRegisteredEvent
    @Transactional
    public void syncSkuStock(Long productId, Long skuId, int available) {
        if (!catalogRepository.existsByProductId(productId)) return;
        catalogRepository.updateSkuStock(skuId, available);
    }

    // Chamado pelo ColorImagesUpdatedEvent
    @Transactional
    public void syncColorImages(Long productId, Long colorId) {
        if (!catalogRepository.existsByProductId(productId)) return;
        List<String> imageUrls = snapshotAssembler.assembleColorImageUrls(productId, colorId);
        catalogRepository.updateColorGroupImages(productId, colorId, imageUrls);
    }

    // Chamado pelo SkuBecamePublishedEvent — novo SKU em produto já publicado
    @Transactional
    public void addSkuToCatalog(Long productId, Long skuId) {
        if (!catalogRepository.existsByProductId(productId)) return;
        SkuSnapshot skuSnapshot = snapshotAssembler.assembleSku(skuId);
        catalogRepository.addSku(productId, skuSnapshot);
    }
}