package br.com.erp.api.catalog.presentation.dto;

import java.math.BigDecimal;
import java.util.List;

public record CatalogProductDetailDTO(
        String name,
        String description,
        String slug,
        CatalogCategoryDTO category,
        CatalogCategoryDTO subcategory,
        String mainImageUrl,
        BigDecimal minPrice,
        BigDecimal maxPrice,
        List<CatalogColorGroupDTO> colors
) {}

