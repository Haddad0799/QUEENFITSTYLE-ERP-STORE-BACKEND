package br.com.erp.api.catalog.presentation.dto;

import java.math.BigDecimal;
import java.util.List;

public record CatalogProductDetailDTO(
        String name,
        String description,
        String slug,
        boolean isLaunch,
        CatalogCategoryDTO category,
        CatalogCategoryDTO subcategory,
        String mainImageUrl,
        CatalogColorDTO mainColor,
        CatalogDefaultSelectionDTO defaultSelection,
        BigDecimal displayPrice,
        BigDecimal maxPrice,
        List<CatalogColorGroupDTO> colors
) {}

