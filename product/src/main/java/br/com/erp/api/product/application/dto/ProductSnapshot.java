package br.com.erp.api.product.application.dto;

import java.util.List;

public record ProductSnapshot(
        Long productId,
        String name,
        String description,
        String slug,
        String categoryName,
        String mainImageUrl,
        List<SkuSnapshot> skus
) {}

