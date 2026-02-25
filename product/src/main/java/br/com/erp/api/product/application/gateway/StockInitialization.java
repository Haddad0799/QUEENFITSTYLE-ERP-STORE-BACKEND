package br.com.erp.api.product.application.gateway;

public record StockInitialization(
        Long skuId,
        Integer quantity)
{
}
