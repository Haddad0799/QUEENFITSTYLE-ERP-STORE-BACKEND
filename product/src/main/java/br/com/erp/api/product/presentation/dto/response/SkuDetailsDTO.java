package br.com.erp.api.product.presentation.dto.response;

import java.math.BigDecimal;

public record SkuDetailsDTO(
        Long id,
        String code,
        Long colorId,
        String colorName,
        Long sizeId,
        String sizeName,
        BigDecimal width,
        BigDecimal height,
        BigDecimal length,
        BigDecimal weight,
        boolean active
) {}
