package br.com.erp.api.inventory.application.usecase;

import br.com.erp.api.inventory.application.port.out.InventoryRepositoryPort;
import br.com.erp.api.inventory.domain.entity.SkuStock;
import br.com.erp.api.inventory.presentation.dto.InventoryDTO;
import org.springframework.stereotype.Service;

@Service
public class GetStockUseCase {

    private final InventoryRepositoryPort repository;

    public GetStockUseCase(InventoryRepositoryPort repository) {
        this.repository = repository;
    }

    public InventoryDTO execute(Long skuId) {
        SkuStock stock = repository.findBySkuId(skuId).orElse(null);
        if (stock == null) {
            return new InventoryDTO(skuId, 0, 0, 0, 0);
        }

        int available = stock.getQuantity() - stock.getReserved();
        return new InventoryDTO(stock.getSkuId(), stock.getQuantity(), stock.getReserved(), stock.getMinQuantity(), available);
    }
}

