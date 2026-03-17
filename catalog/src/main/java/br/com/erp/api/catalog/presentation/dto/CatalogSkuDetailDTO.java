package br.com.erp.api.catalog.presentation.dto;

import java.math.BigDecimal;
import java.util.List;

public record CatalogSkuDetailDTO(
        String productName,
        String productSlug,
        String code,
        String colorName,
        String colorHex,
        String sizeName,
        BigDecimal sellingPrice,
        int availableStock,
        boolean inStock,
        BigDecimal width,
        BigDecimal height,
        BigDecimal length,
        BigDecimal weight,
        List<String> imageUrls
) {}

