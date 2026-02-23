package br.com.erp.api.product.application.query;

import br.com.erp.api.product.application.query.filter.ProductFilter;
import br.com.erp.api.product.presentation.dto.response.ProductDetailsDTO;
import br.com.erp.api.product.presentation.dto.response.ProductSummaryDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductQueryRepository {
    Page<ProductSummaryDTO> findAll(ProductFilter filter, Pageable pageable);
    ProductDetailsDTO findById(Long id);
}
