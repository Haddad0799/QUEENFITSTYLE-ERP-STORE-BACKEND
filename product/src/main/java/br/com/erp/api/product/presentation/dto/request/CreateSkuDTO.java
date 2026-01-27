package br.com.erp.api.product.presentation.dto.request;

import java.util.List;

public record CreateSkuDTO(
        List<SkuItemDto> skus
) {
}
