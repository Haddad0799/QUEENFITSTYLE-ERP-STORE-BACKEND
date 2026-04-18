package br.com.erp.api.catalog.presentation.dto;

import java.math.BigDecimal;

public record CatalogDefaultSelectionDTO(
        String skuCode,
        String label,
        BigDecimal price
) {}
