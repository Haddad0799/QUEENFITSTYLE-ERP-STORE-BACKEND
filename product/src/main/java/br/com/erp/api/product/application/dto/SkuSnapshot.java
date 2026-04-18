package br.com.erp.api.product.application.dto;

import java.math.BigDecimal;
import java.util.List;

public record SkuSnapshot(
        Long skuId,
        String code,
        Long colorId,
        String colorName,
        String colorHex,
        String sizeName,
        BigDecimal sellingPrice,
        int availableStock,
        BigDecimal width,
        BigDecimal height,
        BigDecimal length,
        BigDecimal weight,
        List<String> imageUrls
) {}

