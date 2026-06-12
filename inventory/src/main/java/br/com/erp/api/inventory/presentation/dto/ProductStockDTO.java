package br.com.erp.api.inventory.presentation.dto;

import java.util.List;

public record ProductStockDTO(
        Long productId,
        String productName,
        String primaryImageUrl,
        boolean hasLowStock,
        List<ProductStockColorDTO> colors
) {}
