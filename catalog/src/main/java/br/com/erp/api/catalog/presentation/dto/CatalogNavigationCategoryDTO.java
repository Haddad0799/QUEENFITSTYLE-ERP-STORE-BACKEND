package br.com.erp.api.catalog.presentation.dto;

import java.util.List;

public record CatalogNavigationCategoryDTO(
        Long id,
        String name,
        String slug,
        long productCount,
        List<CatalogNavigationCategoryDTO> subcategories
) {}
