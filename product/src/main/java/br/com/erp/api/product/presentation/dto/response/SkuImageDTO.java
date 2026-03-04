package br.com.erp.api.product.presentation.dto.response;

public record SkuImageDTO(
        Long id,
        String url,
        int order
) {}