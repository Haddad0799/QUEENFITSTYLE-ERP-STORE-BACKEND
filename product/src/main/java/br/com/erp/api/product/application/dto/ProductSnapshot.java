package br.com.erp.api.product.application.dto;

import java.util.List;

public record ProductSnapshot(
        Long productId,
        String name,
        String description,
        String slug,
        Long categoryId,
        String categoryName,
        String categoryNormalizedName,
        Long parentCategoryId,
        String parentCategoryName,
        String parentCategoryNormalizedName,
        String mainImageUrl,
        List<SkuSnapshot> skus
) {}

