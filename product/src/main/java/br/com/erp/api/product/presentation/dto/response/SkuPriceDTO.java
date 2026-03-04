package br.com.erp.api.product.presentation.dto.response;

import java.math.BigDecimal;

public record SkuPriceDTO(
        BigDecimal costPrice,
        BigDecimal sellingPrice
) {}