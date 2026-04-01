package br.com.erp.api.product.application.dto;

import java.math.BigDecimal;

public record ProductImportData(
        String name,
        String description,
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