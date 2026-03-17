package br.com.erp.api.catalog.presentation.dto;

import java.util.List;

public record CatalogColorGroupDTO(
        String colorName,
        String colorHex,
        List<String> imageUrls,
        List<CatalogSkuSummaryDTO> skus
) {}

