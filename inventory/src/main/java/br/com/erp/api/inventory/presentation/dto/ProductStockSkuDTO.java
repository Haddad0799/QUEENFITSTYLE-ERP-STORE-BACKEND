package br.com.erp.api.inventory.presentation.dto;

public record ProductStockSkuDTO(
        Long skuId,
        String skuCode,
        String colorName,
        String sizeName,
        int quantity,
        int reserved,
        int available,
        int minQuantity,
        boolean lowStock
) {}
