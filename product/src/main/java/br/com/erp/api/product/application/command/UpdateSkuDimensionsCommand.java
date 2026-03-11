package br.com.erp.api.product.application.command;

import java.math.BigDecimal;

public record UpdateSkuDimensionsCommand(
        Long productId,
        Long skuId,
        BigDecimal width,
        BigDecimal height,
        BigDecimal length,
        BigDecimal weight
) {}