package br.com.erp.api.inventory.application.usecase;

import br.com.erp.api.inventory.domain.entity.SkuStock;
import br.com.erp.api.inventory.domain.entity.StockMovement;
import br.com.erp.api.inventory.domain.enumerated.MovementType;
import br.com.erp.api.inventory.domain.port.SkuStockRepositoryPort;
import br.com.erp.api.inventory.domain.port.StockMovementRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RegisterStockMovementUseCase {

    private final SkuStockRepositoryPort stockRepository;
    private final StockMovementRepositoryPort movementRepository;

    public RegisterStockMovementUseCase(
            SkuStockRepositoryPort stockRepository,
            StockMovementRepositoryPort movementRepository
    ) {
        this.stockRepository = stockRepository;
        this.movementRepository = movementRepository;
    }

    @Transactional
    public void execute(Long skuId, MovementType type, int quantity, String reason) {

        SkuStock stock = stockRepository.findBySkuId(skuId)
                .orElseThrow(() -> new IllegalArgumentException("Estoque não encontrado: " + skuId));

        switch (type) {
            case INBOUND    -> stock.addStock(quantity);
            case ADJUSTMENT -> stock.adjust(quantity);
            default -> throw new IllegalArgumentException("Tipo não permitido manualmente: " + type);
        }

        // valida consistência
        if (stock.getQuantity() < stock.getMinQuantity()) {
            throw new IllegalArgumentException(
                    "Quantidade não pode ser menor que a quantidade mínima: " + stock.getMinQuantity()
            );
        }

        stockRepository.update(stock);

        movementRepository.save(new StockMovement(
                skuId, type, quantity, reason, null
        ));
    }
}