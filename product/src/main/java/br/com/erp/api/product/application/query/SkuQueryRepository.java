package br.com.erp.api.product.application.query;

import br.com.erp.api.product.application.query.filter.SkuFilter;
import br.com.erp.api.product.application.query.model.SkuBaseData;
import br.com.erp.api.product.presentation.dto.response.SkuSummaryDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface SkuQueryRepository {
    Page<SkuSummaryDTO> findByProductId(Long productId, SkuFilter filter, Pageable pageable);
    Optional<SkuBaseData> findByProductIdAndSkuId(Long productId, Long skuId);
}
