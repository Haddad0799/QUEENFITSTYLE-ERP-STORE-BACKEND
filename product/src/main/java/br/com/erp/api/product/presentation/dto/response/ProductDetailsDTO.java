package br.com.erp.api.product.presentation.dto.response;

public record ProductDetailsDTO(
        Long id,
        String name,
        String slug,
        Long categoryId,
        boolean active
) {
}
