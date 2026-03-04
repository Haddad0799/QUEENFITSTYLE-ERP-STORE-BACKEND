package br.com.erp.api.product.application.dto;

import java.math.BigDecimal;

public record PriceInitialization(
        Long skuId,
        BigDecimal costPrice,
        BigDecimal sellingPrice
) {
}
