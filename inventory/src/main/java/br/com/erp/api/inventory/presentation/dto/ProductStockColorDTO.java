package br.com.erp.api.inventory.presentation.dto;

import java.util.List;

public record ProductStockColorDTO(
        Long colorId,
        String colorName,
        String colorHex,
        boolean hasLowStock,
        int totalAvailable,
        List<ProductStockSkuDTO> skus
) {}
