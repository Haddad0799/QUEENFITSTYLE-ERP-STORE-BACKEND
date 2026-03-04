package br.com.erp.api.product.presentation.dto.response;

public record SkuStock(
        Integer total,
        Integer reserved,
        Integer available
) {}