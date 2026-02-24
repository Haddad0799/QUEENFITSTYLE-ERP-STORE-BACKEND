package br.com.erp.api.product.domain.exception;

public record SkuConflictDetail(
        Long colorId,
        String colorName,
        Long sizeId,
        String sizeName
) {}