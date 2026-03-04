package br.com.erp.api.product.application.query;

import br.com.erp.api.product.application.gateway.StorageGateway;
import br.com.erp.api.product.application.provider.InventoryProvider;
import br.com.erp.api.product.application.provider.PriceProvider;
import br.com.erp.api.product.application.query.filter.SkuFilter;
import br.com.erp.api.product.domain.port.ProductColorImageRepositoryPort;
import br.com.erp.api.product.presentation.dto.response.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SkuQueryService {

    private final SkuQueryRepository repository;
    private final InventoryProvider inventoryProvider;
    private final PriceProvider priceProvider;
    private final ProductColorImageRepositoryPort imageRepository;
    private final StorageGateway storageGateway;

    public SkuQueryService(
            SkuQueryRepository repository,
            InventoryProvider inventoryProvider,
            PriceProvider priceProvider, ProductColorImageRepositoryPort imageRepository, StorageGateway storageGateway
    ) {
        this.repository = repository;
        this.inventoryProvider = inventoryProvider;
        this.priceProvider = priceProvider;
        this.imageRepository = imageRepository;
        this.storageGateway = storageGateway;
    }

    public Page<SkuSummaryDTO> findByProductId(
            Long productId,
            SkuFilter filter,
            Pageable pageable
    ) {
        return repository.findByProductId(productId, filter, pageable);
    }

    public SkuDetailsDTO findByProductIdAndSkuCode(
            Long productId,
            Long skuId
    ) {
        var sku = repository
                .findByProductIdAndSkuId(productId, skuId)
                .orElseThrow(() -> new RuntimeException("SKU not found"));

        SkuStock stock = inventoryProvider.getBySkuId(sku.id());
        SkuPriceDTO price = priceProvider.getBySkuId(sku.id());

        // busca imagens pela cor do SKU
        List<SkuImageDTO> images = imageRepository
                .findByProductIdAndColorId(productId, sku.attributes().colorId())
                .stream()
                .map(img -> new SkuImageDTO(
                        img.getId(),
                        storageGateway.getPublicUrl(img.getImageKey()),
                        img.getOrder()
                ))
                .toList();

        return new SkuDetailsDTO(
                sku.id(),
                sku.code(),
                sku.status(),
                sku.attributes(),
                sku.dimensions(),
                stock,
                price,
                images
        );
    }
}