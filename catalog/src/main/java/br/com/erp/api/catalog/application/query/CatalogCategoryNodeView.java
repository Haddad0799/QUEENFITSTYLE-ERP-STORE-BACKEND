package br.com.erp.api.catalog.application.query;

public record CatalogCategoryNodeView(
        Long id,
        String name,
        String slug,
        Long parentId,
        long directProductCount
) {}
