package br.com.erp.api.product.presentation.dto.request;

public record CreateProductDTO(
        String name,
        String description,
        Long categoryId
) {
}
