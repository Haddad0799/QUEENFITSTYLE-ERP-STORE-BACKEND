package br.com.erp.api.product.application.dto;

import java.math.BigDecimal;

public record ProductImportData(
        int rowNumber,
        String name,
        String slug,
        String category,
        String color,
        String size,
        String skuCode,
        BigDecimal width,
        BigDecimal height,
        BigDecimal length,
        BigDecimal weight,
        BigDecimal costPrice,
        BigDecimal sellingPrice,
        Integer stockQuantity
) {}