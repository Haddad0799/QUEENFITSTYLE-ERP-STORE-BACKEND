package br.com.erp.api.inventory.application.query.projection;

public record ProductSkuStockRow(
        Long productId,
        Long colorId,
        String colorName,
        String colorHex,
        Long skuId,
        String skuCode,
        String sizeName,
        int quantity,
        int reserved,
        int available,
        int minQuantity,
        boolean lowStock
) {}
