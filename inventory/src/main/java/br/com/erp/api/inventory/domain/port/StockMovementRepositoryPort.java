package br.com.erp.api.inventory.domain.port;

import br.com.erp.api.inventory.domain.entity.StockMovement;
import java.util.List;

public interface StockMovementRepositoryPort {
    void save(StockMovement movement);
    List<StockMovement> findBySkuId(Long skuId);
}