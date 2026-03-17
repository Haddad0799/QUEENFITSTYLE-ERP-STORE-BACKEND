package br.com.erp.api.product.infrastructure.adapter;

import br.com.erp.api.attribute.application.query.CategoryQueryService;
import br.com.erp.api.product.application.provider.CategoryProvider;
import org.springframework.stereotype.Component;

@Component
public class CategoryProviderFromAttribute implements CategoryProvider {

    private final CategoryQueryService categoryQueryService;

    public CategoryProviderFromAttribute(CategoryQueryService categoryQueryService) {
        this.categoryQueryService = categoryQueryService;
    }

    @Override
    public boolean existsActiveById(Long id) {
        return categoryQueryService.existsActiveById(id);
    }
}

