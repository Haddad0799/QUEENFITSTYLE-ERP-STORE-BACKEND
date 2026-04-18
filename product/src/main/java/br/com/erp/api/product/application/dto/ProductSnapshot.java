package br.com.erp.api.product.application.dto;

import java.math.BigDecimal;
import java.util.List;

public record ProductSnapshot(
        Long productId,
        String name,
        String description,
        String slug,
        boolean isLaunch,
        Long categoryId,
        String categoryName,
        String categoryNormalizedName,
        Long parentCategoryId,
        String parentCategoryName,
        String parentCategoryNormalizedName,
        String mainImageUrl,
        ColorSnapshot mainColor,
        DefaultSelectionSnapshot defaultSelection,
        BigDecimal displayPrice,
        List<SkuSnapshot> skus
) {}

