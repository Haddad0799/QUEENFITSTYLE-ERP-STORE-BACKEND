package br.com.erp.api.inventory.application.query.projection;

public record ProductStockRow(
        Long productId,
        String productName,
        String primaryImageKey
) {}
