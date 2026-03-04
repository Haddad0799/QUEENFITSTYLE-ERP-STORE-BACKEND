package br.com.erp.api.inventory.infrastructure.gateway;

import br.com.erp.api.inventory.application.usecase.InitializeStockUseCase;
import br.com.erp.api.inventory.domain.port.SkuStockRepositoryPort;
import br.com.erp.api.product.application.gateway.InventoryProvider;
import br.com.erp.api.product.application.gateway.StockInitialization;
import br.com.erp.api.product.presentation.dto.response.SkuStock;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class InventoryGatewayImpl implements InventoryProvider {

    private final InitializeStockUseCase initializeStockUseCase;
    private final SkuStockRepositoryPort stockRepository;

    public InventoryGatewayImpl(
            InitializeStockUseCase initializeStockUseCase,
            SkuStockRepositoryPort stockRepository
    ) {
        this.initializeStockUseCase = initializeStockUseCase;
        this.stockRepository = stockRepository;
    }

    @Override
    public void initializeStocks(List<StockInitialization> stocks) {
        initializeStockUseCase.execute(stocks);
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