package br.com.erp.api.product.application.command;

public record RegisterSkuStockMovementCommand(
        Long productId,
        Long skuId,
        String type,
        int quantity,
        String reason
) {}