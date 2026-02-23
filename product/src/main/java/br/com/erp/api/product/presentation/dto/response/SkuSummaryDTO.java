package br.com.erp.api.product.presentation.dto.response;

import br.com.erp.api.product.domain.enumerated.SkuStatus;

public record SkuSummaryDTO(
        Long id,
        String code,
        String colorName,
        String sizeName,
        SkuStatus status
) {}

