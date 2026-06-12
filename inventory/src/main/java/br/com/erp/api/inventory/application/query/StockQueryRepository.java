package br.com.erp.api.inventory.application.query;

import br.com.erp.api.inventory.application.query.projection.ProductSkuStockRow;
import br.com.erp.api.inventory.application.query.projection.ProductStockRow;
import br.com.erp.api.inventory.presentation.dto.StockMovementDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface StockQueryRepository {

    Page<ProductStockRow> findProductsWithStock(String search, Pageable pageable);

    List<ProductSkuStockRow> findSkuStockByProductIds(List<Long> productIds);

    List<StockMovementDTO> findMovementsBySkuId(Long skuId);
}
