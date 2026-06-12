package br.com.erp.api.inventory.application.query.projection;

public record ProductSkuStockRow(
        Long productId,
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
