package br.com.erp.api.product.application.command;

import java.math.BigDecimal;

public record UpdateSkuPriceCommand(
        Long productId,
        Long skuId,
        BigDecimal costPrice,
        BigDecimal sellingPrice
) {}