package br.com.erp.api.product.presentation.dto.response;

public record ProductSummaryDTO(
        Long id,
        String name,
        String slug,
        String categoryName,
        String status
) {}
