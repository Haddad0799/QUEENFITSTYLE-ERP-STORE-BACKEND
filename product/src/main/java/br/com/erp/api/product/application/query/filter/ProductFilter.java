package br.com.erp.api.product.application.query.filter;

import br.com.erp.api.product.domain.enumerated.ProductStatus;

public record ProductFilter(
        ProductStatus status,
        Long categoryId,
        Long colorId,
        Long sizeId
) {
}
