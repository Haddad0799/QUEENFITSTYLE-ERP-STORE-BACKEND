package br.com.erp.api.catalog.presentation.dto;

import java.math.BigDecimal;

public record CatalogProductSummaryDTO(
        String name,
        String slug,
        String categoryName,
        String mainImageUrl,
        BigDecimal minPrice
) {}

