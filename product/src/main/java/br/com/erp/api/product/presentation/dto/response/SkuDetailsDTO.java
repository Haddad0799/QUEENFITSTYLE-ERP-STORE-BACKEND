package br.com.erp.api.product.presentation.dto.response;

import java.util.List;

public record SkuDetailsDTO(
        Long id,
        String code,
        String status,
        SkuAttributes attributes,
        SkuDimensions dimensions,
        SkuStock stock,
        SkuPriceDTO price,
        List<SkuImageDTO> images ) {}