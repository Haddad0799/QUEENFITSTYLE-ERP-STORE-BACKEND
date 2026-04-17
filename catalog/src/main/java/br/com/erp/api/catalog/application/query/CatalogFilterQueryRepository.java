package br.com.erp.api.catalog.application.query;

import br.com.erp.api.catalog.application.query.filter.ResolvedCatalogFilter;
import br.com.erp.api.catalog.presentation.dto.CatalogAvailableFiltersDTO;

public interface CatalogFilterQueryRepository {
    CatalogAvailableFiltersDTO findAvailableFilters(ResolvedCatalogFilter filter);
}
