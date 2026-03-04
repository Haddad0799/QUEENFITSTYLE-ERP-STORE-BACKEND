package br.com.erp.api.product.application.dto;

public record StockInitialization(
        Long skuId,
        Integer quantity)
{
}
