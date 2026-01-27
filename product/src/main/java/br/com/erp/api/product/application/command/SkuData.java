package br.com.erp.api.product.application.command;

import java.math.BigDecimal;

public record SkuData(
        String code,
        Long colorId,
        Long sizeId,
        BigDecimal width,
        BigDecimal height,
        BigDecimal length,
        BigDecimal weight
) {
}
