package br.com.erp.api.catalog.application.query;

import br.com.erp.api.catalog.application.query.filter.CatalogFilter;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CatalogCategoryScopeResolver {

    private final CatalogCategoryQueryRepository categoryQueryRepository;

    public CatalogCategoryScopeResolver(CatalogCategoryQueryRepository categoryQueryRepository) {
        this.categoryQueryRepository = categoryQueryRepository;
    }

    public List<String> resolve(CatalogFilter filter) {
        if (!filter.hasCategory()) {
            return List.of();
        }

        return categoryQueryRepository.findCategoryAndDescendantSlugs(filter.category());
    }
}
