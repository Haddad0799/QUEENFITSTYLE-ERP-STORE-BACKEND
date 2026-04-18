package br.com.erp.api.product.application.dto;

import java.math.BigDecimal;

public record DefaultSelectionSnapshot(
        String skuCode,
        String label,
        BigDecimal price
) {}
