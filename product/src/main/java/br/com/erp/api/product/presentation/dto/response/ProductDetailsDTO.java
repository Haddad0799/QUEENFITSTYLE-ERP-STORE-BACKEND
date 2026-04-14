package br.com.erp.api.product.presentation.dto.response;

import br.com.erp.api.product.domain.enumerated.ProductStatus;

import java.util.List;

public record ProductDetailsDTO(
        Long id,
        String name,
        String description,
        String slug,
        String mainImageUrl,
        Long categoryId,
        String categoryName,
        boolean isLaunch,
        ProductStatus status,
        List<SkuSummaryDTO> skus
) {}
