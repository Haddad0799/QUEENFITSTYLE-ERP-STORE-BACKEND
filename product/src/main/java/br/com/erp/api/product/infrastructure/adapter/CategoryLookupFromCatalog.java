package br.com.erp.api.product.infrastructure.adapter;

import br.com.erp.api.catalog.application.query.CategoryQueryService;
import br.com.erp.api.product.domain.port.CategoryLookupPort;
import org.springframework.stereotype.Component;

@Component
public class CategoryLookupFromCatalog implements CategoryLookupPort {

    private final CategoryQueryService categoryQueryService;

    public CategoryLookupFromCatalog(CategoryQueryService categoryQueryService) {
        this.categoryQueryService = categoryQueryService;
    }

    @Override
    public boolean existsActiveById(Long id) {
        return categoryQueryService.existsActiveById(id);
    }
}
