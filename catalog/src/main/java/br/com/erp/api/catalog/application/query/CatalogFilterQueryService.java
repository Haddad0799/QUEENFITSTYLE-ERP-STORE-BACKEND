package br.com.erp.api.catalog.application.query;

import br.com.erp.api.catalog.application.query.filter.CatalogFilter;
import br.com.erp.api.catalog.application.query.filter.ResolvedCatalogFilter;
import br.com.erp.api.catalog.presentation.dto.CatalogAvailableFiltersDTO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CatalogFilterQueryService {

    private final CatalogFilterQueryRepository queryRepository;
    private final CatalogCategoryScopeResolver categoryScopeResolver;

    public CatalogFilterQueryService(
            CatalogFilterQueryRepository queryRepository,
            CatalogCategoryScopeResolver categoryScopeResolver
    ) {
        this.queryRepository = queryRepository;
        this.categoryScopeResolver = categoryScopeResolver;
    }

    public CatalogAvailableFiltersDTO getAvailableFilters(CatalogFilter filter) {
        List<String> categorySlugs = categoryScopeResolver.resolve(filter);
        if (filter.hasCategory() && categorySlugs.isEmpty()) {
            return new CatalogAvailableFiltersDTO(List.of(), List.of());
        }

        return queryRepository.findAvailableFilters(ResolvedCatalogFilter.from(filter, categorySlugs));
    }
}
