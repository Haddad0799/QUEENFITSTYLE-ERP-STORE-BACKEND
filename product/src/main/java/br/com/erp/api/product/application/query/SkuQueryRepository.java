package br.com.erp.api.product.application.query;

import br.com.erp.api.product.presentation.dto.response.SkuDetailsDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SkuQueryRepository {
    Page<SkuDetailsDTO> findByProductId(Long productId, Pageable pageable);
}
