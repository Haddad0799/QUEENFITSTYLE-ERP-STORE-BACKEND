package br.com.erp.api.product.presentation.dto.request;

import java.math.BigDecimal;

public record SkuItemDto(
        String code,
        Long colorId,
        Long sizeId,
        BigDecimal width,
        BigDecimal height,
        BigDecimal length,
        BigDecimal weight
) {
}
