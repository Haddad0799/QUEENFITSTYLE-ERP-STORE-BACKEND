package br.com.erp.api.inventory.presentation.dto;

public record StockOverviewItemDTO(
        Long skuId,
        String skuCode,
        String productName,
        String colorName,
        String sizeName,
        int quantity,
        int reserved,
        int available,
        int minQuantity,
        boolean lowStock
) {}
