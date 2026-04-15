package br.com.erp.api.catalog.application.query;

import java.util.List;

public interface CatalogCategoryQueryRepository {
    List<CatalogCategoryNodeView> findNavigableCategories();
}
