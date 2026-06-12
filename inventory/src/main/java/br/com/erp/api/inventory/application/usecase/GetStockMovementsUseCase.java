package br.com.erp.api.inventory.application.usecase;

import br.com.erp.api.inventory.application.query.StockQueryRepository;
import br.com.erp.api.inventory.presentation.dto.StockMovementDTO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GetStockMovementsUseCase {

    private final StockQueryRepository queryRepository;

    public GetStockMovementsUseCase(StockQueryRepository queryRepository) {
        this.queryRepository = queryRepository;
    }

    public List<StockMovementDTO> execute(Long skuId) {
        return queryRepository.findMovementsBySkuId(skuId);
    }
}
