package br.com.erp.api.product.presentation.dto.response;

import java.math.BigDecimal;

public record SkuDimensions(
        BigDecimal width,
        BigDecimal height,
        BigDecimal length,
        BigDecimal weight
) {}