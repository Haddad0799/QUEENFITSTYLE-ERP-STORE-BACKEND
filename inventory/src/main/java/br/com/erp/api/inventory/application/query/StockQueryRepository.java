package br.com.erp.api.inventory.application.query;

import br.com.erp.api.inventory.presentation.dto.StockMovementDTO;
import br.com.erp.api.inventory.presentation.dto.StockOverviewItemDTO;

import java.util.List;

public interface StockQueryRepository {

    List<StockOverviewItemDTO> findStockOverview();

    List<StockMovementDTO> findMovementsBySkuId(Long skuId);
}
