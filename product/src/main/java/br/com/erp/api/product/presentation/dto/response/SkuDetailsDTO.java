package br.com.erp.api.product.presentation.dto.response;

public record SkuDetailsDTO(
        Long id,
        String code,
        String status,
        SkuAttributes attributes,
        SkuDimensions dimensions,
        SkuStock stock
) {}