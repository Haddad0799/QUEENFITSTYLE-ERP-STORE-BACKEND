package br.com.erp.api.product.application.usecase;

import br.com.erp.api.product.application.dto.ProductSnapshot;
import br.com.erp.api.product.application.exception.ProductNotFoundException;
import br.com.erp.api.product.application.gateway.CatalogGateway;
import br.com.erp.api.product.domain.entity.Product;
import br.com.erp.api.product.domain.entity.Sku;
import br.com.erp.api.product.domain.exception.ProductNotReadyForSaleException;
import br.com.erp.api.product.domain.port.ProductRepositoryPort;
import br.com.erp.api.product.domain.port.SkuRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PublishProductUseCase {

    private final ProductRepositoryPort productRepository;
    private final SkuRepositoryPort skuRepository;
    private final SnapshotAssembler snapshotAssembler;
    private final CatalogGateway catalogGateway;

    public PublishProductUseCase(
            ProductRepositoryPort productRepository,
            SkuRepositoryPort skuRepository,
            SnapshotAssembler snapshotAssembler,
            CatalogGateway catalogGateway
    ) {
        this.productRepository = productRepository;
        this.skuRepository = skuRepository;
        this.snapshotAssembler = snapshotAssembler;
        this.catalogGateway = catalogGateway;
    }

    @Transactional
    public void execute(Long productId) {

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));

        List<Sku> skus = skuRepository.findByProductId(productId);

        List<Sku> readySkus = skus.stream()
                .filter(Sku::isReady)
                .toList();

        if (readySkus.isEmpty()) {
            throw new ProductNotReadyForSaleException();
        }

        // 1. Muda status no backoffice
        readySkus.forEach(Sku::activate);
        skus.stream()
                .filter(sku -> !sku.isReady() && !sku.isActive())
                .forEach(Sku::markAsIncomplete);

        product.publish();

        productRepository.updateStatus(product);
        skuRepository.updateStatusBatch(readySkus);

        // 2. Monta snapshot e propaga ao catálogo
        String categoryName = productRepository.findCategoryNameByProductId(productId);
        ProductSnapshot snapshot = snapshotAssembler.assemble(product, readySkus, categoryName);
        catalogGateway.publish(snapshot);
    }
}