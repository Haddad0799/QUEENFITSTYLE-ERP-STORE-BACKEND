package br.com.erp.api.product.application.query;

import br.com.erp.api.product.application.gateway.InventoryProvider;
import br.com.erp.api.product.application.query.filter.SkuFilter;
import br.com.erp.api.product.presentation.dto.response.SkuDetailsDTO;
import br.com.erp.api.product.presentation.dto.response.SkuStock;
import br.com.erp.api.product.presentation.dto.response.SkuSummaryDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class SkuQueryService {

    private final SkuQueryRepository repository;
    private final InventoryProvider inventoryProvider;

    public SkuQueryService(
            SkuQueryRepository repository,
            InventoryProvider inventoryProvider
    ) {
        this.repository = repository;
        this.inventoryProvider = inventoryProvider;
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
                .findByProductIdAndSkuId(productId, skuId )
                .orElseThrow(() -> new RuntimeException("SKU not found"));

        SkuStock stock = inventoryProvider.getBySkuId(sku.id());

        return new SkuDetailsDTO(
                sku.id(),
                sku.code(),
                sku.status(),
                sku.attributes(),
                sku.dimensions(),
                stock
        );
    }
}