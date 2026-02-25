package br.com.erp.api.product.application.query;

import br.com.erp.api.product.application.query.filter.SkuFilter;
import br.com.erp.api.product.presentation.dto.response.SkuDetailsDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class SkuQueryService {

    private final SkuQueryRepository repository;

    public SkuQueryService(SkuQueryRepository repository) {
        this.repository = repository;
    }

    public Page<SkuDetailsDTO> findByProductId(Long productId, SkuFilter filter, Pageable pageable) {
        return repository.findByProductId(productId, filter, pageable);
    }
}
