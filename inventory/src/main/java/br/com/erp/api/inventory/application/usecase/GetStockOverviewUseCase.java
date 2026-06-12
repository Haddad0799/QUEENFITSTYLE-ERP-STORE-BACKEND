package br.com.erp.api.inventory.application.usecase;

import br.com.erp.api.inventory.application.query.StockQueryRepository;
import br.com.erp.api.inventory.presentation.dto.StockOverviewItemDTO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GetStockOverviewUseCase {

    private final StockQueryRepository queryRepository;

    public GetStockOverviewUseCase(StockQueryRepository queryRepository) {
        this.queryRepository = queryRepository;
    }

    public List<StockOverviewItemDTO> execute() {
        return queryRepository.findStockOverview();
    }
}
