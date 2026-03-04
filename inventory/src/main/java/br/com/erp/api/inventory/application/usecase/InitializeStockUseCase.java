package br.com.erp.api.inventory.application.usecase;

import br.com.erp.api.inventory.domain.entity.SkuStock;
import br.com.erp.api.inventory.domain.entity.StockMovement;
import br.com.erp.api.inventory.domain.enumerated.MovementType;
import br.com.erp.api.inventory.domain.port.SkuStockRepositoryPort;
import br.com.erp.api.inventory.domain.port.StockMovementRepositoryPort;
import br.com.erp.api.product.application.dto.StockInitialization;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class InitializeStockUseCase {

    private final SkuStockRepositoryPort stockRepository;
    private final StockMovementRepositoryPort movementRepository;

    public InitializeStockUseCase(
            SkuStockRepositoryPort stockRepository,
            StockMovementRepositoryPort movementRepository
    ) {
        this.stockRepository = stockRepository;
        this.movementRepository = movementRepository;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void execute(List<StockInitialization> initializations) {

        List<SkuStock> stocks = initializations.stream()
                .map(init -> new SkuStock(init.skuId(), init.quantity(), 0))  // quantidade real
                .toList();

        stockRepository.saveAll(stocks);

        initializations.forEach(init ->
                movementRepository.save(new StockMovement(
                        init.skuId(),
                        MovementType.INBOUND,
                        init.quantity(),
                        "Estoque inicial informado no cadastro do SKU",
                        null
                ))
        );
    }
}