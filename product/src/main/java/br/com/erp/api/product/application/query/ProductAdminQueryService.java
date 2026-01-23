package br.com.erp.api.product.application.query;

import br.com.erp.api.product.application.query.filter.ProductFilter;
import br.com.erp.api.product.presentation.dto.response.ProductDetailsDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class ProductAdminQueryService {

    private final ProductQueryRepository queryRepository;

    public ProductAdminQueryService(ProductQueryRepository queryRepository) {
        this.queryRepository = queryRepository;
    }

    public Page<ProductDetailsDTO> getAll(Boolean active, Long categoryId, Pageable pageable) {

        ProductFilter filter = new ProductFilter(active, categoryId);

        return queryRepository.findAll(filter, pageable);
    }

    public ProductDetailsDTO getProductById(Long id) {
        return queryRepository.findById(id);
    }
}
