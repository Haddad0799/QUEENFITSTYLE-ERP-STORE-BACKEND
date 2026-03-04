package br.com.erp.api.product.presentation.dto.response;

public record SkuAttributes(
        Long colorId,
        String colorName,
        Long sizeId,
        String sizeName
) {}