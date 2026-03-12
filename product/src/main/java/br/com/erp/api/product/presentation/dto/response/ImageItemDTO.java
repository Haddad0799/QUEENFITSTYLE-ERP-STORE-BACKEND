package br.com.erp.api.product.presentation.dto.response;

public record ImageItemDTO(
        Long id,
        String url,
        int order
) {}

