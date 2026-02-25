package br.com.erp.api.product.application.query.filter;

public record SkuFilter(
        String status,
        Long colorId,
        Long sizeId
) {
}