package br.com.erp.api.inventory.infrastructure.adapter;

import br.com.erp.api.inventory.domain.port.SkuStockRepositoryPort;
import br.com.erp.api.product.application.provider.InventoryProvider;
import br.com.erp.api.product.presentation.dto.response.SkuStock;
import org.springframework.stereotype.Component;

@Component
public class InventoryProviderAdapter implements InventoryProvider {

    private final SkuStockRepositoryPort stockRepository;

    public InventoryProviderAdapter(SkuStockRepositoryPort stockRepository) {
        this.stockRepository = stockRepository;
    }

    @Override
    public SkuStock getBySkuId(Long skuId) {

        var stock = stockRepository.findBySkuId(skuId)
                .orElse(null);

        if (stock == null) {
            return new SkuStock(0, 0, 0);
        }

        int available = stock.getQuantity() - stock.getReserved();

        return new SkuStock(
                stock.getQuantity(),
                stock.getReserved(),
                available
        );
    }
}
