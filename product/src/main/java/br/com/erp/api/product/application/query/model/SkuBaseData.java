package br.com.erp.api.product.application.query.model;

import br.com.erp.api.product.presentation.dto.response.SkuAttributes;
import br.com.erp.api.product.presentation.dto.response.SkuDimensions;

public record SkuBaseData(
        Long id,
        String code,
        String status,
        SkuAttributes attributes,
        SkuDimensions dimensions
) {}