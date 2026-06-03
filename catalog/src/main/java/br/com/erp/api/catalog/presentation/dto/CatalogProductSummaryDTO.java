package br.com.erp.api.catalog.presentation.dto;

import java.math.BigDecimal;

public record CatalogProductSummaryDTO(
        String name,
        String slug,
        boolean isLaunch,
        CatalogCategoryDTO category,
        CatalogCategoryDTO subcategory,
        CatalogSelectionDTO selection,
        BigDecimal displayPrice,
        String displayImageUrl
) {}

