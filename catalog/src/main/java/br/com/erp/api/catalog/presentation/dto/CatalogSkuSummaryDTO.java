package br.com.erp.api.catalog.presentation.dto;

import java.math.BigDecimal;

public record CatalogSkuSummaryDTO(
        String code,
        String sizeName,
        BigDecimal sellingPrice,
        int availableStock,
        boolean inStock
) {}

